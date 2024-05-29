package com.example.chessonline.data.socket

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class PieSocketListener : WebSocketListener() {
    val messageLD: MutableLiveData<String> = MediatorLiveData()

    override fun onOpen(webSocket: WebSocket, response: Response) {
        webSocket.send("WebSocket has connected!")
        output("Connected")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        output("Received : $text")
        output(webSocket.request().url.toString())
        this.messageLD.postValue(text)
        Log.d(TAG, "${this.messageLD.value}")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null)
        output("Closing : $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        output("Error : " + t.message)
    }

    fun output(text: String?) {
        Log.d(TAG, text!!)
    }

    companion object {
        private const val NORMAL_CLOSURE_STATUS = 1000
        private const val TAG = "ListenerPieSocket"
    }
}