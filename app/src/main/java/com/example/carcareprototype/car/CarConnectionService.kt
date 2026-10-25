package com.example.carcareprototype.car

import android.content.pm.ApplicationInfo
import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator
import com.example.carcareprototype.diagnostics.DiagnosticRepository

class CarConnectionService : CarAppService() {
    override fun onCreate() {
        super.onCreate()
        DiagnosticRepository.log("CarAppService created")
    }

    override fun createHostValidator(): HostValidator {
        val isDebug = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

        return if (isDebug) {
            HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
        } else {
            HostValidator.Builder(this)
                .addAllowedHosts(
                    androidx.car.app.R.array.hosts_allowlist_sample
                )
                .build()
        }
    }

    override fun onCreateSession(): Session {
        DiagnosticRepository.log("Car session requested")
        return CarSession()
    }
}


