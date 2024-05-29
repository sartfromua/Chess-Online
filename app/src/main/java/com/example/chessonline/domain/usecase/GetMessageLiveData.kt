package com.example.chessonline.domain.usecase

import androidx.lifecycle.LiveData
import com.example.chessonline.data.socket.PieWebSocket

class GetMessageLiveData {
    fun getMessageLiveData(channelId: Int = 0): LiveData<String> {
        if (PieWebSocket.channelId != channelId) {
            PieWebSocket.channelId = channelId
            PieWebSocket.WebSocketService.rebuildWebSocket()
        }
        return PieWebSocket.WebSocketService.messageLD
    }
}