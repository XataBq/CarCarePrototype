package com.example.carcareprototype.bluetooth

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.carcareprototype.store.HistoryAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BluetoothRepository(context: Context) : BluetoothDiagnostics {
    private val scanner = BluetoothScanner(context)
    private val bleScanner = BleScanner(context)
    private val rfcomm = RfcommClient(context)
    private val gatt = GattExplorer(context)
    private val store = BluetoothDiagnosticStore(context)

    private val _state = MutableStateFlow(BluetoothUiState())
    override val state = _state.asStateFlow()
    private val linkLock = Any()
    private var linkSessionId = 0L
    private val historyQueue = Channel<HistoryAction>(Channel.UNLIMITED)
    private val historyScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var scannerStarted = false
    private var closed = false
    private val operationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var scanTimeout: Job? = null
    private var connectionTimeout: Job? = null

    init {
        historyScope.launch {
            runCatching { store.readLast() }
                .onSuccess { records -> _state.update { it.copy(records = records) } }
                .onFailure { Log.e("CarTesterBT", "History load failed", it) }
            for (action in historyQueue) {
                when (action) {
                    is HistoryAction.Append -> {
                        _state.update { it.copy(records = (it.records + action.record).takeLast(250)) }
                        runCatching { store.append(action.record) }
                            .onFailure { Log.e("CarTesterBT", "History write failed", it) }
                    }
                    HistoryAction.Clear -> {
                        runCatching { store.clear() }
                            .onSuccess { _state.update { it.copy(records = emptyList()) } }
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
            val previous = _state.value
            val update = updateBluetoothDevices(previous.devices, incoming)
            if (!_state.compareAndSet(previous, previous.copy(devices = update.devices))) continue
            if (update.meaningfulChange) {
                record("DEVICE", "Device discovered/updated: ${update.device.type}", update.device,
                    uuids = (update.device.bleAdvertisedUuids + update.device.uuids).takeIf { it.isNotEmpty() })
            }
            return update.device
        }
    }

    private fun handle(event: BluetoothScanEvent) {
        if (!scannerStarted) return
        when (event) {
            is BluetoothScanEvent.Adapter -> _state.update {
                it.copy(adapterState = it.adapterState.copy(available = event.available, enabled = event.enabled))
            }
            is BluetoothScanEvent.Scanning -> _state.update { it.copy(adapterState = it.adapterState.copy(scanning = event.active)) }
            is BluetoothScanEvent.BleScanning -> _state.update { it.copy(adapterState = it.adapterState.copy(bleScanning = event.active)) }
            is BluetoothScanEvent.DeviceSeen -> mergeDevice(event.device)
            is BluetoothScanEvent.SdpResult -> {
                val merged = mergeDevice(event.device)
                if (event.uuids == null) {
                    record("SDP_TIMEOUT", "EXTRA_UUID=null; saved UUIDs unchanged", merged)
                } else {
                    _state.update { state ->
                        state.copy(devices = state.devices.map { device ->
                            if (device.address == event.device.address) {
                                device.copy(uuids = event.uuids, uuidSource = BluetoothUuidSource.SDP)
                            } else device
                        })
                    }
                    record("SDP_RESULT", "UUID count=${event.uuids.size}; possibly cached", merged,
                        uuids = event.uuids)
                }
            }
            is BluetoothScanEvent.Message -> record(event.kind, event.text, address = event.address)
        }
    }

    private fun handleLink(event: BluetoothLinkEvent) {
        _state.update { it.copy(link = it.link.reduce(event)) }
        if (_state.value.link.phase !in setOf(LinkPhase.CONNECTING, LinkPhase.DISCOVERING)) {
            connectionTimeout?.cancel()
            connectionTimeout = null
        }
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

    override fun start() {
        if (closed) return
        if (scannerStarted) return
        scannerStarted = true
        scanner.start(::handle)
    }

    override fun stop() {
        if (!scannerStarted) return
        scannerStarted = false
        scanTimeout?.cancel()
        scanTimeout = null
        scanner.stop()
        bleScanner.stop()
        disconnect()
        _state.update { it.copy(adapterState = it.adapterState.copy(scanning = false, bleScanning = false)) }
    }

    override fun refreshPaired() = scanner.refreshPaired()
    override fun discover() {
        if (!scannerStarted || closed) return
        scanTimeout?.cancel()
        bleScanner.stop()
        scanner.discover()
    }
    override fun requestSdp(address: String) = scanner.requestSdp(address)
    override fun scanBle() {
        if (!scannerStarted || closed) return
        scanTimeout?.cancel()
        scanner.cancelDiscovery()
        bleScanner.start(::handle)
        if (_state.value.adapterState.bleScanning) {
            scanTimeout = operationScope.launch {
                delay(BLE_SCAN_TIMEOUT_MS)
                bleScanner.stop()
            }
        }
    }

    override fun connectSpp(address: String) {
        if (!scannerStarted || closed) return
        if (_state.value.link.isBusy) return
        val device = _state.value.devices.find { it.address == address }
        if (device?.canConnectSpp != true) {
            record("RFCOMM_SKIP", "Need a paired device advertising standard SPP UUID", address = address)
            return
        }
        stopScanning()
        rfcomm.connect(address, linkCallback())
        scheduleConnectionTimeout()
    }

    override fun connectGatt(address: String) {
        if (!scannerStarted || closed) return
        if (_state.value.link.isBusy) return
        val device = _state.value.devices.find { it.address == address }
        if (device?.canConnectGatt != true) {
            record("GATT_SKIP", "Device not observed as BLE-capable", address = address)
            return
        }
        stopScanning()
        gatt.connect(address, linkCallback())
        scheduleConnectionTimeout()
    }

    override fun disconnect() {
        val previous = synchronized(linkLock) {
            connectionTimeout?.cancel()
            connectionTimeout = null
            ++linkSessionId
            _state.value.link.also { _state.update { it.copy(link = BluetoothLinkState()) } }
        }
        rfcomm.stop()
        gatt.stop()
        if (previous.phase != LinkPhase.IDLE) {
            record("LINK_CLOSE", "Closed ${previous.mode} connection", address = previous.address)
        }
    }

    private fun stopScanning() {
        scanTimeout?.cancel()
        scanTimeout = null
        scanner.cancelDiscovery()
        bleScanner.stop()
    }

    private fun scheduleConnectionTimeout() = synchronized(linkLock) {
        connectionTimeout?.cancel()
        val session = linkSessionId
        if (_state.value.link.phase !in setOf(LinkPhase.CONNECTING, LinkPhase.DISCOVERING)) return@synchronized
        connectionTimeout = operationScope.launch {
            delay(CONNECTION_TIMEOUT_MS)
            abortTimedOutLink(session)
        }
    }

    private fun abortTimedOutLink(session: Long) = synchronized(linkLock) {
        if (session != linkSessionId) return@synchronized
        val current = _state.value.link
        if (current.phase !in setOf(LinkPhase.CONNECTING, LinkPhase.DISCOVERING)) return@synchronized
        disconnect()
        _state.update { it.copy(link = current.copy(phase = LinkPhase.FAILED, services = emptyList(), error = "Timeout (20s)")) }
        record("LINK_TIMEOUT", "Connection/discovery exceeded 20 seconds", address = current.address)
    }

    override fun clearHistory() { historyQueue.trySend(HistoryAction.Clear) }
    override fun exportHistory(uri: Uri) { historyQueue.trySend(HistoryAction.Export(uri)) }

    override fun close() {
        if (closed) return
        stop()
        closed = true
        operationScope.cancel()
        historyQueue.close()
    }

    private companion object {
        const val BLE_SCAN_TIMEOUT_MS = 12_000L
        const val CONNECTION_TIMEOUT_MS = 20_000L
    }
}
