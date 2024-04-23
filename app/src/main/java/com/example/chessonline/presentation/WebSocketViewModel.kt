package com.example.chessonline.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.chessonline.domain.usecase.GetMessageLiveData
import com.example.chessonline.domain.usecase.SendMessageWebSocket

class WebSocketViewModel(application: Application): AndroidViewModel(application)  {
//    val channelId = 1
    private val getMessageLiveDataUseCase = GetMessageLiveData()
    private val sendMessageWebSocketUseCase = SendMessageWebSocket()
    private var _lastMessage: String = ""
    val lastMessage: String
        get() = _lastMessage

    fun sendMessage(text: String) {
        Log.d("PieSocket", "Send: $text")
        _lastMessage = text
        sendMessageWebSocketUseCase.sendMessageWebSocket(text)
    }

    private val _messageLd = getMessageLiveDataUseCase.getMessageLiveData()
    val messageLd: LiveData<String>
        get() = _messageLd

    fun nextMessage() {
        _lastMessage = ""
    }
//    ws.send("Message ")
}