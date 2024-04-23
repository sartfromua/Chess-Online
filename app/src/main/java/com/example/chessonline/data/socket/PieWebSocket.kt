package com.example.chessonline.data.socket

import androidx.lifecycle.MutableLiveData
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okio.ByteString

object PieWebSocket {
    private var channelId = 1
    private val request: Request by lazy {
        Request
            .Builder()
            .url("wss://s12331.ams1.piesocket.com/v3/$channelId?api_key=${com.example.chessonline.apiKey}&notify_self=1")
            .build()
    }
    private val listener: PieSocketListener by lazy { PieSocketListener() }
    private val client by lazy { OkHttpClient() }

    object WebSocketService {
        val ws: WebSocket by lazy {
            client.newWebSocket(request, listener)
        }
        val messageLD: MutableLiveData<String> = listener.messageLD
    }
}