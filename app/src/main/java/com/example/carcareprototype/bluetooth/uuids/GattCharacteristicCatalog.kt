package com.example.carcareprototype.bluetooth.uuids

// Названия стандартных идентификаторов взяты из Bluetooth SIG Assigned Numbers (2026-05-13).
// Краткие пояснения составлены специально для диагностического UI.


internal object GattCharacteristicCatalog {
    val entries: Map<Int, com.example.carcareprototype.bluetooth.uuids.UuidMeaning> = mapOf(
        0x2A00 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Device Name",
            "Имя, которое устройство сообщает клиентам GATT",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A01 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Appearance",
            "Стандартный тип или внешний вид устройства",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A02 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Peripheral Privacy Flag",
            "Признак поддержки конфиденциальности адреса",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A03 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Reconnection Address",
            "Адрес для повторного подключения",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A04 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Preferred Connection Parameters",
            "Предпочтительные интервалы и параметры BLE-соединения",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A05 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Service Changed",
            "Уведомление об изменении структуры GATT",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A06 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Alert Level",
            "Уровень сигнала тревоги",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A07 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Tx Power Level",
            "Мощность передатчика",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A08 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Date Time",
            "Дата и время",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A09 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Day of Week",
            "День недели",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A0A to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Day Date Time",
            "Дата, время и день недели",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A0D to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "DST Offset",
            "Смещение летнего времени",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A0E to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Time Zone",
            "Часовой пояс",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A0F to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Local Time Information",
            "Местное время и часовой пояс",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A16 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Time Update Control Point",
            "Запрос обновления времени",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A17 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Time Update State",
            "Состояние обновления времени",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A18 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Glucose Measurement",
            "Измерение уровня глюкозы",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A19 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Battery Level",
            "Заряд батареи, как правило в процентах",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A1C to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Temperature Measurement",
            "Показание медицинского термометра",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A1D to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Temperature Type",
            "Место или тип измерения температуры",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A1E to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Intermediate Temperature",
            "Промежуточные измерения температуры",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A21 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Measurement Interval",
            "Период между измерениями",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A22 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Boot Keyboard Input Report",
            "Ввод клавиатуры в режиме Boot HID",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A23 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "System ID",
            "Идентификатор системы устройства",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A24 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Model Number",
            "Модель устройства",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A25 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Serial Number",
            "Серийный номер устройства",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A26 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Firmware Revision",
            "Версия прошивки устройства",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A27 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Hardware Revision",
            "Ревизия аппаратной части",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A28 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Software Revision",
            "Версия программного обеспечения",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A29 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Manufacturer Name",
            "Название производителя устройства",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A2B to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Current Time",
            "Текущее время",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A31 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Scan Refresh",
            "Сигнал необходимости обновления сканирования",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A32 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Boot Keyboard Output Report",
            "Выходные данные клавиатуры Boot HID",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A33 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Boot Mouse Input Report",
            "Ввод мыши в режиме Boot HID",
            UuidKind.GATT_CHARACTERISTIC
        ),
        0x2A34 to UuidMeaning("Glucose Measurement Context", "Дополнительные сведения об измерении глюкозы", UuidKind.GATT_CHARACTERISTIC),
        0x2A35 to UuidMeaning("Blood Pressure Measurement", "Результат измерения давления", UuidKind.GATT_CHARACTERISTIC),
        0x2A36 to UuidMeaning("Intermediate Cuff Pressure", "Промежуточное давление манжеты", UuidKind.GATT_CHARACTERISTIC),
        0x2A37 to UuidMeaning("Heart Rate Measurement", "Текущее измерение пульса", UuidKind.GATT_CHARACTERISTIC),
        0x2A38 to UuidMeaning("Body Sensor Location", "Расположение датчика пульса", UuidKind.GATT_CHARACTERISTIC),
        0x2A39 to UuidMeaning("Heart Rate Control Point", "Управление датчиком пульса", UuidKind.GATT_CHARACTERISTIC),
        0x2A4A to UuidMeaning("HID Information", "Версия и параметры устройства ввода", UuidKind.GATT_CHARACTERISTIC),
        0x2A4B to UuidMeaning("Report Map", "Описание формата HID-отчётов", UuidKind.GATT_CHARACTERISTIC),
        0x2A4C to UuidMeaning("HID Control Point", "Команды управления HID", UuidKind.GATT_CHARACTERISTIC),
        0x2A4D to UuidMeaning("HID Report", "Отчёт устройства ввода", UuidKind.GATT_CHARACTERISTIC),
        0x2A4E to UuidMeaning("Protocol Mode", "Режим работы HID", UuidKind.GATT_CHARACTERISTIC),
        0x2A4F to UuidMeaning("Scan Interval Window", "Временные параметры сканирования", UuidKind.GATT_CHARACTERISTIC),
        0x2A50 to UuidMeaning("PnP ID", "Идентификаторы производителя и продукта", UuidKind.GATT_CHARACTERISTIC),
        0x2A53 to UuidMeaning("Running Speed/Cadence Measurement", "Скорость и шаговый темп во время бега", UuidKind.GATT_CHARACTERISTIC),
        0x2A54 to UuidMeaning("Running Speed/Cadence Feature", "Возможности бегового датчика", UuidKind.GATT_CHARACTERISTIC),
        0x2A55 to UuidMeaning("Speed/Cadence Control Point", "Управление датчиком скорости и каденса", UuidKind.GATT_CHARACTERISTIC),
        0x2A5B to UuidMeaning("Cycling Speed/Cadence Measurement", "Скорость колеса и каденс велосипеда, не пробег автомобиля", UuidKind.GATT_CHARACTERISTIC),
        0x2A5C to UuidMeaning("Cycling Speed/Cadence Feature", "Функции велосипедного датчика", UuidKind.GATT_CHARACTERISTIC),
        0x2A5D to UuidMeaning("Sensor Location", "Место установки датчика", UuidKind.GATT_CHARACTERISTIC),
        0x2A63 to UuidMeaning("Cycling Power Measurement", "Измерение мощности велодатчика", UuidKind.GATT_CHARACTERISTIC),
        0x2A64 to UuidMeaning("Cycling Power Vector", "Вектор мощности велосипедного датчика", UuidKind.GATT_CHARACTERISTIC),
        0x2A65 to UuidMeaning("Cycling Power Feature", "Возможности датчика велосипедной мощности", UuidKind.GATT_CHARACTERISTIC),
        0x2A66 to UuidMeaning("Cycling Power Control Point", "Управление датчиком велосипедной мощности", UuidKind.GATT_CHARACTERISTIC),
        0x2A67 to UuidMeaning("Location and Speed", "Координаты и скорость навигационного датчика", UuidKind.GATT_CHARACTERISTIC),
        0x2A68 to UuidMeaning("Navigation", "Направление движения и навигация", UuidKind.GATT_CHARACTERISTIC),
        0x2A69 to UuidMeaning("Position Quality", "Показатели точности определения координат", UuidKind.GATT_CHARACTERISTIC),
        0x2A6A to UuidMeaning("Location and Navigation Feature", "Возможности навигационного датчика", UuidKind.GATT_CHARACTERISTIC),
        0x2A6B to UuidMeaning("Location and Navigation Control Point", "Команды навигационного датчика", UuidKind.GATT_CHARACTERISTIC),
        0x2A6C to UuidMeaning("Elevation", "Высота над уровнем моря", UuidKind.GATT_CHARACTERISTIC),
        0x2A6D to UuidMeaning("Pressure", "Давление окружающей среды", UuidKind.GATT_CHARACTERISTIC),
        0x2A6E to UuidMeaning("Temperature", "Температура окружающей среды", UuidKind.GATT_CHARACTERISTIC),
        0x2A6F to UuidMeaning("Humidity", "Относительная влажность", UuidKind.GATT_CHARACTERISTIC),
        0x2A70 to UuidMeaning("True Wind Speed", "Скорость ветра", UuidKind.GATT_CHARACTERISTIC),
        0x2A71 to UuidMeaning("True Wind Direction", "Направление ветра", UuidKind.GATT_CHARACTERISTIC),
        0x2A72 to UuidMeaning("Apparent Wind Speed", "Кажущаяся скорость ветра", UuidKind.GATT_CHARACTERISTIC),
        0x2A73 to UuidMeaning("Apparent Wind Direction", "Кажущееся направление ветра", UuidKind.GATT_CHARACTERISTIC),
        0x2A76 to UuidMeaning("UV Index", "Ультрафиолетовый индекс", UuidKind.GATT_CHARACTERISTIC),
        0x2A78 to UuidMeaning("Rainfall", "Количество осадков", UuidKind.GATT_CHARACTERISTIC),
        0x2A7B to UuidMeaning("Dew Point", "Температура точки росы", UuidKind.GATT_CHARACTERISTIC),
        0x2A85 to UuidMeaning("Date of Birth", "Дата рождения пользователя", UuidKind.GATT_CHARACTERISTIC),
        0x2A8A to UuidMeaning("First Name", "Имя пользователя", UuidKind.GATT_CHARACTERISTIC),
        0x2A8E to UuidMeaning("Height", "Рост пользователя", UuidKind.GATT_CHARACTERISTIC),
        0x2A90 to UuidMeaning("Last Name", "Фамилия пользователя", UuidKind.GATT_CHARACTERISTIC),
        0x2A96 to UuidMeaning("VO2 Max", "Максимальное потребление кислорода", UuidKind.GATT_CHARACTERISTIC),
        0x2A98 to UuidMeaning("Weight", "Вес пользователя", UuidKind.GATT_CHARACTERISTIC),
        0x2A99 to UuidMeaning("Database Change Increment", "Счётчик изменения пользовательских данных", UuidKind.GATT_CHARACTERISTIC),
        0x2A9A to UuidMeaning("User Index", "Идентификатор пользователя устройства", UuidKind.GATT_CHARACTERISTIC),
        0x2A9C to UuidMeaning("Body Composition Measurement", "Данные состава тела", UuidKind.GATT_CHARACTERISTIC),
        0x2A9D to UuidMeaning("Weight Measurement", "Результат взвешивания", UuidKind.GATT_CHARACTERISTIC),
        0x2A9E to UuidMeaning("Weight Scale Feature", "Возможности весов", UuidKind.GATT_CHARACTERISTIC),
        0x2A9F to UuidMeaning("User Control Point", "Управление пользователями устройства", UuidKind.GATT_CHARACTERISTIC),
        0x2AA2 to UuidMeaning("Language", "Код языка", UuidKind.GATT_CHARACTERISTIC),
        0x2AAE to UuidMeaning("Latitude", "Географическая широта", UuidKind.GATT_CHARACTERISTIC),
        0x2AAF to UuidMeaning("Longitude", "Географическая долгота", UuidKind.GATT_CHARACTERISTIC),
        0x2AB2 to UuidMeaning("Floor Number", "Номер этажа", UuidKind.GATT_CHARACTERISTIC),
        0x2AB3 to UuidMeaning("Altitude", "Высота", UuidKind.GATT_CHARACTERISTIC),
        0x2AB5 to UuidMeaning("Location Name", "Название местоположения", UuidKind.GATT_CHARACTERISTIC),
        0x2AB6 to UuidMeaning("URI", "Ссылка на ресурс", UuidKind.GATT_CHARACTERISTIC),
        0x2AB7 to UuidMeaning("HTTP Headers", "Заголовки HTTP", UuidKind.GATT_CHARACTERISTIC),
        0x2AB8 to UuidMeaning("HTTP Status Code", "Код ответа HTTP", UuidKind.GATT_CHARACTERISTIC),
        0x2AB9 to UuidMeaning("HTTP Entity Body", "Тело HTTP-ответа", UuidKind.GATT_CHARACTERISTIC),
        0x2ABA to UuidMeaning("HTTP Control Point", "Команды HTTP-прокси", UuidKind.GATT_CHARACTERISTIC),
        0x2ABB to UuidMeaning("HTTPS Security", "Параметры защиты HTTPS", UuidKind.GATT_CHARACTERISTIC),
        0x2ABC to UuidMeaning("TDS Control Point", "Управление обнаружением транспорта", UuidKind.GATT_CHARACTERISTIC),
        0x2ABD to UuidMeaning("Object Transfer Feature", "Возможности передачи объектов", UuidKind.GATT_CHARACTERISTIC),
        0x2ABE to UuidMeaning("Object Name", "Имя передаваемого объекта", UuidKind.GATT_CHARACTERISTIC),
        0x2ABF to UuidMeaning("Object Type", "Тип передаваемого объекта", UuidKind.GATT_CHARACTERISTIC),
        0x2AC0 to UuidMeaning("Object Size", "Размер объекта", UuidKind.GATT_CHARACTERISTIC),
        0x2AC3 to UuidMeaning("Object ID", "Идентификатор объекта", UuidKind.GATT_CHARACTERISTIC),
        0x2AC4 to UuidMeaning("Object Properties", "Свойства передаваемого объекта", UuidKind.GATT_CHARACTERISTIC),
        0x2AC5 to UuidMeaning("Object Action Control Point", "Команды над объектом", UuidKind.GATT_CHARACTERISTIC),
        0x2AC6 to UuidMeaning("Object List Control Point", "Команды списка объектов", UuidKind.GATT_CHARACTERISTIC),
        0x2AC8 to UuidMeaning("Object Changed", "Уведомление об изменении объекта", UuidKind.GATT_CHARACTERISTIC),
        0x2ACC to UuidMeaning("Fitness Machine Feature", "Возможности тренажёра", UuidKind.GATT_CHARACTERISTIC),
        0x2ACD to UuidMeaning("Treadmill Data", "Данные беговой дорожки", UuidKind.GATT_CHARACTERISTIC),
        0x2ACE to UuidMeaning("Cross Trainer Data", "Данные эллиптического тренажёра", UuidKind.GATT_CHARACTERISTIC),
        0x2AD1 to UuidMeaning("Rower Data", "Данные гребного тренажёра", UuidKind.GATT_CHARACTERISTIC),
        0x2AD2 to UuidMeaning("Indoor Bike Data", "Данные велотренажёра", UuidKind.GATT_CHARACTERISTIC),
        0x2AD3 to UuidMeaning("Training Status", "Состояние тренировки", UuidKind.GATT_CHARACTERISTIC),
        0x2AD4 to UuidMeaning("Supported Speed Range", "Допустимый диапазон скорости тренажёра", UuidKind.GATT_CHARACTERISTIC),
        0x2AD9 to UuidMeaning("Fitness Machine Control Point", "Управление тренажёром", UuidKind.GATT_CHARACTERISTIC),
        0x2ADA to UuidMeaning("Fitness Machine Status", "Состояние тренажёра", UuidKind.GATT_CHARACTERISTIC),
        0x2ADB to UuidMeaning("Mesh Provisioning Data In", "Данные в узел настройки Mesh", UuidKind.GATT_CHARACTERISTIC),
        0x2ADC to UuidMeaning("Mesh Provisioning Data Out", "Данные от узла настройки Mesh", UuidKind.GATT_CHARACTERISTIC),
        0x2ADD to UuidMeaning("Mesh Proxy Data In", "Передача данных в Mesh Proxy", UuidKind.GATT_CHARACTERISTIC),
        0x2ADE to UuidMeaning("Mesh Proxy Data Out", "Передача данных из Mesh Proxy", UuidKind.GATT_CHARACTERISTIC),
        0x2AE0 to UuidMeaning("Average Current", "Средняя сила тока", UuidKind.GATT_CHARACTERISTIC),
        0x2AE1 to UuidMeaning("Average Voltage", "Среднее напряжение", UuidKind.GATT_CHARACTERISTIC),
        0x2AEE to UuidMeaning("Electric Current", "Сила электрического тока", UuidKind.GATT_CHARACTERISTIC),
        0x2AF2 to UuidMeaning("Energy", "Энергия", UuidKind.GATT_CHARACTERISTIC),
        0x2AF9 to UuidMeaning("Generic Level", "Универсальный числовой уровень", UuidKind.GATT_CHARACTERISTIC),
        0x2AFB to UuidMeaning("Illuminance", "Освещённость", UuidKind.GATT_CHARACTERISTIC),
        0x2B05 to UuidMeaning("Power", "Электрическая мощность", UuidKind.GATT_CHARACTERISTIC),
        0x2B18 to UuidMeaning("Voltage", "Электрическое напряжение", UuidKind.GATT_CHARACTERISTIC),
        0x2B29 to UuidMeaning("Client Supported Features", "Функции, поддерживаемые GATT-клиентом", UuidKind.GATT_CHARACTERISTIC),
        0x2B2A to UuidMeaning("Database Hash", "Контрольная сумма структуры GATT-базы", UuidKind.GATT_CHARACTERISTIC),
        0x2B7D to UuidMeaning("Volume State", "Текущее состояние громкости", UuidKind.GATT_CHARACTERISTIC),
        0x2B7E to UuidMeaning("Volume Control Point", "Управление громкостью", UuidKind.GATT_CHARACTERISTIC),
        0x2B7F to UuidMeaning("Volume Flags", "Флаги настройки громкости", UuidKind.GATT_CHARACTERISTIC),
        0x2B80 to UuidMeaning("Volume Offset State", "Смещение уровня громкости", UuidKind.GATT_CHARACTERISTIC),
        0x2B82 to UuidMeaning("Volume Offset Control Point", "Управление смещением громкости", UuidKind.GATT_CHARACTERISTIC)
    )
}
