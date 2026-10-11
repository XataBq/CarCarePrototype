package com.example.carcareprototype.bluetooth.uuids

/** 128-bit vendor UUIDs. These are NOT Bluetooth SIG standard 16-bit assigned numbers.
 * ANCS/AMS: Apple documentation. Continuity/Nearby: unofficial naming from observed BLE usage.
 */
internal object VendorUuidCatalog {
    private const val ANCS = "7905f431-b5ce-4e99-a40f-4b1e122d00d0"
    private const val AMS = "89d3502b-0f36-433a-8ef4-c502ad55f8dc"
    private const val CONTINUITY = "d0611e78-bbb4-4591-a5f8-487910ae4366"
    private const val NEARBY = "9fa480e0-4967-4542-9390-d343dc5d04ae"

    val services: Map<String, UuidMeaning> = mapOf(
        ANCS to UuidMeaning("Apple Notification Center (ANCS)", "Уведомления iPhone для совместимых аксессуаров", UuidKind.VENDOR_SERVICE),
        AMS to UuidMeaning("Apple Media Service (AMS)", "Управление медиа и информация о воспроизведении iPhone", UuidKind.VENDOR_SERVICE),
        CONTINUITY to UuidMeaning("Apple Continuity (неофициальное название)", "Предположительно межустройственное взаимодействие Apple; не публичный стандарт Bluetooth SIG", UuidKind.VENDOR_SERVICE),
        NEARBY to UuidMeaning("Apple Nearby (неофициальное название)", "Предположительно функции обнаружения близких устройств Apple; протокол закрыт", UuidKind.VENDOR_SERVICE)
    )

    val characteristics: Map<Pair<String, String>, UuidMeaning> = mapOf(
        (ANCS to "69d1d8f3-45e1-49a8-9821-9bbdfdaad9d9") to
            UuidMeaning("ANCS Control Point", "Команды получения подробностей уведомлений; запись требует авторизации", UuidKind.VENDOR_CHARACTERISTIC),
        (ANCS to "9fbf120d-6301-42d9-8c58-25e699a21dbd") to
            UuidMeaning("ANCS Notification Source", "Сообщает о появлении, изменении и удалении уведомлений", UuidKind.VENDOR_CHARACTERISTIC),
        (ANCS to "22eac6e9-24d6-4bb5-be44-b36ace7c7bfb") to
            UuidMeaning("ANCS Data Source", "Возвращает подробности уведомлений", UuidKind.VENDOR_CHARACTERISTIC),
        (AMS to "9b3c81d8-57b1-4a8a-b8df-0e56f7ca51c2") to
            UuidMeaning("AMS Remote Command", "Команды воспроизведения и сведения о поддерживаемых командах", UuidKind.VENDOR_CHARACTERISTIC),
        (AMS to "2f7cabce-808d-411f-9a0c-bb92ba96c102") to
            UuidMeaning("AMS Entity Update", "Уведомления о состоянии плеера, очереди и текущего трека", UuidKind.VENDOR_CHARACTERISTIC),
        (AMS to "c6b2f38c-23ab-46d8-a6ab-a3a870bbd5d7") to
            UuidMeaning("AMS Entity Attribute", "Получение отдельных атрибутов медиаплеера и трека", UuidKind.VENDOR_CHARACTERISTIC)
    )
}
