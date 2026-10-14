package com.example.carcareprototype.bluetooth.uuids

internal object ClassicServiceCatalog {
    val entries: Map<Int, UuidMeaning> = mapOf(
        0x1000 to UuidMeaning(
            "SDP Server",
            "Служба обнаружения сервисов Bluetooth Classic",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1001 to UuidMeaning(
            "Browse Group Descriptor",
            "Группировка сервисов для просмотра через SDP",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1101 to UuidMeaning(
            "Serial Port (SPP)",
            "Последовательный обмен байтами через RFCOMM",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1102 to UuidMeaning(
            "LAN Access Using PPP",
            "Сетевое подключение через PPP; устаревший профиль",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1103 to UuidMeaning(
            "Dial-up Networking (DUN)",
            "Использование модема телефона для передачи данных",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1104 to UuidMeaning(
            "IrMC Sync",
            "Синхронизация контактов и календаря по IrMC",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1105 to UuidMeaning(
            "OBEX Object Push (OPP)",
            "Передача отдельных объектов, например контактов и файлов",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1106 to UuidMeaning(
            "OBEX File Transfer (FTP)",
            "Просмотр и передача файлов через OBEX",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1107 to UuidMeaning(
            "IrMC Sync Command",
            "Командный канал синхронизации IrMC",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1108 to UuidMeaning(
            "Headset (HSP)",
            "Гарнитура и базовые голосовые вызовы",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1109 to UuidMeaning(
            "Cordless Telephony",
            "Беспроводная телефонная связь",
            UuidKind.CLASSIC_SERVICE
        ),
        0x110A to UuidMeaning(
            "Audio Source (A2DP)",
            "Источник аудиопотока: например, телефон",
            UuidKind.CLASSIC_SERVICE
        ),
        0x110B to UuidMeaning(
            "Audio Sink (A2DP)",
            "Приёмник аудиопотока: например, автомагнитола",
            UuidKind.CLASSIC_SERVICE
        ),
        0x110C to UuidMeaning(
            "AVRCP Target",
            "Устройство, принимающее команды управления медиа",
            UuidKind.CLASSIC_SERVICE
        ),
        0x110D to UuidMeaning(
            "Advanced Audio Distribution (A2DP)",
            "Профиль стереозвука Bluetooth Classic",
            UuidKind.CLASSIC_SERVICE
        ),
        0x110E to UuidMeaning(
            "AVRCP",
            "Профиль управления воспроизведением медиа",
            UuidKind.CLASSIC_SERVICE
        ),
        0x110F to UuidMeaning(
            "AVRCP Controller",
            "Устройство, посылающее команды управления медиа",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1110 to UuidMeaning(
            "Intercom",
            "Голосовая связь между устройствами",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1111 to UuidMeaning(
            "Fax",
            "Обмен факсимильными данными",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1112 to UuidMeaning(
            "Headset Audio Gateway",
            "Шлюз голосовой гарнитуры, обычно телефон",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1113 to UuidMeaning(
            "WAP",
            "Доступ к сетевым WAP-сервисам; устаревший профиль",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1114 to UuidMeaning(
            "WAP Client",
            "Клиент WAP; устаревший профиль",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1115 to UuidMeaning(
            "PAN User (PANU)",
            "Клиент персональной сети по Bluetooth",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1116 to UuidMeaning(
            "Network Access Point (NAP)",
            "Точка доступа к сети через Bluetooth PAN",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1117 to UuidMeaning(
            "Group Network (GN)",
            "Групповая персональная сеть Bluetooth PAN",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1118 to UuidMeaning(
            "Direct Printing",
            "Прямая печать на Bluetooth-принтер",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1119 to UuidMeaning(
            "Reference Printing",
            "Печать со ссылками на данные",
            UuidKind.CLASSIC_SERVICE
        ),
        0x111A to UuidMeaning(
            "Basic Imaging Profile (BIP)",
            "Обмен изображениями",
            UuidKind.CLASSIC_SERVICE
        ),
        0x111B to UuidMeaning(
            "Imaging Responder",
            "Устройство, принимающее запросы обмена изображениями",
            UuidKind.CLASSIC_SERVICE
        ),
        0x111C to UuidMeaning(
            "Imaging Automatic Archive",
            "Автоматическое архивирование изображений",
            UuidKind.CLASSIC_SERVICE
        ),
        0x111D to UuidMeaning(
            "Imaging Referenced Objects",
            "Получение изображений по ссылкам",
            UuidKind.CLASSIC_SERVICE
        ),
        0x111E to UuidMeaning(
            "Hands-Free (HFP)",
            "Громкая связь, звонки через автомобиль или гарнитуру",
            UuidKind.CLASSIC_SERVICE
        ),
        0x111F to UuidMeaning(
            "Hands-Free Audio Gateway",
            "Телефонная сторона профиля громкой связи HFP",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1120 to UuidMeaning(
            "Direct Printing Referenced Objects",
            "Печать объектов по ссылке",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1121 to UuidMeaning(
            "Reflected UI",
            "Удалённое отображение пользовательского интерфейса",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1122 to UuidMeaning(
            "Basic Printing",
            "Базовый профиль печати",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1123 to UuidMeaning(
            "Printing Status",
            "Сведения о состоянии печати",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1124 to UuidMeaning(
            "Human Interface Device (HID)",
            "Клавиатуры, мыши, игровые контроллеры",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1125 to UuidMeaning(
            "Hardcopy Cable Replacement",
            "Замена проводного интерфейса принтера или сканера",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1126 to UuidMeaning(
            "HCR Print",
            "Печать по профилю замены кабеля",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1127 to UuidMeaning(
            "HCR Scan",
            "Сканирование по профилю замены кабеля",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1128 to UuidMeaning(
            "Common ISDN Access",
            "Доступ к цифровой телефонной сети ISDN",
            UuidKind.CLASSIC_SERVICE
        ),
        0x112D to UuidMeaning(
            "SIM Access (SAP)",
            "Удалённый доступ к SIM-карте телефона",
            UuidKind.CLASSIC_SERVICE
        ),
        0x112E to UuidMeaning(
            "Phonebook Access Client (PBAP)",
            "Клиент загрузки телефонной книги",
            UuidKind.CLASSIC_SERVICE
        ),
        0x112F to UuidMeaning(
            "Phonebook Access Server (PBAP)",
            "Источник телефонной книги",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1130 to UuidMeaning(
            "Phonebook Access (PBAP)",
            "Профиль передачи телефонной книги",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1131 to UuidMeaning(
            "Headset HS",
            "Роль гарнитуры в Headset Profile",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1132 to UuidMeaning(
            "Message Access Server (MAP)",
            "Источник сообщений и SMS",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1133 to UuidMeaning(
            "Message Notification Server (MAP)",
            "Уведомления об изменениях сообщений",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1134 to UuidMeaning(
            "Message Access Profile (MAP)",
            "Чтение сообщений и уведомлений, часто в авто",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1135 to UuidMeaning(
            "GNSS Profile",
            "Передача навигационных спутниковых данных",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1136 to UuidMeaning(
            "GNSS Server",
            "Сервер навигационных спутниковых данных",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1137 to UuidMeaning(
            "3D Display",
            "Устройство 3D-отображения",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1138 to UuidMeaning(
            "3D Glasses",
            "Очки для 3D-отображения",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1139 to UuidMeaning(
            "3D Synchronization",
            "Синхронизация 3D-устройств",
            UuidKind.CLASSIC_SERVICE
        ),
        0x113A to UuidMeaning(
            "Multi Profile Specification (MPS)",
            "Согласованная работа нескольких Bluetooth-профилей",
            UuidKind.CLASSIC_SERVICE
        ),
        0x113B to UuidMeaning(
            "MPS Service",
            "Служба координации нескольких профилей",
            UuidKind.CLASSIC_SERVICE
        ),
        0x113C to UuidMeaning(
            "CTN Access Service",
            "Доступ к календарю, задачам и заметкам",
            UuidKind.CLASSIC_SERVICE
        ),
        0x113D to UuidMeaning(
            "CTN Notification Service",
            "Уведомления календаря, задач и заметок",
            UuidKind.CLASSIC_SERVICE
        ),
        0x113E to UuidMeaning(
            "Calendar Tasks and Notes (CTN)",
            "Профиль обмена календарём, задачами и заметками",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1200 to UuidMeaning(
            "PnP Information",
            "Идентификация производителя, продукта и версии",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1201 to UuidMeaning(
            "Generic Networking",
            "Обобщённый класс сетевых сервисов",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1202 to UuidMeaning(
            "Generic File Transfer",
            "Обобщённый класс передачи файлов",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1203 to UuidMeaning(
            "Generic Audio",
            "Обобщённый класс аудиоустройств",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1204 to UuidMeaning(
            "Generic Telephony",
            "Обобщённый класс телефонных сервисов",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1205 to UuidMeaning(
            "UPnP Service",
            "Обнаружение UPnP-сервисов",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1206 to UuidMeaning(
            "UPnP IP Service",
            "Сетевые UPnP-сервисы поверх IP",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1303 to UuidMeaning(
            "Video Source",
            "Источник видеопотока",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1304 to UuidMeaning(
            "Video Sink",
            "Получатель видеопотока",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1305 to UuidMeaning(
            "Video Distribution Profile",
            "Передача видео между устройствами",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1400 to UuidMeaning(
            "Health Device Profile (HDP)",
            "Обмен данными медицинских приборов",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1401 to UuidMeaning(
            "HDP Source",
            "Источник медицинских измерений",
            UuidKind.CLASSIC_SERVICE
        ),
        0x1402 to UuidMeaning(
            "HDP Sink",
            "Получатель медицинских измерений",
            UuidKind.CLASSIC_SERVICE
        )
    )
}
