package com.example.carcareprototype.bluetooth.uuids

// Названия стандартных идентификаторов взяты из Bluetooth SIG Assigned Numbers (2026-05-13).
// Краткие пояснения составлены специально для диагностического UI.


internal object GattDescriptorCatalog {
    val entries: Map<Int, UuidMeaning> = mapOf(
        0x2900 to UuidMeaning("Characteristic Extended Properties", "Дополнительные свойства характеристики", UuidKind.GATT_DESCRIPTOR),
        0x2901 to UuidMeaning("Characteristic User Description", "Человекочитаемое описание характеристики", UuidKind.GATT_DESCRIPTOR),
        0x2902 to UuidMeaning("Client Characteristic Configuration (CCCD)", "Включает или выключает Notify и Indicate для клиента", UuidKind.GATT_DESCRIPTOR),
        0x2903 to UuidMeaning("Server Characteristic Configuration", "Конфигурация уведомлений со стороны сервера", UuidKind.GATT_DESCRIPTOR),
        0x2904 to UuidMeaning("Characteristic Presentation Format", "Формат, единицы измерения и представление данных", UuidKind.GATT_DESCRIPTOR),
        0x2905 to UuidMeaning("Characteristic Aggregate Format", "Групповое описание форматов значений", UuidKind.GATT_DESCRIPTOR),
        0x2906 to UuidMeaning("Valid Range", "Допустимый диапазон значений характеристики", UuidKind.GATT_DESCRIPTOR),
        0x2907 to UuidMeaning("External Report Reference", "Ссылка на внешний HID-отчёт", UuidKind.GATT_DESCRIPTOR),
        0x2908 to UuidMeaning("Report Reference", "Ссылка на HID-отчёт", UuidKind.GATT_DESCRIPTOR),
        0x2909 to UuidMeaning("Number of Digitals", "Количество цифровых входов или выходов", UuidKind.GATT_DESCRIPTOR),
        0x290A to UuidMeaning("Value Trigger Setting", "Условие отправки изменения значения", UuidKind.GATT_DESCRIPTOR),
        0x290B to UuidMeaning("Environmental Sensing Configuration", "Настройки датчика окружающей среды", UuidKind.GATT_DESCRIPTOR),
        0x290C to UuidMeaning("Environmental Sensing Measurement", "Описание измерения датчика среды", UuidKind.GATT_DESCRIPTOR),
        0x290D to UuidMeaning("Environmental Sensing Trigger Setting", "Порог уведомления датчика среды", UuidKind.GATT_DESCRIPTOR),
        0x290E to UuidMeaning("Time Trigger Setting", "Временной триггер уведомлений", UuidKind.GATT_DESCRIPTOR),
        0x290F to UuidMeaning("Complete BR-EDR Transport Block Data", "Сведения о BR/EDR-транспорте", UuidKind.GATT_DESCRIPTOR),
        0x2910 to UuidMeaning("Observation Schedule", "Расписание наблюдений", UuidKind.GATT_DESCRIPTOR),
        0x2911 to UuidMeaning("Valid Range and Accuracy", "Диапазон и точность измерений", UuidKind.GATT_DESCRIPTOR),
        0x2912 to UuidMeaning("Measurement Description", "Пояснение к измерению", UuidKind.GATT_DESCRIPTOR),
        0x2913 to UuidMeaning("Manufacturer Limits", "Ограничения производителя", UuidKind.GATT_DESCRIPTOR),
        0x2914 to UuidMeaning("Process Tolerances", "Допуски процесса", UuidKind.GATT_DESCRIPTOR),
        0x2915 to UuidMeaning("IMD Trigger Setting", "Триггер промышленного измерительного устройства", UuidKind.GATT_DESCRIPTOR),
        0x2916 to UuidMeaning("Cooking Sensor Info", "Информация о кухонном датчике", UuidKind.GATT_DESCRIPTOR),
        0x2917 to UuidMeaning("Cooking Trigger Setting", "Триггер кухонного датчика", UuidKind.GATT_DESCRIPTOR)
    )
}
