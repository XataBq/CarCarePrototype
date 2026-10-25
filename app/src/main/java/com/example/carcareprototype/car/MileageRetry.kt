package com.example.carcareprototype.car

import android.os.Handler
import android.os.Looper
import com.example.carcareprototype.diagnostics.DiagnosticRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Main-thread command routing. A stopped/destroyed session never owns a retry callback. */
internal object MileageRetry {
    private val handler = Handler(Looper.getMainLooper())
    private var owner: String? = null
    private var retry: (() -> Unit)? = null
    private val _feedback = MutableStateFlow<String?>(null)
    val feedback = _feedback.asStateFlow()

    fun attach(token: String, action: () -> Unit) {
        check(Looper.myLooper() == Looper.getMainLooper())
        owner = token
        retry = action
        _feedback.value = null
    }

    fun detach(token: String) {
        check(Looper.myLooper() == Looper.getMainLooper())
        if (owner == token) { owner = null; retry = null }
    }

    fun request() {
        // Capture owner now so an enqueued click cannot act on a different connection.
        check(Looper.myLooper() == Looper.getMainLooper())
        val expectedOwner = owner
        val expectedAction = retry
        handler.post {
            if (expectedOwner != null && expectedOwner == owner && expectedAction != null && retry === expectedAction) {
                _feedback.value = "Retry requested; see current request status"
                expectedAction()
            } else {
                val reason = "Retry unavailable: open this app in Android Auto first"
                _feedback.value = reason
                DiagnosticRepository.log(reason)
            }
        }
    }
}
