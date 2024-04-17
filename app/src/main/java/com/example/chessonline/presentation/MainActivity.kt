package com.example.chessonline.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.chessonline.Figure
import com.example.chessonline.Position
import com.example.chessonline.WHITE_TEAM
import okhttp3.OkHttpClient

class MainActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityMainBinding
    private lateinit var board: BoardCanvas
    private lateinit var viewModel: FiguresViewModel
    private var team = WHITE_TEAM
    private var figures: MutableList<Figure> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
        board = BoardCanvas(this)
        setContentView(board)
        viewModel = ViewModelProvider(this)[FiguresViewModel::class.java]

        // Adding start figures
//        viewModel.addFiguresList(BoardData.getStartFigures(team))


        // refreshing figures on board
        viewModel.figuresListLD.observe(this) {
//            Log.d("XXXXX", "Figures list: \n$it")
            figures = it.toMutableList()
            board.figures = it.toMutableList()
            board.team = team
            board.invalidate()
        }

        viewModel.getFiguresList()

        board.move.observe(this) {
            val chosenPos = it.first
            val position = it.second

            val indChosen = board.getIndexFigureOnPos(chosenPos)

            if (indChosen != null && board.canFigureGo(figures[indChosen], position)) {
                Log.d("XXXX", "Move ${figures[indChosen]} to $position")
                // Moving figure and deleting figure on pos if needed
                viewModel.moveFigureTo(chosenPos, position)
            }
        }
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