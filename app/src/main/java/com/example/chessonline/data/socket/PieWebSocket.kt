package com.example.chessonline.data.socket

import androidx.lifecycle.MutableLiveData
import com.example.chessonline.apiKey
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okio.ByteString

object PieWebSocket {
    var channelId = 0
    private val requestDelegate = resettableLazy {
        Request
            .Builder()
            .url("wss://s12331.ams1.piesocket.com/v3/$channelId?api_key=$apiKey&notify_self=1")
            .build()
    }
    val request: Request by requestDelegate
    private val listener: PieSocketListener by lazy { PieSocketListener() }
    private val client by lazy { OkHttpClient() }

    private fun rebuildRequest() {
        requestDelegate.reset()
    }

    object WebSocketService {
        private val wsDelegate = resettableLazy {
            client.newWebSocket(request, listener)
        }
        val ws: WebSocket by wsDelegate
        val messageLD: MutableLiveData<String> = listener.messageLD

        fun rebuildWebSocket() {
            rebuildRequest()
            wsDelegate.reset()
        }
    }
}

fun <T> resettableLazy(initializer: () -> T): ResettableLazy<T> {
    return ResettableLazy(initializer)
}
