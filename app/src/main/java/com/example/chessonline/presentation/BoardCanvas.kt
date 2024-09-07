package com.example.chessonline.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.chessonline.BISHOP_NAME
import com.example.chessonline.BLACK_TEAM
import com.example.chessonline.Figure
import com.example.chessonline.HORSE_NAME
import com.example.chessonline.KING_NAME
import com.example.chessonline.PAWN_NAME
import com.example.chessonline.Position
import com.example.chessonline.QUEEN_NAME
import com.example.chessonline.ROOK_NAME
import com.example.chessonline.WHITE_TEAM
import com.example.chessonline.Coordinates
import kotlin.math.abs
import kotlin.math.sign

class BoardCanvas(context: Context?): View(context) {
    private var boardCoords: Pair<Coordinates, Coordinates>? = null
    private var cellSize: Float? = null
    private var timerCoords: Pair<Coordinates, Coordinates>? = null
    var team = "NO_TEAM"
    var turn = WHITE_TEAM
    private var position = Position(-1, -1)
    var figures = mutableListOf<Figure>()
    private var chosenPos: Position = Position(-1, -1)
    val move: MutableLiveData<Pair<Position, Position>> = MediatorLiveData()
    var gameRestartButton: MutableLiveData<Boolean?> = MediatorLiveData()
    var waiting: Boolean = false
    var timerWhite = 0L
    var timerBlack = 0L
    var check: String = ""
    var checkMate: String = ""
    var gameOverLD = MutableLiveData<Boolean>(false)
    var timeGameOver = ""
    val chosenRoomLD = MutableLiveData<Int>(0)
    var castling: Pair<Figure, Figure>? = null
    var pessant = 0
    var promotionCheck = false
    var figurePromotion = MutableLiveData<Pair<Figure, String>>()
    var testMod = false

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        boardCoords = boardCoords ?: BoardData.getBoardCoordinates(width, height)
        cellSize = cellSize ?: BoardData.getCellSize(width)
        timerCoords = timerCoords ?: BoardData.getTimerCoordinates(width, height)

        drawBoardGrid(canvas)
        drawRestartButton(canvas)
        drawRooms(canvas)
        drawFigures(canvas)
        drawTimer(canvas)

        drawPawnPromotion(canvas)
        gameEndCheck(canvas)

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        // Room choosing
        val rooms = BoardData.getRoomsCoordinates(width, height).toList()
        if (event!!.y in rooms[0].first.y..rooms[0].second.y) {
            for (ind in rooms.indices) {
                if (event.x in rooms[ind].first.x..rooms[ind].second.x) {
                    chosenRoomLD.value = ind+1
                    invalidate()
                    break
                }
            }
        }

        // Restart button click detection
        val restartButt = BoardData.getRestartButtonCoordinates(width, height)
        if (event.x in restartButt.first.x..restartButt.second.x &&
            event.y in restartButt.first.y..restartButt.second.y)
            gameRestartButton.value = (gameRestartButton.value ?: true).xor(true)

        val offlineButt = BoardData.getOfflineButtonCoordinates(width, height)
        if (event.x in offlineButt.x..offlineButt.x+cellSize!! &&
            event.y in offlineButt.y..offlineButt.y+cellSize!!)
            testMod = !testMod

        if (gameOverLD.value == true) return super.onTouchEvent(event)
        if (!(turn == team || testMod)) return super.onTouchEvent(event)


        if (promotionCheck) {
            Log.d("YYYYY", "Promotion check")
            var cords = BoardData.getMessageCoordinates(width, height)
            for (name in listOf("queen", "bishop", "horse", "rook")) {
                if (cords.x < event.x && event.x < cords.x+ cellSize!! &&
                    cords.y - cellSize!! < event.y  && cords.y > event.y) {
                    Log.d("YYYYY", "Promotion cords right")
                    val ind = getIndexFigureOnPos(chosenPos)?: return super.onTouchEvent(event)
                    figurePromotion.value = Pair(figures[ind], name)
                    Log.d("YYYYY", "Promotion ${figurePromotion.value}")
                    promotionCheck = false
                    chosenPos = Position()
                    return super.onTouchEvent(event)
                }
                cords = Coordinates(cords.x + cellSize!!*1.3F, cords.y)
            }
        }


