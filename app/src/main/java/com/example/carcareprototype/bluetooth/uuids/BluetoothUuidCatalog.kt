package com.example.carcareprototype.bluetooth.uuids

import java.util.Locale
import java.util.UUID

/** Type of Bluetooth identifier. SDP services, GATT services and attributes are separate namespaces. */
enum class UuidKind {
    CLASSIC_SERVICE,
    GATT_SERVICE,
    GATT_CHARACTERISTIC,
    GATT_DESCRIPTOR,
    VENDOR_SERVICE,
    VENDOR_CHARACTERISTIC
}

data class UuidMeaning(
    val name: String,
    val description: String,
    val kind: UuidKind
)

/** Pure Kotlin dictionary. No Android Context, resources, database, or I/O required. */
object BluetoothUuidCatalog {
    private const val SIG_SUFFIX = "-0000-1000-8000-00805f9b34fb"
    private val shortPattern = Regex("^(?:0x)?([0-9a-f]{4})$")
    private val expandedPattern = Regex("^0000([0-9a-f]{4})${Regex.escape(SIG_SUFFIX)}$")

    /** 0x180F / 180F / 0000180f-0000-1000-8000-00805f9b34fb -> 0x180F.
     * Other 128-bit vendor UUIDs cannot be shortened this way.
     */
    fun assigned16(uuid: String): Int? {
        val text = uuid.trim().lowercase(Locale.ROOT)
        return (shortPattern.matchEntire(text) ?: expandedPattern.matchEntire(text))
            ?.groupValues?.get(1)?.toIntOrNull(16)
    }

    fun classic(uuid: String): UuidMeaning? =
        assigned16(uuid)?.let(ClassicServiceCatalog.entries::get)

    fun gattService(uuid: String): UuidMeaning? =
        VendorUuidCatalog.services[normalize(uuid)]
            ?: assigned16(uuid)?.let(GattServiceCatalog.entries::get)

    fun gattCharacteristic(serviceUuid: String, characteristicUuid: String): UuidMeaning? {
        val service = normalize(serviceUuid)
        val characteristic = normalize(characteristicUuid)
        return VendorUuidCatalog.characteristics[service to characteristic]
            ?: assigned16(characteristic)?.let(GattCharacteristicCatalog.entries::get)
    }

    fun gattDescriptor(uuid: String): UuidMeaning? =
        assigned16(uuid)?.let(GattDescriptorCatalog.entries::get)

    fun displayName(info: UuidMeaning?): String = info?.name ?: "Неизвестный UUID"

    fun description(info: UuidMeaning?): String =
        info?.description ?: "Назначение не определено; возможно, собственный сервис производителя"

    /** Preserve the technical identifier in UI and logs. */
    fun displayLabel(uuid: String, info: UuidMeaning?): String =
        "${displayName(info)} ($uuid)"

    /** Normalize known UUID spelling for vendor lookup; no attempts to guess partial codes. */
    private fun normalize(uuid: String): String {
        val raw = uuid.trim().lowercase(Locale.ROOT)
        return assigned16(raw)?.let { String.format(Locale.ROOT, "%08x", it) + SIG_SUFFIX }
            ?: raw
    }

    fun classic(uuid: UUID): UuidMeaning? = classic(uuid.toString())
    fun gattService(uuid: UUID): UuidMeaning? = gattService(uuid.toString())
    fun gattCharacteristic(serviceUuid: UUID, characteristicUuid: UUID): UuidMeaning? =
        gattCharacteristic(serviceUuid.toString(), characteristicUuid.toString())
    fun gattDescriptor(uuid: UUID): UuidMeaning? = gattDescriptor(uuid.toString())
}
