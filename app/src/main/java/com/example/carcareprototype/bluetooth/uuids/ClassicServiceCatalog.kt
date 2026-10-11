package com.example.carcareprototype.bluetooth.uuids

// Названия стандартных идентификаторов взяты из Bluetooth SIG Assigned Numbers (2026-05-13).
// Краткие пояснения составлены специально для диагностического UI.


internal object ClassicServiceCatalog {
    val entries: Map<Int, com.example.carcareprototype.bluetooth.uuids.UuidMeaning> = mapOf(
        0x1000 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "SDP Server",
            "Служба обнаружения сервисов Bluetooth Classic",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1001 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Browse Group Descriptor",
            "Группировка сервисов для просмотра через SDP",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1101 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Serial Port (SPP)",
            "Последовательный обмен байтами через RFCOMM",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1102 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "LAN Access Using PPP",
            "Сетевое подключение через PPP; устаревший профиль",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1103 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Dial-up Networking (DUN)",
            "Использование модема телефона для передачи данных",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1104 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "IrMC Sync",
            "Синхронизация контактов и календаря по IrMC",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1105 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "OBEX Object Push (OPP)",
            "Передача отдельных объектов, например контактов и файлов",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1106 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "OBEX File Transfer (FTP)",
            "Просмотр и передача файлов через OBEX",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1107 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "IrMC Sync Command",
            "Командный канал синхронизации IrMC",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1108 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Headset (HSP)",
            "Гарнитура и базовые голосовые вызовы",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1109 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Cordless Telephony",
            "Беспроводная телефонная связь",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x110A to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Audio Source (A2DP)",
            "Источник аудиопотока: например, телефон",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x110B to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Audio Sink (A2DP)",
            "Приёмник аудиопотока: например, автомагнитола",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x110C to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "AVRCP Target",
            "Устройство, принимающее команды управления медиа",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x110D to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Advanced Audio Distribution (A2DP)",
            "Профиль стереозвука Bluetooth Classic",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x110E to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "AVRCP",
            "Профиль управления воспроизведением медиа",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x110F to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "AVRCP Controller",
            "Устройство, посылающее команды управления медиа",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1110 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Intercom",
            "Голосовая связь между устройствами",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1111 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Fax",
            "Обмен факсимильными данными",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1112 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Headset Audio Gateway",
            "Шлюз голосовой гарнитуры, обычно телефон",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1113 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "WAP",
            "Доступ к сетевым WAP-сервисам; устаревший профиль",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1114 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "WAP Client",
            "Клиент WAP; устаревший профиль",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1115 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "PAN User (PANU)",
            "Клиент персональной сети по Bluetooth",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1116 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Network Access Point (NAP)",
            "Точка доступа к сети через Bluetooth PAN",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1117 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Group Network (GN)",
            "Групповая персональная сеть Bluetooth PAN",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1118 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Direct Printing",
            "Прямая печать на Bluetooth-принтер",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1119 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Reference Printing",
            "Печать со ссылками на данные",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x111A to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Basic Imaging Profile (BIP)",
            "Обмен изображениями",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x111B to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Imaging Responder",
            "Устройство, принимающее запросы обмена изображениями",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x111C to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Imaging Automatic Archive",
            "Автоматическое архивирование изображений",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x111D to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Imaging Referenced Objects",
            "Получение изображений по ссылкам",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x111E to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Hands-Free (HFP)",
            "Громкая связь, звонки через автомобиль или гарнитуру",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x111F to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Hands-Free Audio Gateway",
            "Телефонная сторона профиля громкой связи HFP",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1120 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Direct Printing Referenced Objects",
            "Печать объектов по ссылке",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1121 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Reflected UI",
            "Удалённое отображение пользовательского интерфейса",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1122 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Basic Printing",
            "Базовый профиль печати",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1123 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Printing Status",
            "Сведения о состоянии печати",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1124 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Human Interface Device (HID)",
            "Клавиатуры, мыши, игровые контроллеры",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1125 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Hardcopy Cable Replacement",
            "Замена проводного интерфейса принтера или сканера",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1126 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "HCR Print",
            "Печать по профилю замены кабеля",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1127 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "HCR Scan",
            "Сканирование по профилю замены кабеля",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1128 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Common ISDN Access",
            "Доступ к цифровой телефонной сети ISDN",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x112D to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "SIM Access (SAP)",
            "Удалённый доступ к SIM-карте телефона",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x112E to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Phonebook Access Client (PBAP)",
            "Клиент загрузки телефонной книги",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x112F to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Phonebook Access Server (PBAP)",
            "Источник телефонной книги",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1130 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Phonebook Access (PBAP)",
            "Профиль передачи телефонной книги",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1131 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Headset HS",
            "Роль гарнитуры в Headset Profile",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1132 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Message Access Server (MAP)",
            "Источник сообщений и SMS",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1133 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Message Notification Server (MAP)",
            "Уведомления об изменениях сообщений",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1134 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Message Access Profile (MAP)",
            "Чтение сообщений и уведомлений, часто в авто",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1135 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "GNSS Profile",
            "Передача навигационных спутниковых данных",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1136 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "GNSS Server",
            "Сервер навигационных спутниковых данных",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1137 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "3D Display",
            "Устройство 3D-отображения",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1138 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "3D Glasses",
            "Очки для 3D-отображения",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1139 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "3D Synchronization",
            "Синхронизация 3D-устройств",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x113A to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Multi Profile Specification (MPS)",
            "Согласованная работа нескольких Bluetooth-профилей",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x113B to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "MPS Service",
            "Служба координации нескольких профилей",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x113C to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "CTN Access Service",
            "Доступ к календарю, задачам и заметкам",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x113D to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "CTN Notification Service",
            "Уведомления календаря, задач и заметок",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x113E to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Calendar Tasks and Notes (CTN)",
            "Профиль обмена календарём, задачами и заметками",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1200 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "PnP Information",
            "Идентификация производителя, продукта и версии",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1201 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Generic Networking",
            "Обобщённый класс сетевых сервисов",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1202 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Generic File Transfer",
            "Обобщённый класс передачи файлов",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1203 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Generic Audio",
            "Обобщённый класс аудиоустройств",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1204 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Generic Telephony",
            "Обобщённый класс телефонных сервисов",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1205 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "UPnP Service",
            "Обнаружение UPnP-сервисов",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1206 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "UPnP IP Service",
            "Сетевые UPnP-сервисы поверх IP",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1303 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Video Source",
            "Источник видеопотока",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1304 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Video Sink",
            "Получатель видеопотока",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1305 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Video Distribution Profile",
            "Передача видео между устройствами",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1400 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "Health Device Profile (HDP)",
            "Обмен данными медицинских приборов",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1401 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "HDP Source",
            "Источник медицинских измерений",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        ),
        0x1402 to _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidMeaning(
            "HDP Sink",
            "Получатель медицинских измерений",
            _root_ide_package_.com.example.carcareprototype.bluetooth.uuids.UuidKind.CLASSIC_SERVICE
        )
    )
}
