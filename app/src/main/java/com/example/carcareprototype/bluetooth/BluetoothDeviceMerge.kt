package com.example.carcareprototype.bluetooth

internal data class BluetoothDeviceUpdate(
    val devices: List<BluetoothDeviceInfo>,
    val device: BluetoothDeviceInfo,
    val meaningfulChange: Boolean
)

internal fun mergeBluetoothDevice(
    previous: BluetoothDeviceInfo?,
    incoming: BluetoothDeviceInfo
): BluetoothDeviceInfo {
    if (previous == null) return incoming
    return incoming.copy(
        name = incoming.name.takeUnless { it == "Unknown device" } ?: previous.name,
        type = when {
            previous.type == BluetoothDeviceType.DUAL || incoming.type == BluetoothDeviceType.DUAL ->
                BluetoothDeviceType.DUAL
            incoming.observedBle && previous.type == BluetoothDeviceType.CLASSIC -> BluetoothDeviceType.DUAL
            previous.observedBle && incoming.type == BluetoothDeviceType.CLASSIC -> BluetoothDeviceType.DUAL
            incoming.type == BluetoothDeviceType.UNKNOWN -> previous.type
            else -> incoming.type
        },
        rssi = incoming.rssi ?: previous.rssi,
        observedBle = incoming.observedBle || previous.observedBle,
        bleAdvertisedUuids = incoming.bleAdvertisedUuids.ifEmpty { previous.bleAdvertisedUuids },
        uuids = if (previous.uuidSource == BluetoothUuidSource.SDP) previous.uuids
                else incoming.uuids.ifEmpty { previous.uuids },
        uuidSource = when {
            previous.uuidSource == BluetoothUuidSource.SDP -> BluetoothUuidSource.SDP
            incoming.uuids.isNotEmpty() -> BluetoothUuidSource.CACHED
            else -> previous.uuidSource
        }
    )
}

private val deviceOrder = compareByDescending<BluetoothDeviceInfo> { it.bonded }.thenBy { it.name }

internal fun updateBluetoothDevices(
    devices: List<BluetoothDeviceInfo>,
    incoming: BluetoothDeviceInfo
): BluetoothDeviceUpdate {
    val index = devices.indexOfFirst { it.address == incoming.address }
    val previous = devices.getOrNull(index)
    val merged = mergeBluetoothDevice(previous, incoming)
    if (previous == merged) return BluetoothDeviceUpdate(devices, merged, false)

    val meaningfulChange = previous == null || merged.copy(
        rssi = previous.rssi, uuidSource = previous.uuidSource
    ) != previous
    val updated = devices.toMutableList().apply {
        if (index < 0) add(merged) else this[index] = merged
    }
    val sortOrderChanged = previous == null || previous.name != merged.name || previous.bonded != merged.bonded
    return BluetoothDeviceUpdate(
        devices = if (sortOrderChanged) updated.sortedWith(deviceOrder) else updated,
        device = merged,
        meaningfulChange = meaningfulChange
    )
}
