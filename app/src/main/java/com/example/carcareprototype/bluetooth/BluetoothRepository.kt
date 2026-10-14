package com.example.carcareprototype.bluetooth

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.carcareprototype.store.HistoryAction
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
    private val linkLock = Any()
    private var linkSessionId = 0L



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

    private fun mergeDevice(incoming: BluetoothDeviceInfo): BluetoothDeviceInfo {
        while (true) {
            val previous = _devices.value
            val update = updateBluetoothDevices(previous, incoming)
            if (!_devices.compareAndSet(previous, update.devices)) continue
            if (update.meaningfulChange) {
                record("DEVICE", "Device discovered/updated: ${update.device.type}", update.device,
                    uuids = (update.device.bleAdvertisedUuids + update.device.uuids).takeIf { it.isNotEmpty() })
            }
            return update.device
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
                val merged = mergeDevice(event.device)
                if (event.uuids == null) {
                    record("SDP_TIMEOUT", "EXTRA_UUID=null; saved UUIDs unchanged", merged)
                } else {
                    _devices.update { devices ->
                        devices.map { device ->
                            if (device.address == event.device.address) {
                                device.copy(uuids = event.uuids, uuidSource = BluetoothUuidSource.SDP)
                            } else device
                        }
                    }
                    record("SDP_RESULT", "UUID count=${event.uuids.size}; possibly cached", merged,
                        uuids = event.uuids)
                }
            }
            is BluetoothScanEvent.Message -> record(event.kind, event.text, address = event.address)
        }
    }

    private fun handleLink(event: BluetoothLinkEvent) {
        _link.update { it.reduce(event) }
        when (event) {
            is BluetoothLinkEvent.Phase -> {
                record("LINK_${event.phase}", "${event.mode}: ${event.detail}", address = event.address)
            }
            is BluetoothLinkEvent.Failure -> {
                record("LINK_ERROR", "${event.mode}: ${event.reason}", address = event.address)
            }
            is BluetoothLinkEvent.GattServices -> {
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

    private fun linkCallback(): (BluetoothLinkEvent) -> Unit {
        val sessionId = synchronized(linkLock) { ++linkSessionId }
        return { event ->
            synchronized(linkLock) {
                if (sessionId == linkSessionId) handleLink(event)
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

    fun connectSpp(address: String) {
        if (_link.value.isBusy) return
        val device = _devices.value.find { it.address == address }
        if (device?.canConnectSpp != true) {
            record("RFCOMM_SKIP", "Need a paired device advertising standard SPP UUID", address = address)
            return
        }
        scanner.cancelDiscovery()
        bleScanner.stop()
        rfcomm.connect(address, linkCallback())
    }

    fun connectGatt(address: String) {
        if (_link.value.isBusy) return
        val device = _devices.value.find { it.address == address }
        if (device?.canConnectGatt != true) {
            record("GATT_SKIP", "Device not observed as BLE-capable", address = address)
            return
        }
        scanner.cancelDiscovery()
        bleScanner.stop()
        gatt.connect(address, linkCallback())
    }

    fun disconnect() {
        val previous = synchronized(linkLock) {
            ++linkSessionId
            _link.value.also { _link.value = BluetoothLinkState() }
        }
        rfcomm.stop()
        gatt.stop()
        if (previous.phase != LinkPhase.IDLE) {
            record("LINK_CLOSE", "Closed ${previous.mode} connection", address = previous.address)
        }
    }

    fun abortTimedOutLink() = synchronized(linkLock) {
        val current = _link.value
        if (current.phase !in setOf(LinkPhase.CONNECTING, LinkPhase.DISCOVERING)) return@synchronized
        disconnect()
        _link.value = current.copy(phase = LinkPhase.FAILED, services = emptyList(), error = "Timeout (20s)")
        record("LINK_TIMEOUT", "Connection/discovery exceeded 20 seconds", address = current.address)
    }

    fun clearHistory() { historyQueue.trySend(HistoryAction.Clear) }
    fun exportHistory(uri: Uri) { historyQueue.trySend(HistoryAction.Export(uri)) }

    fun close() {
        stop()
        historyQueue.close()
    }
}
