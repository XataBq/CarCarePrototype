package com.example.carcareprototype.bluetooth

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.carcareprototype.bluetooth.RfcommClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
class BluetoothRepository(context: Context) {
    private val scanner = BluetoothScanner(context)
    private val bleScanner = BleScanner(context)
    private val rfcomm = RfcommClient(context)
    private val gatt = GattExplorer(context)
    private val store = BluetoothDiagnosticStore(context)

    private val _devices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val devices = _devices.asStateFlow()

    private val _adapterState = MutableStateFlow(BluetoothAdapterState())
    val adapterState = _adapterState.asStateFlow()

    private val _records = MutableStateFlow<List<BluetoothRecord>>(emptyList())
    val records = _records.asStateFlow()

    private val _link = MutableStateFlow(BluetoothLinkState())
    val link = _link.asStateFlow()

    private sealed interface HistoryAction {
        data class Append(val record: BluetoothRecord) : HistoryAction
        data object Clear : HistoryAction
        data class Export(val uri: Uri) : HistoryAction
    }

    private val historyQueue = Channel<HistoryAction>(Channel.UNLIMITED)
    private val historyScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var scannerStarted = false

    init {
        historyScope.launch {
            runCatching { store.readLast() }
                .onSuccess { _records.value = it }
                .onFailure { Log.e("CarTesterBT", "History load failed", it) }
            for (action in historyQueue) {
                when (action) {
                    is HistoryAction.Append -> {
                        _records.update { (it + action.record).takeLast(250) }
                        runCatching { store.append(action.record) }
                            .onFailure { Log.e("CarTesterBT", "History write failed", it) }
                    }
                    HistoryAction.Clear -> {
                        runCatching { store.clear() }
                            .onSuccess { _records.value = emptyList() }
                            .onFailure { Log.e("CarTesterBT", "History clear failed", it) }
                    }
                    is HistoryAction.Export -> {
                        runCatching { store.export(action.uri) }
                            .onFailure { Log.e("CarTesterBT", "History export failed", it) }
                    }
                }
            }
        }
    }

    private fun record(
        kind: String,
        message: String,
        device: BluetoothDeviceInfo? = null,
        address: String? = null,
        uuids: List<String>? = null
    ) {
        val entry = BluetoothRecord(
            kind = kind,
            message = message,
            deviceAddress = device?.address ?: address,
            deviceName = device?.name?.takeUnless { it == "Unknown device" },
            uuids = uuids,
            rssi = device?.rssi
        )
        Log.d("CarTesterBT", "$kind: $message")
        historyQueue.trySend(HistoryAction.Append(entry))
    }

    private fun mergeDevice(incoming: BluetoothDeviceInfo) {
        var meaningfulChange = false
        _devices.update { devices ->
            val old = devices.find { it.address == incoming.address }
            val merged = if (old == null) incoming else incoming.copy(
                name = incoming.name.takeUnless { it == "Unknown device" } ?: old.name,
                type = when {
                    old.type == "Dual" || incoming.type == "Dual" -> "Dual"
                    incoming.observedBle && old.type == "Classic" -> "Dual"
                    old.observedBle && incoming.type == "Classic" -> "Dual"
                    incoming.type == "Unknown" -> old.type
                    else -> incoming.type
                },
                rssi = incoming.rssi ?: old.rssi,
                bonded = incoming.bonded,
                observedBle = incoming.observedBle || old.observedBle,
                bleAdvertisedUuids = incoming.bleAdvertisedUuids.ifEmpty { old.bleAdvertisedUuids },
                uuids = if (old.uuidSource == "SDP") old.uuids
                        else incoming.uuids.ifEmpty { old.uuids },
                uuidSource = if (old.uuidSource == "SDP") "SDP"
                             else if (incoming.uuids.isNotEmpty()) "CACHED"
                             else old.uuidSource
            )
            // RSSI fluctuations update the UI but should not flood persistent logs.
            meaningfulChange = old == null || old.name != merged.name || old.uuids != merged.uuids ||
                old.bleAdvertisedUuids != merged.bleAdvertisedUuids || old.type != merged.type ||
                old.bonded != merged.bonded || old.observedBle != merged.observedBle
            if (old == merged) return@update devices
            (devices.filterNot { it.address == merged.address } + merged)
                .sortedWith(compareByDescending<BluetoothDeviceInfo> { it.bonded }.thenBy { it.name })
        }
        if (meaningfulChange) {
            record("DEVICE", "Device discovered/updated: ${incoming.type}", incoming,
                uuids = (incoming.bleAdvertisedUuids + incoming.uuids).takeIf { it.isNotEmpty() })
        }
    }

