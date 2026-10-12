package com.example.carcareprototype.bluetooth

internal fun BluetoothLinkState.reduce(event: BluetoothLinkEvent): BluetoothLinkState = when (event) {
    is BluetoothLinkEvent.Phase -> when {
        event.phase == LinkPhase.CONNECTING -> BluetoothLinkState(
            mode = event.mode, phase = event.phase, address = event.address
        )
        event.mode != mode || event.address != address -> this
        !isBusy -> this
        event.phase == LinkPhase.IDLE -> BluetoothLinkState(
            mode = mode, phase = LinkPhase.IDLE, address = address
        )
        else -> copy(phase = event.phase, error = null)
    }
    is BluetoothLinkEvent.Failure -> if (isBusy && (event.mode != mode || event.address != address)) {
        this
    } else {
        BluetoothLinkState(
            mode = event.mode, phase = LinkPhase.FAILED, address = event.address, error = event.reason
        )
    }
    is BluetoothLinkEvent.GattServices -> if (
        mode == LinkMode.GATT && address == event.address &&
        (phase == LinkPhase.DISCOVERING || phase == LinkPhase.READY)
    ) {
        copy(services = event.services)
    } else this
    is BluetoothLinkEvent.IncomingBytes -> this
}
