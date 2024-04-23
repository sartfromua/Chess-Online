package com.example.chessonline.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.chessonline.BLACK_TEAM
import com.example.chessonline.Figure
import com.example.chessonline.Game
import com.example.chessonline.WHITE_TEAM

class MainActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityMainBinding
    private lateinit var board: BoardCanvas
    private lateinit var viewModel: FiguresViewModel
    private lateinit var webSocketViewModel: WebSocketViewModel
    private var game = Game(false, WHITE_TEAM, WHITE_TEAM)
    private var figures: MutableList<Figure> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        board = BoardCanvas(this)
        setContentView(board)
        viewModel = ViewModelProvider(this)[FiguresViewModel::class.java]
        webSocketViewModel = ViewModelProvider(this)[WebSocketViewModel::class.java]

        // restart button OnClick
        board.gameRestartButton.observe(this) {
//            viewModel.restartFigures(game.team)
//            game.turn = WHITE_TEAM
            game = Game(false, WHITE_TEAM, WHITE_TEAM, game.channel+1)
            lookForAGame()
        }

        // refreshing figures on board when changing them in DB
        viewModel.figuresListLD.observe(this) {
//            Log.d("XXXXX", "Figures list: \n$it")
            figures = it.toMutableList()
            board.figures = it.toMutableList()
            board.team = game.team
            board.turn = game.turn
            board.invalidate()
        }

        // make a move in DB when figure on canvas moved
        board.move.observe(this) {
            val chosenPos = it.first
            val position = it.second

            val indChosen = board.getIndexFigureOnPos(chosenPos)

            if (indChosen != null && board.canFigureGo(figures[indChosen], position)
                && game.turn == figures[indChosen].team) {
                Log.d("XXXX", "Move ${figures[indChosen]} to $position")
                // Moving figure and deleting figure on pos if needed
                viewModel.moveFigureTo(chosenPos, position)
                webSocketViewModel.sendMessage("MOVE ${chosenPos.x},${chosenPos.y} to ${position.x},${position.y}")
                game.turn = if (game.turn == WHITE_TEAM) BLACK_TEAM else WHITE_TEAM
            }
        }

        // WebSocket commands processing
        webSocketViewModel.messageLd.observe(this) {
//            Log.d("PieSocket", "MessageLd: $it")
            if (it == webSocketViewModel.lastMessage) {
                webSocketViewModel.nextMessage()
                return@observe
            }
            if ("MOVE" in it) {
                val pos = MessageParser.parseMoveMessage(it)
                Log.d("PieSocket", "Parsed message: $pos")
                viewModel.moveFigureTo(pos.first, pos.second)
                game.turn = if (game.turn == WHITE_TEAM) BLACK_TEAM else WHITE_TEAM
            }
            if ("LOOKING FOR A GAME" in it) {
                val channelId = MessageParser.parseLookingMessage(it)
                if (channelId == game.channel && !game.started) {
                    webSocketViewModel.sendMessage("START GAME in channel:$channelId")
                } else webSocketViewModel.sendMessage("BUSY channel:$channelId")
            }
            if ("START GAME" in it) {
                val channelId = MessageParser.parseLookingMessage(it)
                if (channelId == game.channel && !game.started) {
                    webSocketViewModel.sendMessage("YOU PLAY FOR BLACK")
                    game.started = true
                    game.team = WHITE_TEAM
                    game.turn = WHITE_TEAM
                    viewModel.restartFigures(game.team)
                }
            }
            if ("YOU PLAY FOR BLACK" in it) {
                game.started = true
                game.team = BLACK_TEAM
                game.turn = WHITE_TEAM
                viewModel.restartFigures(game.team)
            }
            if ("BUSY" in it) {
                game.channel += 1
                lookForAGame()
            }
        }

//        if (!game.started) {
//            lookForAGame()
//        }

        viewModel.getFiguresList()
    }

    private fun lookForAGame() {
        webSocketViewModel.sendMessage("LOOKING FOR A GAME in channel:${game.channel}")
    }
}