    private fun handle(event: BluetoothScanEvent) {
        when (event) {
            is BluetoothScanEvent.Adapter -> _adapterState.update {
                it.copy(available = event.available, enabled = event.enabled)
            }
            is BluetoothScanEvent.Scanning -> _adapterState.update { it.copy(scanning = event.active) }
            is BluetoothScanEvent.BleScanning -> _adapterState.update { it.copy(bleScanning = event.active) }
            is BluetoothScanEvent.DeviceSeen -> mergeDevice(event.device)
            is BluetoothScanEvent.SdpResult -> {
                mergeDevice(event.device)
                if (event.uuids == null) {
                    record("SDP_TIMEOUT", "EXTRA_UUID=null; saved UUIDs unchanged", event.device)
                } else {
                    _devices.update { devices ->
                        devices.map { device ->
                            if (device.address == event.device.address) {
                                device.copy(uuids = event.uuids, uuidSource = "SDP")
                            } else device
                        }
                    }
                    record("SDP_RESULT", "UUID count=${event.uuids.size}; possibly cached", event.device,
                        uuids = event.uuids)
                }
            }
            is BluetoothScanEvent.Message -> record(event.kind, event.text, address = event.address)
        }
    }

    private fun handleLink(event: BluetoothLinkEvent) {
        when (event) {
            is BluetoothLinkEvent.Phase -> {
                _link.value = BluetoothLinkState(
                    mode = event.mode, phase = event.phase, address = event.address,
                    services = if (event.mode == LinkMode.GATT) _link.value.services else emptyList()
                )
                record("LINK_${event.phase}", "${event.mode}: ${event.detail}", address = event.address)
            }
            is BluetoothLinkEvent.Failure -> {
                _link.value = BluetoothLinkState(
                    mode = event.mode, phase = LinkPhase.FAILED,
                    address = event.address, error = event.reason
                )
                record("LINK_ERROR", "${event.mode}: ${event.reason}", address = event.address)
            }
            is BluetoothLinkEvent.GattServices -> {
                _link.update { it.copy(services = event.services) }
                val entries = event.services.flatMap { service ->
                    listOf("service:${service.uuid}") + service.characteristics.map { char ->
                        "characteristic:${char.uuid}:${char.properties}"
                    }
                }
                record("GATT_SERVICES", "${event.services.size} services, ${entries.size - event.services.size} characteristics",
                    address = event.address, uuids = entries)
            }
            is BluetoothLinkEvent.IncomingBytes -> {
                record("RFCOMM_RX", "Received ${event.size} bytes (max 64 hex preview): ${event.hexPreview}",
                    address = event.address)
            }
        }
    }

    fun start() {
        if (scannerStarted) return
        scannerStarted = true
        scanner.start(::handle)
    }

    fun stop() {
        scanner.stop()
        bleScanner.stop()
        disconnect()
        scannerStarted = false
        _adapterState.update { it.copy(scanning = false, bleScanning = false) }
    }

    fun refreshPaired() = scanner.refreshPaired()
    fun discover() {
        bleScanner.stop()
        scanner.discover()
    }
    fun requestSdp(address: String) = scanner.requestSdp(address)
    fun scanBle() {
        scanner.cancelDiscovery()
        bleScanner.start(::handle)
    }
    fun stopBleScan() = bleScanner.stop()

    private fun busy(): Boolean = _link.value.phase in setOf(
        LinkPhase.CONNECTING, LinkPhase.CONNECTED, LinkPhase.DISCOVERING, LinkPhase.READY
    )

    fun connectSpp(address: String) {
        if (busy()) return
        val device = _devices.value.find { it.address == address }
        if (device == null || !device.bonded || RfcommClient.SPP_UUID_TEXT !in device.uuids) {
            record("RFCOMM_SKIP", "Need a paired device advertising standard SPP UUID", address = address)
            return
        }
        scanner.cancelDiscovery()
        bleScanner.stop()
        rfcomm.connect(address, ::handleLink)
    }

    fun connectGatt(address: String) {
        if (busy()) return
        val device = _devices.value.find { it.address == address }
        if (device == null || !(device.observedBle || device.type == "BLE" || device.type == "Dual")) {
            record("GATT_SKIP", "Device not observed as BLE-capable", address = address)
            return
        }
        scanner.cancelDiscovery()
        bleScanner.stop()
        gatt.connect(address, ::handleLink)
    }

    fun disconnect() {
        rfcomm.stop()
        gatt.stop()
        val previous = _link.value
        if (previous.phase != LinkPhase.IDLE) {
            record("LINK_CLOSE", "Closed ${previous.mode} connection", address = previous.address)
        }
        _link.value = BluetoothLinkState()
    }

    fun abortTimedOutLink() {
        val current = _link.value
        if (current.phase !in setOf(LinkPhase.CONNECTING, LinkPhase.DISCOVERING)) return
        disconnect()
        _link.value = current.copy(phase = LinkPhase.FAILED, error = "Timeout (20s)")
        record("LINK_TIMEOUT", "Connection/discovery exceeded 20 seconds", address = current.address)
    }

    fun clearHistory() { historyQueue.trySend(HistoryAction.Clear) }
    fun exportHistory(uri: Uri) { historyQueue.trySend(HistoryAction.Export(uri)) }

    fun close() {
        stop()
        historyQueue.close()
    }
}
