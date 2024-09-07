package com.example.chessonline.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.chessonline.BLACK_TEAM
import com.example.chessonline.Game
import com.example.chessonline.Position
import com.example.chessonline.WHITE_TEAM
import kotlin.math.sign

class MainActivity : AppCompatActivity() {
    private lateinit var board: BoardCanvas
    private lateinit var viewModel: FiguresViewModel
    private lateinit var webSocketViewModel: WebSocketViewModel
    private var game = Game(false, WHITE_TEAM, WHITE_TEAM)
    private val timeToLose = 300L
    var timeWhite = timeToLose
    var timeBlack = timeToLose
    private lateinit var timer: CountDownTimer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        board = BoardCanvas(this)
        setContentView(board)
        viewModel = ViewModelProvider(this)[FiguresViewModel::class.java]
        webSocketViewModel = ViewModelProvider(this)[WebSocketViewModel::class.java]


        // restart button OnClick
        board.gameRestartButton.observe(this) {
            gameResetSettings()
            lookForAGame()
        }

        board.chosenRoomLD.observe(this) {
            game.channel = it
            webSocketViewModel.setChannelId(game.channel)
        }

        // refreshing figures on board when changing them in DB
        viewModel.figuresListLD.observe(this) {
            Log.d("XXXXX", "Figures list: \n$it")
            board.check = ""
            board.figures = it.toMutableList()
            board.team = game.team
            board.turn = game.turn

            board.checkMateCheck()

            board.invalidate()
        }

        board.figurePromotion.observe(this) {
            Log.d("XXXXX", "Promotion {$it}")
            val figure = it.first
            figure.name = it.second
            viewModel.editFigure(figure)
        }

        board.gameOverLD.observe(this) {
            if (it) timer.cancel()
        }

        // make a move in DB when figure on canvas moved
        board.move.observe(this) {
            val chosenPos = it.first
            var position = it.second

            val indChosen = board.getIndexFigureOnPos(chosenPos)

            if (indChosen != null && board.canFigureGo(board.figures[indChosen], position)
                && (game.turn == board.figures[indChosen].team || board.testMod)) {
                Log.d("XXXX", "Move ${board.figures[indChosen]} to $position")
                // Moving figure and deleting figure on pos if needed
                if (board.pessant != 0) {
                    viewModel.removeFigure(Position(position.x, position.y+board.pessant))
                    webSocketViewModel.sendMessage("FALL ${position.x},${position.y+board.pessant}")
                }
                if (board.promotionCheck) return@observe

                if (board.castling != null) {
                    // castling
                    val king = board.castling!!.first
                    val rook = board.castling!!.second
                    position = Position(king.pos.x + 2 * sign((rook.pos.x-king.pos.x).toDouble()).toInt(), king.pos.y)
                    val rookPos = Position(king.pos.x + sign((rook.pos.x-king.pos.x).toDouble()).toInt(), king.pos.y)

                    viewModel.moveFigureTo(rook.pos, rookPos)
                    viewModel.moveFigureTo(king.pos, position)

                    webSocketViewModel.sendMessage(
                        "CASTLING ${chosenPos.x},${chosenPos.y} to ${position.x},${position.y}" +
                                "\n${rook.pos.x},${rook.pos.y} to ${rookPos.x},${rookPos.y}" +
                                "\nTIME $timeWhite,$timeBlack"
                    )
                    board.castling = null
                } else {
                    // usual MOVE
                    viewModel.moveFigureTo(chosenPos, position)
                    webSocketViewModel.sendMessage(
                        "MOVE ${chosenPos.x},${chosenPos.y} to ${position.x},${position.y}" +
                                "\nTIME $timeWhite,$timeBlack"
                    )
                }
                if (game.turn == WHITE_TEAM) {
                    game.turn = BLACK_TEAM
                    timerStart(BLACK_TEAM, timeBlack)
                } else {
                    game.turn = WHITE_TEAM
                    timerStart(WHITE_TEAM, timeWhite)
                }
            }
        }

