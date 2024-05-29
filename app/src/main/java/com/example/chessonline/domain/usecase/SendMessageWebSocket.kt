package com.example.chessonline.domain.usecase

import com.example.chessonline.data.socket.PieWebSocket

class SendMessageWebSocket {
    fun sendMessageWebSocket(text: String, channelId: Int = 0) {
        if (PieWebSocket.channelId != channelId) {
            PieWebSocket.channelId = channelId
            PieWebSocket.WebSocketService.rebuildWebSocket()
        }
        PieWebSocket.WebSocketService.ws.send(text)
    }
}