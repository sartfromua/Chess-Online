package com.example.chessonline.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.chessonline.domain.usecase.GetMessageLiveData
import com.example.chessonline.domain.usecase.SendMessageWebSocket

class WebSocketViewModel(application: Application): AndroidViewModel(application)  {
    private var channelId = 0
    private val getMessageLiveDataUseCase = GetMessageLiveData()
    private val sendMessageWebSocketUseCase = SendMessageWebSocket()
    private var _lastMessage: String = ""
    val lastMessage: String
        get() = _lastMessage

    fun setChannelId(channelId: Int) {
        this.channelId = channelId
        _messageLd = getMessageLiveDataUseCase.getMessageLiveData(channelId)
    }

    fun sendMessage(text: String) {
        Log.d("PieSocket", "Send: $text")
        if ("test" in text) Log.d("PieSocket", "Test: $text")
        _lastMessage = text
        sendMessageWebSocketUseCase.sendMessageWebSocket(text, channelId)
    }

    private var _messageLd = getMessageLiveDataUseCase.getMessageLiveData(channelId)
    val messageLd: LiveData<String>
        get() = _messageLd

    fun nextMessage() {
        _lastMessage = ""
    }
//    ws.send("Message ")
}