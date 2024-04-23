package com.example.chessonline.domain.usecase

import androidx.lifecycle.LiveData
import com.example.chessonline.data.socket.PieWebSocket

class GetMessageLiveData {
    fun getMessageLiveData(): LiveData<String> {
        return PieWebSocket.WebSocketService.messageLD
    }
}