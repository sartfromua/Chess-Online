package com.example.chessonline.domain.usecase

import com.example.chessonline.data.socket.PieWebSocket

class SendMessageWebSocket {
    fun sendMessageWebSocket(text: String) {
        PieWebSocket.WebSocketService.ws.send(text)
    }
}