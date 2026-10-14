package com.example.carcareprototype.bluetooth

import java.util.UUID

internal const val SPP_SERVICE_UUID = "00001101-0000-1000-8000-00805f9b34fb"

enum class BluetoothDeviceType(private val label: String) {
    CLASSIC("Classic"), BLE("BLE"), DUAL("Dual"), UNKNOWN("Unknown");

    override fun toString(): String = label
}

enum class BluetoothUuidSource { NONE, CACHED, SDP }

data class BluetoothDeviceInfo(
    val name: String,
    val address: String,
    val bonded: Boolean,
    val type: BluetoothDeviceType,
    val rssi: Int? = null,
    val uuids: List<String> = emptyList(),
    val uuidSource: BluetoothUuidSource = BluetoothUuidSource.NONE,
    val bleAdvertisedUuids: List<String> = emptyList(),
    val observedBle: Boolean = false
) {
    val canConnectSpp: Boolean
        get() = bonded && SPP_SERVICE_UUID in uuids

    val canConnectGatt: Boolean
        get() = observedBle || type == BluetoothDeviceType.BLE || type == BluetoothDeviceType.DUAL
}

data class BluetoothAdapterState(
    val available: Boolean = false,
    val enabled: Boolean = false,
    val scanning: Boolean = false,
    val bleScanning: Boolean = false
)

data class BluetoothRecord(
    val id: String = UUID.randomUUID().toString(),
    val timestampMillis: Long = System.currentTimeMillis(),
    val kind: String,
    val message: String,
    val deviceAddress: String? = null,
    val deviceName: String? = null,
    val uuids: List<String>? = null,
    val rssi: Int? = null
)

data class GattCharacteristicInfo(val uuid: String, val properties: String)

data class GattServiceInfo(
    val uuid: String,
    val characteristics: List<GattCharacteristicInfo>
)

enum class LinkMode { NONE, RFCOMM, GATT }
enum class LinkPhase { IDLE, CONNECTING, CONNECTED, DISCOVERING, READY, FAILED }

data class BluetoothLinkState(
    val mode: LinkMode = LinkMode.NONE,
    val phase: LinkPhase = LinkPhase.IDLE,
    val address: String? = null,
    val services: List<GattServiceInfo> = emptyList(),
    val error: String? = null
) {
    val isBusy: Boolean
        get() = when (phase) {
            LinkPhase.CONNECTING, LinkPhase.CONNECTED, LinkPhase.DISCOVERING, LinkPhase.READY -> true
            LinkPhase.IDLE, LinkPhase.FAILED -> false
        }
}
data class BluetoothUiState(
    val devices: List<BluetoothDeviceInfo> = emptyList(),
    val adapterState: BluetoothAdapterState = BluetoothAdapterState(),
    val records: List<BluetoothRecord> = emptyList(),
    val link: BluetoothLinkState = BluetoothLinkState()
)

sealed interface BluetoothScanEvent {
    data class Adapter(val available: Boolean, val enabled: Boolean) : BluetoothScanEvent
    data class Scanning(val active: Boolean) : BluetoothScanEvent
    data class BleScanning(val active: Boolean) : BluetoothScanEvent
    data class DeviceSeen(val device: BluetoothDeviceInfo) : BluetoothScanEvent
    data class SdpResult(
        val device: BluetoothDeviceInfo,
        val uuids: List<String>?
    ) : BluetoothScanEvent
    data class Message(
        val kind: String,
        val text: String,
        val address: String? = null
    ) : BluetoothScanEvent
}

sealed interface BluetoothLinkEvent {
    data class Phase(
        val mode: LinkMode,
        val address: String,
        val phase: LinkPhase,
        val detail: String
    ) : BluetoothLinkEvent

    data class GattServices(
        val address: String,
        val services: List<GattServiceInfo>
    ) : BluetoothLinkEvent

    data class IncomingBytes(val address: String, val size: Int, val hexPreview: String) : BluetoothLinkEvent

    data class Failure(val mode: LinkMode, val address: String, val reason: String) : BluetoothLinkEvent
}
