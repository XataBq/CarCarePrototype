package com.example.carcareprototype.car

internal class MileageStateScope {
    private var connectionToken: String? = null
    var carId: String? = null
        private set
    var state = MileageState()
        private set

    fun connect(token: String, selectedCarId: String?) {
        if (connectionToken == token) return
        connectionToken = token
        carId = selectedCarId
        state = MileageState()
    }

    fun selectCar(selectedCarId: String?) {
        carId = selectedCarId
        state = MileageState()
    }

    fun disconnect(token: String) {
        if (connectionToken != token) return
        connectionToken = null
        state = MileageState()
    }

    fun update(token: String?, revision: Long?, currentRevision: Long, value: MileageState): Boolean {
        if (token == null || token != connectionToken || revision != currentRevision) return false
        state = value
        return true
    }
}