        // WebSocket commands processing
        commandsProcessing()

//        viewModel.getFiguresList()
    }

    private fun commandsProcessing() {
        webSocketViewModel.messageLd.observe(this) {
            if (it == webSocketViewModel.lastMessage) {
                webSocketViewModel.nextMessage()
                return@observe
            }
            Log.d("PieSocket", "Processing message: $it")
            if ("MOVE" in it) {
                val move = MessageParser.parseMoveMessage(it)
                Log.d("PieSocket", "Parsed message: $move")
                viewModel.moveFigureTo(move.fromPos, move.toPos)

                timeWhite = move.whiteTimer
                timeBlack = move.blackTimer
                if (game.turn == WHITE_TEAM) {
                    game.turn = BLACK_TEAM
                    timerStart(BLACK_TEAM, timeBlack)
                } else {
                    game.turn = WHITE_TEAM
                    timerStart(WHITE_TEAM, timeWhite)
                }
                board.invalidate()

            }
            if ("CASTLING" in it) {
                val moves = MessageParser.parseCastling(it)
                viewModel.moveFigureTo(moves.first.fromPos, moves.first.toPos)
                viewModel.moveFigureTo(moves.second.fromPos, moves.second.toPos)

                timeWhite = moves.first.whiteTimer
                timeBlack = moves.first.blackTimer
                if (game.turn == WHITE_TEAM) {
                    game.turn = BLACK_TEAM
                    timerStart(BLACK_TEAM, timeBlack)
                } else {
                    game.turn = WHITE_TEAM
                    timerStart(WHITE_TEAM, timeWhite)
                }
                board.invalidate()
            }
            if ("FALL" in it) {
                val pos = MessageParser.parseFallMessage(it)
                viewModel.removeFigure(pos)
            }
            if ("LOOKING FOR A GAME" in it) {
                if (!game.started) {
                    webSocketViewModel.sendMessage("START GAME")
                } else webSocketViewModel.sendMessage("BUSY")
            }
            if ("START GAME" in it) {
                if (board.waiting && !game.started) {
                    webSocketViewModel.sendMessage("YOU PLAY FOR BLACK")
                    game.started = true
                    board.waiting = false
                    game.team = WHITE_TEAM
                    game.turn = WHITE_TEAM
                    viewModel.restartFigures(game.team)
                    timerStart(WHITE_TEAM, timeWhite)
                    board.invalidate()
                }
            }
            if ("YOU PLAY FOR BLACK" in it) {
                game.started = true
                board.waiting = false
                game.team = BLACK_TEAM
                game.turn = WHITE_TEAM
                viewModel.restartFigures(game.team)
                timerStart(WHITE_TEAM, timeWhite)
                board.invalidate()
            }
            if ("BUSY" in it) {
                board.waiting = false
                board.invalidate()
            }
        }
    }

    private fun gameResetSettings() {
        game = Game(false, WHITE_TEAM, WHITE_TEAM, game.channel)
        if (!board.testMod) board.waiting = true
        board.promotionCheck = false
        board.check = ""
        board.checkMate = ""
        board.team = game.team
        board.turn = game.turn
        board.timeGameOver = ""
        board.gameOverLD.value = false

        timeWhite = timeToLose
        timeBlack = timeToLose
    }

    private fun timerStart(team: String, timeLeft: Long) {
        if (this::timer.isInitialized) timer.cancel()
        timer = object: CountDownTimer(timeLeft*1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (team == WHITE_TEAM) {
                    timeWhite = millisUntilFinished / 1000
                    board.timerWhite = timeWhite
                } else {
                    timeBlack = millisUntilFinished / 1000
                    board.timerBlack = timeBlack
                }

                board.invalidate()
            }

            override fun onFinish() {
                Log.d("XXXXX", "END OF GAME")
                if (timeWhite == 0L) board.timeGameOver = WHITE_TEAM
                if (timeBlack == 0L) board.timeGameOver = BLACK_TEAM
            }
        }

        timer.start()


    }

    private fun lookForAGame() {
        if (board.testMod) {
            game.started = true
            board.waiting = false
            game.team = WHITE_TEAM
            game.turn = WHITE_TEAM
            viewModel.restartFigures(game.team)
            timerStart(WHITE_TEAM, timeWhite)
            board.invalidate()
        } else webSocketViewModel.sendMessage("LOOKING FOR A GAME")

        gameResetSettings()
        board.invalidate()
    }
}