        // Position by click detection
        if (team == WHITE_TEAM) {
            position = getPosition(Coordinates(event.x, event.y))
            Log.d(TAG, position.toString())
        } else {
            position = getPosition(Coordinates(event.x, event.y))
            position.x = 9 - position.x
            position.y = 9 - position.y
            Log.d(TAG, position.toString())
        }

//        Log.d(TAG, "ChosenPos: $chosenPos")
        if (chosenPos == Position(-1, -1) && getIndexFigureOnPos(position) != null) {
            chosenPos = position

            promotionCheck = promotionCheck(position)
            invalidate()
        } else {
            // Making move
            move.value = Pair(chosenPos, position)

            chosenPos = Position()
        }
        if (getIndexFigureOnPos(position) == null) {
            chosenPos = Position()
            invalidate()
        }

        return super.onTouchEvent(event)
    }



    fun getIndexFigureOnPos(position: Position): Int? {
        for (fig in figures) {
            if (fig.pos == position) {
//                Log.d("XXXXX", "Figure on $position: ${figures[figures.indexOf(fig)]}")
                return figures.indexOf(fig)
            }
        }
        return null
    }

    @SuppressLint("DiscouragedApi")
    private fun drawRestartButton(canvas: Canvas) {
        val buttonCords = BoardData.getRestartButtonCoordinates(width, height)
        val coords1 = buttonCords.first
        val coords2 = buttonCords.second

        var resId = context.resources.getIdentifier("restart_button", "drawable", context.packageName)
        var bitmap = BitmapFactory.decodeResource(resources, resId)
        var drawable = BitmapDrawable(resources, bitmap)
        drawable.setBounds((coords1.x).toInt(), (coords1.y).toInt(),
            (coords2.x).toInt(), (coords2.y).toInt())
        drawable.draw(canvas)

        if (waiting) {
            resId = context.resources.getIdentifier("waiting_label", "drawable", context.packageName)
            bitmap = BitmapFactory.decodeResource(resources, resId)
            drawable = BitmapDrawable(resources, bitmap)
            drawable.setBounds((coords1.x).toInt(), (coords2.y).toInt(),
                (coords2.x).toInt(), (coords2.y + (coords2.y - coords1.y)/2).toInt())
            drawable.draw(canvas)
        }

        if (testMod) {
            resId = context.resources.getIdentifier("pressed_button", "drawable", context.packageName)
        } else {
            resId = context.resources.getIdentifier("unpressed_button", "drawable", context.packageName)
        }

        val cordsBut = BoardData.getOfflineButtonCoordinates(width, height)
        bitmap = BitmapFactory.decodeResource(resources, resId)
        drawable = BitmapDrawable(resources, bitmap)
        drawable.setBounds((cordsBut.x).toInt(), (cordsBut.y).toInt(),
            (cordsBut.x + cellSize!!).toInt(), (cordsBut.y + cellSize!!).toInt())
        drawable.draw(canvas)

    }

    @SuppressLint("DiscouragedApi")
    private fun drawRooms(canvas: Canvas) {
        val cords =
            BoardData.getRoomsCoordinates(width, height).toList()
        var i = 1
        for (room in cords) {
            val resId = context.resources.getIdentifier("room_$i", "drawable", context.packageName)
            i++
            val bitmap = BitmapFactory.decodeResource(resources, resId)
            val drawable = BitmapDrawable(resources, bitmap)
            drawable.setBounds((room.first.x).toInt(), (room.first.y).toInt(),
                (room.second.x).toInt(), (room.second.y).toInt())
            drawable.draw(canvas)
        }
        if (chosenRoomLD.value in 1..3) {
            val room = cords[chosenRoomLD.value!! - 1]
            val resId = context.resources.getIdentifier("chosen_room", "drawable", context.packageName)
            val bitmap = BitmapFactory.decodeResource(resources, resId)
            val drawable = BitmapDrawable(resources, bitmap)
            drawable.setBounds((room.first.x).toInt(), (room.first.y).toInt(),
                (room.second.x).toInt(), (room.second.y).toInt())
            drawable.draw(canvas)
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun drawTimer(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 7F

        paint.textSize = 75f
        if (team == BLACK_TEAM) {
            canvas.drawText(getTime(timerBlack), timerCoords!!.first.x, timerCoords!!.first.y, paint)
            canvas.drawText(getTime(timerWhite), timerCoords!!.second.x, timerCoords!!.second.y, paint)
        } else {
            canvas.drawText(getTime(timerWhite), timerCoords!!.first.x, timerCoords!!.first.y, paint)
            canvas.drawText(getTime(timerBlack), timerCoords!!.second.x, timerCoords!!.second.y, paint)
        }

    }

    fun getTime(time: Long): String {
        var timer = ""
        if (time / 60 < 10) timer += "0"
        timer += "${time/60}:"
        if (time % 60 < 10) timer += "0"
        timer += "${time%60}"
        return timer
    }

    private fun drawBoardGrid(canvas: Canvas) {
        canvas.drawColor(Color.WHITE)

        val paint = Paint()
        paint.color = Color.BLACK
        paint.strokeWidth = 5F

        val boardStart = boardCoords!!.first
        val boardEnd = boardCoords!!.second
        for (i in 0..8) {
            canvas.drawLine(boardStart.x+i* cellSize!!, boardStart.y, boardStart.x+i* cellSize!!, boardEnd.y, paint)
            canvas.drawLine(boardStart.x, boardStart.y+i* cellSize!!, boardEnd.x, boardStart.y+i* cellSize!!, paint)
        }
        if (chosenPos == Position(-1, -1)) return
        val index = getIndexFigureOnPos(chosenPos) ?: return
        if (turn == figures[index].team) {
            if (turn != team && !testMod) return
            val possibleMoves = getPossibleMoves(figures[getIndexFigureOnPos(chosenPos)!!])
            if (team == BLACK_TEAM)
                drawColoredCell(Position(9 - chosenPos.x, 9 - chosenPos.y), "#CCFFFF", canvas)
            else drawColoredCell(chosenPos, "#CCFFFF", canvas)
            for (move in possibleMoves) {
                if (team == BLACK_TEAM)
                    drawColoredCell(Position(9 - move.first.x, 9 - move.first.y), move.second, canvas)
                else drawColoredCell(move.first, move.second, canvas)
            }
        }
//        Log.d(TAG, "Done drawing board!")
    }

    private fun drawFigures(canvas: Canvas) {
        for (fig in figures) drawFigure(fig, canvas)
//        Log.d(TAG, "Done drawing figures!")
    }

    private fun drawColoredCell(position: Position, hexColor: String, canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.parseColor(hexColor)
        paint.style = Paint.Style.FILL
        val coords = getCoordinates(position)
        canvas.drawRect(coords.x, coords.y, coords.x+cellSize!!, coords.y+cellSize!!, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5F
        paint.color = Color.BLACK
        canvas.drawRect(coords.x, coords.y, coords.x+cellSize!!, coords.y+cellSize!!, paint)
    }



    @SuppressLint("DiscouragedApi")
    private fun drawFigure(figure: Figure, canvas: Canvas) {
        val resId = context.resources.getIdentifier("${figure.team.lowercase()}_${figure.name}", "drawable", context.packageName)

        val bitmap = BitmapFactory.decodeResource(resources, resId)
        val drawable = BitmapDrawable(resources, bitmap)
        var pos = figure.pos.copy()
        if (team == BLACK_TEAM) pos = Position(9 - figure.pos.x,  9 - figure.pos.y)
        val coordiantes: Coordinates = getCoordinates(pos)
        drawable.setBounds((coordiantes.x).toInt(), (coordiantes.y).toInt(),
            (coordiantes.x+cellSize!!).toInt(), (coordiantes.y+cellSize!!).toInt())
        drawable.draw(canvas)
//        Log.d(TAG, "Done drawing ${figure.team}_${figure.name}!")
    }

    @SuppressLint("DiscouragedApi")
    private fun drawPawnPromotion(canvas: Canvas) {
//        Log.d("PROMOTION", "$promotionCheck || $team")
        if (!promotionCheck || team == "NO_TEAM") return
//        Log.d("PROMOTION", "PROMOTION draw")
        var cords = BoardData.getMessageCoordinates(width, height)
        for (name in listOf("queen", "bishop", "horse", "rook")) {
            val resId = context.resources.getIdentifier("${team}_$name", "drawable", context.packageName)

            val bitmap = BitmapFactory.decodeResource(resources, resId)

            val drawable = BitmapDrawable(resources, bitmap)
            drawable.setBounds(
                (cords.x).toInt(), (cords.y - cellSize!!).toInt(),
                (cords.x + cellSize!!).toInt(), (cords.y).toInt(),
            )
            drawable.draw(canvas)
            cords = Coordinates(cords.x + cellSize!!*1.3F, cords.y)
        }
    }

    fun canFigureGo(figure: Figure, pos: Position, ignoreTurn: Boolean = false): Boolean {
        pessant = 0
        if (((figure.team != team || turn != team) && !(testMod || ignoreTurn))) return false
//        Log.d("XXX", "CanFigureGo: $figure to $pos")

        val index = getIndexFigureOnPos(pos)
//        Log.d("XXX", "Figure on pos: ${figures.getOrNull(index?: 100)}")

        if (index != null)
            if (figure.name != KING_NAME && figure.team == figures[index].team) {
//                Log.d("XXXX", "same teams! WRONG move")
                return false
            }


        when (figure.name) {
            PAWN_NAME -> {
                // Attack
                if (pos.y == figure.pos.y + 1 && abs(pos.x - figure.pos.x) == 1 &&
                    index != null && figure.team == WHITE_TEAM) return true
                if (pos.y == figure.pos.y - 1 && abs(pos.x - figure.pos.x) == 1 &&
                    index != null && figure.team == BLACK_TEAM) return true

                // en passant
                if (figure.team == WHITE_TEAM && figure.pos.y == 5 && index == null) {
                    val ind = getIndexFigureOnPos(Position(pos.x, pos.y-1))
                    if (ind != null && figures[ind].name == PAWN_NAME && figures[ind].team != figure.team)
                        {pessant = -1; return true}
                }
                if (figure.team == BLACK_TEAM && figure.pos.y == 4 && index == null) {
                    val ind = getIndexFigureOnPos(Position(pos.x, pos.y+1))
                    if (ind != null && figures[ind].name == PAWN_NAME && figures[ind].team != figure.team)
                        {pessant = 1; return true}
                }

                // Move
                if (pos.x == figure.pos.x){
                    if (figure.team == WHITE_TEAM) {
                        if (pos.y == figure.pos.y + 1 && index == null) return true
                        if (pos.y == figure.pos.y + 2 && figure.pos.y == 2 &&
                            index == null &&
                            getIndexFigureOnPos(Position(pos.x, pos.y-1)) == null) return true
                    } else {
                        if (pos.y == figure.pos.y - 1 && getIndexFigureOnPos(pos) == null) return true
                        if (pos.y == figure.pos.y - 2 && figure.pos.y == 7 &&
                            index == null &&
                            getIndexFigureOnPos(Position(pos.x, pos.y+1)) == null) return true
                    }
                }
            }
            HORSE_NAME -> {
                if ((figure.pos.x == pos.x + 1 || figure.pos.x == pos.x - 1) &&
                    (figure.pos.y == pos.y + 2 || figure.pos.y == pos.y - 2))
                    return true
                if ((figure.pos.x == pos.x + 2 || figure.pos.x == pos.x - 2) &&
                    (figure.pos.y == pos.y + 1 || figure.pos.y == pos.y - 1))
                    return true
            }
            ROOK_NAME -> {
                if (figure.pos.x == pos.x) {
                    for (y in minOf(figure.pos.y, pos.y)+1..<maxOf(figure.pos.y, pos.y))
                        if (getIndexFigureOnPos(Position(pos.x, y)) != null) return false
                    return true
                }
                if (figure.pos.y == pos.y) {
                    for (x in minOf(figure.pos.x, pos.x)+1..<maxOf(figure.pos.x, pos.x))
                        if (getIndexFigureOnPos(Position(x, pos.y)) != null) return false
                    return true
                }
            }
            BISHOP_NAME -> {
                if (abs(figure.pos.x - pos.x) == abs(figure.pos.y - pos.y)) {
                    val dx = sign((pos.x - figure.pos.x).toDouble()).toInt()
                    val dy = sign((pos.y - figure.pos.y).toDouble()).toInt()
                    for (i in 1..<abs(pos.x - figure.pos.x))
                        if (getIndexFigureOnPos(Position(figure.pos.x + i*dx, figure.pos.y + i*dy)) != null)
                            return false
                    return true
                }
            }
            KING_NAME -> {
                val ind = getIndexFigureOnPos(pos)
                if (abs(figure.pos.x - pos.x) <= 1 && abs(figure.pos.y - pos.y) <= 1) {
                    if (ind == null || figure.team != figures[ind].team) return true
                } else {
                    if (ind == null) return false
                    // castling
                    if (figures[ind].name != ROOK_NAME) return false

                    if (figure.team == WHITE_TEAM && figures[ind].pos.y == 1 &&
                        (figures[ind].pos.x == 1 || figures[ind].pos.x == 8)) {
                        if (figure.pos == Position(5, 1)) {
                            for (x in minOf(figure.pos.x, pos.x)+1..<maxOf(figure.pos.x, pos.x))
                                if (getIndexFigureOnPos(Position(x, pos.y)) != null) return false
                            castling = Pair(figure, figures[ind])
                            return true
                        }
                    }
                    if (figure.team == BLACK_TEAM && figures[ind].pos.y == 8 &&
                        (figures[ind].pos.x == 1 || figures[ind].pos.x == 8))
                        if (figure.pos == Position(5, 8)) {
                            for (x in minOf(figure.pos.x, pos.x)+1..<maxOf(figure.pos.x, pos.x))
                                if (getIndexFigureOnPos(Position(x, pos.y)) != null) return false
                            castling = Pair(figure, figures[ind])
                            return true
                        }
                }
            }
            QUEEN_NAME -> {
                if (abs(figure.pos.x - pos.x) == abs(figure.pos.y - pos.y)) {
                    val dx = sign((pos.x - figure.pos.x).toDouble()).toInt()
                    val dy = sign((pos.y - figure.pos.y).toDouble()).toInt()
                    for (i in 1..<abs(pos.x - figure.pos.x))
                        if (getIndexFigureOnPos(Position(figure.pos.x + i*dx, figure.pos.y + i*dy)) != null) return false
                    return true
                }
                if (figure.pos.x == pos.x) {
                    for (y in minOf(figure.pos.y, pos.y)+1..<maxOf(figure.pos.y, pos.y))
                        if (getIndexFigureOnPos(Position(pos.x, y)) != null) return false
                    return true
                }
                if (figure.pos.y == pos.y) {
                    for (x in minOf(figure.pos.x, pos.x)+1..<maxOf(figure.pos.x, pos.x))
                        if (getIndexFigureOnPos(Position(x, pos.y)) != null) return false
                    return true
                }
            }
        }
        return false
    }

    private fun promotionCheck(position: Position): Boolean {
        val index = getIndexFigureOnPos(position) ?: return false
        val figure = figures[index]
        if (figure.name != PAWN_NAME || turn != figure.team) return false
        if (figure.team == WHITE_TEAM && figure.pos.y == 8) return true
        if (figure.team == BLACK_TEAM && figure.pos.y == 1) return true
        return false
    }

    private fun check(team: String): Boolean {
        var king: Figure? = null
        for (figure in figures) {
            if (figure.name == KING_NAME && figure.team == team) { king = figure; break }
        }
        if (king == null) return false
        for (figure in figures) {
            if (figure.team != king.team) {
                if (canFigureGo(figure, king.pos, ignoreTurn = true)) {
                    Log.d(TAG, "Check to $king")
                    return true
                }
            }
        }

        return false
    }

    private fun checkMate(team: String): Boolean {
        var king: Figure? = null
        for (figure in figures) {
            if (figure.name == KING_NAME && figure.team == team) { king = figure; break }
        }
        if (king == null) return false

        // if king is under check
        if (check != team) return false
        for (figure in figures) {
            if (figure.team != king.team) {
                if (canFigureGo(figure, king.pos, ignoreTurn = true)) {
                    var posDanger = true
                    for (fig in figures)
                        if (fig.team != figure.team)
                            if (canFigureGo(fig, figure.pos, ignoreTurn = true)) {
                                // if fig can beat figure and make cell safe again
                                posDanger = false
                            }
                    if (!posDanger) return false
                }
            }
        }
        val poses = getPossibleMoves(figure = king).map{ it.first}
        // check neighbor cell for safety
        for (pos in poses) {
            var safePos = true
            for (figure in figures) {
                if (figure.team != king.team) {
                    if (canFigureGo(figure, pos, ignoreTurn = true)) {
                        // if cell is in danger from figure
                        var posDanger = true
                        for (fig in figures)
                            if (fig.team != figure.team)
                                if (canFigureGo(fig, figure.pos, ignoreTurn = true)) {
                                    // if fig can beat figure and make cell safe again
                                    posDanger = false
                                }
                        safePos = !posDanger
                    }
                }
            }
//            Log.d("checkMateChecking", "$pos is safe: $safePos")
            if (safePos) return false
        }

        return true
    }

    fun checkMateCheck() {
        if (check(WHITE_TEAM)) {
            check = WHITE_TEAM
            if (checkMate(WHITE_TEAM)) checkMate = WHITE_TEAM
        }
        if (check(BLACK_TEAM)) {
            check = BLACK_TEAM
            if (checkMate(BLACK_TEAM)) checkMate = BLACK_TEAM
        }

        Log.d("YYYYY", "Check: '$check' | CheckMate: '$checkMate'")
    }

    private fun gameEndCheck(canvas: Canvas) {
        if (checkMate != "" || timeGameOver != "") {gameEnd(canvas); return}
        if (check != "") {
            val message = "Check to $check!"

            val paint = Paint()
            paint.color = Color.MAGENTA
            paint.style = Paint.Style.FILL
            paint.strokeWidth = 15F
            paint.textSize = 75F
            val cords = BoardData.getMessageCoordinates(width, height)
            canvas.drawText(message, cords.x, cords.y, paint)
        }
    }

    private fun gameEnd(canvas: Canvas) {
        val message = if (checkMate != "") "CheckMate to $checkMate!"
        else "$timeGameOver out of time!"
        val paint = Paint()
        paint.color = Color.MAGENTA
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 15F
        paint.textSize = 75F
        val cords = BoardData.getMessageCoordinates(width, height)
        canvas.drawText(message, cords.x, cords.y, paint)

        gameOverLD.value = true
    }

    private fun getPossibleMoves(figure: Figure): MutableList<Pair<Position, String>> {
        val moves = mutableListOf<Pair<Position, String>>()
        val moveColor = "#99FF00"
        val attackColor = "#FF3333"
        val castlingColor = "#B8860B"

        val x = figure.pos.x
        val y = figure.pos.y

        when (figure.name) {
            PAWN_NAME -> {
                if (figure.team == WHITE_TEAM) {
                    if (getIndexFigureOnPos(Position(x, y+1)) == null && y<=7) moves.add(Pair(Position(x, y+1), moveColor))
                    if (getIndexFigureOnPos(Position(x, y+1)) == null &&
                        getIndexFigureOnPos(Position(x, y+2)) == null &&
                        figure.pos.y == 2) moves.add(Pair(Position(x, y+2), moveColor))

                    if (getIndexFigureOnPos(Position(x+1, y+1)) != null)
                        if (figures[getIndexFigureOnPos(Position(x+1, y+1))!!].team != figure.team)
                            moves.add(Pair(Position(x+1, y+1), attackColor))
                    if (getIndexFigureOnPos(Position(x-1, y+1)) != null)
                        if (figures[getIndexFigureOnPos(Position(x-1, y+1))!!].team != figure.team)
                            moves.add(Pair(Position(x-1, y+1), attackColor))

                    if (figure.pos.y == 5) {
                        for (dx in listOf(-1, 1)) {
                            val ind = getIndexFigureOnPos(Position(x + dx, y))
                            if (ind != null && figures[ind].name == PAWN_NAME && figures[ind].team != figure.team
                                && getIndexFigureOnPos(Position(x+dx, y+1)) == null)
                                moves.add(Pair(Position(x+dx, y+1), attackColor))
                        }
                    }

                }
                if (figure.team == BLACK_TEAM) {
                    if (getIndexFigureOnPos(Position(x, y-1)) == null && y>=2) moves.add(Pair(Position(x, y-1), moveColor))
                    if (getIndexFigureOnPos(Position(x, y-1)) == null &&
                        getIndexFigureOnPos(Position(x, y-2)) == null &&
                        figure.pos.y == 7) moves.add(Pair(Position(x, y-2), moveColor))
                    if (getIndexFigureOnPos(Position(x+1, y-1)) != null)
                        if (figures[getIndexFigureOnPos(Position(x+1, y-1))!!].team != figure.team)
                            moves.add(Pair(Position(x+1, y-1), attackColor))
                    if (getIndexFigureOnPos(Position(x-1, y-1)) != null)
                        if (figures[getIndexFigureOnPos(Position(x-1, y-1))!!].team != figure.team)
                            moves.add(Pair(Position(x-1, y-1), attackColor))

                    if (figure.pos.y == 4) {
                        for (dx in listOf(-1, 1)) {
                            val ind = getIndexFigureOnPos(Position(x + dx, y))
                            if (ind != null && figures[ind].name == PAWN_NAME && figures[ind].team != figure.team
                                && getIndexFigureOnPos(Position(x+dx, y-1)) == null)
                                moves.add(Pair(Position(x+dx, y-1), attackColor))
                        }
                    }
                }
            }
            HORSE_NAME -> {
                val positions = listOf(Position(x+1, y+2), Position(x+1, y-2),
                    Position(x+2, y+1), Position(x+2, y-1), Position(x-1, y+2),
                    Position(x-1, y-2), Position(x-2, y+1), Position(x-2, y-1))
                for (pos in positions) {
                    if (pos.x in 1..8 && pos.y in 1..8){
                        val ind = getIndexFigureOnPos(pos)
                        if (ind == null) moves.add(Pair(pos, moveColor))
                        else if (figures[ind].team != figure.team) moves.add(Pair(pos, attackColor))
                    }
                }
            }
            ROOK_NAME -> {
                for (i in 1..7)
                    if (x+i in 1..8)
                        if (getIndexFigureOnPos(Position(x+i, y)) == null)
                            moves.add(Pair(Position(x+i, y), moveColor))
                        else {
                            if (figures[getIndexFigureOnPos(Position(x+i, y))!!].team != figure.team)
                                moves.add(Pair(Position(x + i, y), attackColor))
                            break
                        }
                for (i in 1..7)
                    if (x-i in 1..8)
                        if (getIndexFigureOnPos(Position(x-i, y)) == null)
                            moves.add(Pair(Position(x-i, y), moveColor))
                        else {
                            if (figures[getIndexFigureOnPos(Position(x-i, y))!!].team != figure.team)
                                moves.add(Pair(Position(x - i, y), attackColor))
                            break
                        }
                for (i in 1..7)
                    if (y+i in 1..8)
                        if (getIndexFigureOnPos(Position(x, y+i)) == null)
                            moves.add(Pair(Position(x, y+i), moveColor))
                        else {
                            if (figures[getIndexFigureOnPos(Position(x, y+i))!!].team != figure.team)
                                moves.add(Pair(Position(x, y+i), attackColor))
                            break
                        }
                for (i in 1..7)
                    if (y-i in 1..8)
                        if (getIndexFigureOnPos(Position(x, y-i)) == null)
                            moves.add(Pair(Position(x, y-i), moveColor))
                        else {
                            if (figures[getIndexFigureOnPos(Position(x, y-i))!!].team != figure.team)
                                moves.add(Pair(Position(x, y-i), attackColor))
                            break
                        }
            }
            BISHOP_NAME -> {
                for (i in 1..7)
                    if (x+i in 1..8 && y+i in 1..8)
                        if (getIndexFigureOnPos(Position(x+i, y+i)) == null)
                            moves.add(Pair(Position(x+i, y+i), moveColor))
                        else {
                            if (figures[getIndexFigureOnPos(Position(x+i, y+i))!!].team != figure.team)
                                moves.add(Pair(Position(x + i, y+i), attackColor))
                            break
                        }
                for (i in 1..7)
                    if (x-i in 1..8 && y+i in 1..8)
                        if (getIndexFigureOnPos(Position(x-i, y+i)) == null)
                            moves.add(Pair(Position(x-i, y+i), moveColor))
                        else {
                            if (figures[getIndexFigureOnPos(Position(x-i, y+i))!!].team != figure.team)
                                moves.add(Pair(Position(x - i, y+i), attackColor))
                            break
                        }
                for (i in 1..7)
                    if (x+i in 1..8 && y-i in 1..8)
                        if (getIndexFigureOnPos(Position(x+i, y-i)) == null)
                            moves.add(Pair(Position(x+i, y-i), moveColor))
                        else {
                            if (figures[getIndexFigureOnPos(Position(x+i, y-i))!!].team != figure.team)
                                moves.add(Pair(Position(x+i, y-i), attackColor))
                            break
                        }
                for (i in 1..7)
                    if (x-i in 1..8 && y-i in 1..8)
                        if (getIndexFigureOnPos(Position(x-i, y-i)) == null)
                            moves.add(Pair(Position(x-i, y-i), moveColor))
                        else {
                            if (figures[getIndexFigureOnPos(Position(x-i, y-i))!!].team != figure.team)
                                moves.add(Pair(Position(x-i, y-i), attackColor))
                            break
                        }
            }
            KING_NAME -> {
                for (dx in -1..1)
                    for (dy in -1..1)
                        if (x+dx in 1..8 && y+dy in 1..8) {
                            val index = getIndexFigureOnPos(Position(x + dx, y + dy))
                            if (index == null)
                                moves.add(Pair(Position(x + dx, y + dy), moveColor))
                            else if (figures[index].team != figure.team)
                                moves.add(Pair(Position(x + dx, y + dy), attackColor))
                        }
                val positions = listOf<Position>(Position(1,1),Position(1,8),
                    Position(8,1),Position(8,8))
                for (pos in positions) {
                    val index = getIndexFigureOnPos(pos) ?: continue
                    // castling
                    if (figures[index].name != ROOK_NAME) continue
                    if (figure.team == WHITE_TEAM && figures[index].pos.y == 1 &&
                        (figures[index].pos.x == 1 || figures[index].pos.x == 8))
                            if (figure.pos == Position(5, 1)) {
                                var clear = true
                                for (dx in minOf(figure.pos.x, pos.x) + 1..<maxOf(figure.pos.x, pos.x))
                                    if (getIndexFigureOnPos(Position(dx, pos.y)) != null) clear = false
                                if (clear) moves.add(Pair(pos, castlingColor))
                            }

                    if (figure.team == BLACK_TEAM && figures[index].pos.y == 8 &&
                        (figures[index].pos.x == 1 || figures[index].pos.x == 8)
                    )
                        if (figure.pos == Position(5, 8)) {
                            var clear = true
                            for (dx in minOf(figure.pos.x, pos.x) + 1..<maxOf(figure.pos.x, pos.x))
                                if (getIndexFigureOnPos(Position(dx, pos.y)) != null) clear = false
                            if (clear) moves.add(Pair(pos, castlingColor))
                        }
                }
            }
            QUEEN_NAME -> {
                for (i in 1..7)
                    if (x+i in 1..8)
                        if (getIndexFigureOnPos(Position(x+i, y)) == null)
                            moves.add(Pair(Position(x+i, y), moveColor))
                        else {
                            if (figures[getIndexFigureOnPos(Position(x+i, y))!!].team != figure.team)
                                moves.add(Pair(Position(x + i, y), attackColor))
                            break
                        }
                for (i in 1..7)
                    if (x-i in 1..8)
                        if (getIndexFigureOnPos(Position(x-i, y)) == null)
                            moves.add(Pair(Position(x-i, y), moveColor))
                        else {
                            if (figures[getIndexFigureOnPos(Position(x-i, y))!!].team != figure.team)
                                moves.add(Pair(Position(x - i, y), attackColor))
                            break
                        }
                for (i in 1..7)
                    if (y+i in 1..8)
                        if (getIndexFigureOnPos(Position(x, y+i)) == null)
                            moves.add(Pair(Position(x, y+i), moveColor))
                        else {
                            if (figures[getIndexFigureOnPos(Position(x, y+i))!!].team != figure.team)
                                moves.add(Pair(Position(x, y+i), attackColor))
                            break
                        }
                for (i in 1..7)
                    if (y-i in 1..8)
                        if (getIndexFigureOnPos(Position(x, y-i)) == null)
                            moves.add(Pair(Position(x, y-i), moveColor))
                        else {
                            if (figures[getIndexFigureOnPos(Position(x, y-i))!!].team != figure.team)
                                moves.add(Pair(Position(x, y-i), attackColor))
                            break
                        }
                for (i in 1..7)
                    if (x+i in 1..8 && y+i in 1..8)
                        if (getIndexFigureOnPos(Position(x+i, y+i)) == null)
                            moves.add(Pair(Position(x+i, y+i), moveColor))
                        else {
                            if (figures[getIndexFigureOnPos(Position(x+i, y+i))!!].team != figure.team)
                                moves.add(Pair(Position(x + i, y+i), attackColor))
                            break
                        }
                for (i in 1..7)
                    if (x-i in 1..8 && y+i in 1..8)
                        if (getIndexFigureOnPos(Position(x-i, y+i)) == null)
                            moves.add(Pair(Position(x-i, y+i), moveColor))
                        else {
                            if (figures[getIndexFigureOnPos(Position(x-i, y+i))!!].team != figure.team)
                                moves.add(Pair(Position(x - i, y+i), attackColor))
                            break
                        }
                for (i in 1..7)
                    if (x+i in 1..8 && y-i in 1..8)
                        if (getIndexFigureOnPos(Position(x+i, y-i)) == null)
                            moves.add(Pair(Position(x+i, y-i), moveColor))
                        else {
                            if (figures[getIndexFigureOnPos(Position(x+i, y-i))!!].team != figure.team)
                                moves.add(Pair(Position(x+i, y-i), attackColor))
                            break
                        }
                for (i in 1..7)
                    if (x-i in 1..8 && y-i in 1..8)
                        if (getIndexFigureOnPos(Position(x-i, y-i)) == null)
                            moves.add(Pair(Position(x-i, y-i), moveColor))
                        else {
                            if (figures[getIndexFigureOnPos(Position(x-i, y-i))!!].team != figure.team)
                                moves.add(Pair(Position(x-i, y-i), attackColor))
                            break
                        }
            }
        }

        return moves
    }

    fun getCoordinates(position: Position): Coordinates {
        return Coordinates(boardCoords!!.first.x+((position.x-1)*cellSize!!),
            boardCoords!!.first.y+((8-position.y)*cellSize!!))
    }

    fun getPosition(coordinates: Coordinates): Position {
        return if (boardCoords!!.first.x < coordinates.x && coordinates.x < boardCoords!!.second.x &&
            boardCoords!!.first.y < coordinates.y && coordinates.y < boardCoords!!.second.y)
            Position(1+((coordinates.x-boardCoords!!.first.x)/cellSize!!).toInt(),
                (9-(coordinates.y-boardCoords!!.first.y)/cellSize!!).toInt())
        else Position(-1, -1)
    }

    companion object {
        const val TAG = "BoardCanvas"
    }

}