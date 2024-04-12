package com.example.chessonline.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.chessonline.BLACK_TEAM
import com.example.chessonline.Position
import com.example.chessonline.databinding.ActivityMainBinding
import okhttp3.OkHttpClient

class MainActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityMainBinding
    private lateinit var board: BoardCanvas
    private lateinit var viewModel: FiguresViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
        board = BoardCanvas(this)
        setContentView(board)
        viewModel = ViewModelProvider(this)[FiguresViewModel::class.java]


    }

    private fun webSocket() {
        val client = OkHttpClient()
//
//        val channelId = 1
//
//        lateinit var request: Request
//
//        var index = 1
//        lateinit var listener: PieSocketListener
//        lateinit var ws: WebSocket
//
//        binding.button2.setOnClickListener {
//            request = Request
//                    .Builder()
//                .url("wss://s12331.ams1.piesocket.com/v3/$channelId?api_key=${com.example.checkers.apiKey}&notify_self=1")
//                .build()
//            listener = PieSocketListener()
//            ws = client.newWebSocket(request, listener)
//        }
//
//        binding.button1.setOnClickListener {
//            ws.send("Message #$index")
//            index += 1
//        }
    }
}