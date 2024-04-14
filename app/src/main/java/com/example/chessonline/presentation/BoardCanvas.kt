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
    private lateinit var boardCoords: Pair<Coordinates, Coordinates>
    private var cellSize: Float = 10F
    private var team = WHITE_TEAM
    private var position = Position(1, 2)
    var figures = getStartFigures(team)
    var chosenPos: Position = Position(-1, -1)


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        boardCoords = BoardData.getBoardCoordinates(width, height)
        cellSize = BoardData.getCellSize(width)

        drawBoardGrid(canvas)

        for (fig in figures) drawFigure(fig, canvas)

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.d(TAG, getPosition(Coordinates(event!!.x, event.y)).toString())
        position = getPosition(Coordinates(event.x, event.y))
        Log.d(TAG, "ChosenPos: $chosenPos")

        if (chosenPos == Position(-1, -1) && getIndexFigureOnPos(position) != null) {
            chosenPos = position
            invalidate()
        } else {
            val indChosen = getIndexFigureOnPos(chosenPos)
            val indPos = getIndexFigureOnPos(position)

            if (indChosen != null && canFigureGo(figures[indChosen], position)) {
                Log.d("XXXX", "Move ${figures[indChosen]} to $position")
                figures[indChosen].position = position
                if (indPos != null)
                    figures.removeAt(indPos)
                invalidate() // Обновление canvas
            }
            chosenPos = Position(-1, -1)

        }
        if (getIndexFigureOnPos(position) == null) {
            chosenPos = Position(-1, -1)
            invalidate()
        }

        return super.onTouchEvent(event)
    }

    private fun getIndexFigureOnPos(position: Position): Int? {
        for (fig in figures) {
            if (fig.position == position) {
//                Log.d("XXXXX", "Figure on $position: ${figures[figures.indexOf(fig)]}")
                return figures.indexOf(fig)
            }
        }
        return null
    }

    private fun drawBoardGrid(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.BLACK
        paint.strokeWidth = 5F

        val boardStart = boardCoords.first
        val boardEnd = boardCoords.second
        for (i in 0..8) {
            canvas.drawLine(boardStart.x+i*cellSize, boardStart.y, boardStart.x+i*cellSize, boardEnd.y, paint)
            canvas.drawLine(boardStart.x, boardStart.y+i*cellSize, boardEnd.x, boardStart.y+i*cellSize, paint)
        }
        if (chosenPos != Position(-1, -1)) {
            val possibleMoves = getPossibleMoves(figures[getIndexFigureOnPos(chosenPos)!!])
            drawColoredCell(chosenPos, "#CCFFFF", canvas)
            for (move in possibleMoves) {
                drawColoredCell(move.first, move.second, canvas)
            }
        }
        Log.d(TAG, "Done drawing board!")
    }

    private fun drawColoredCell(position: Position, hexColor: String, canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.parseColor(hexColor)
        paint.style = Paint.Style.FILL
        val coords = getCoordinates(position)
        canvas.drawRect(coords.x, coords.y, coords.x+cellSize, coords.y+cellSize, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5F
        paint.color = Color.BLACK
        canvas.drawRect(coords.x, coords.y, coords.x+cellSize, coords.y+cellSize, paint)
    }

    fun getStartFigures(team: String): MutableList<Figure> {
        val figures = mutableListOf<Figure>()
        val enemyTeam = if (team == WHITE_TEAM) {
            BLACK_TEAM
        } else WHITE_TEAM
        var id = 1

        for (x in 1..8) figures.add(Figure(PAWN_NAME, team, Position(x,2), id++))
        figures.add(Figure(ROOK_NAME, team, Position(1, 1), id++))
        figures.add(Figure(ROOK_NAME, team, Position(8, 1), id++))
        figures.add(Figure(HORSE_NAME, team, Position(2, 1), id++))
        figures.add(Figure(HORSE_NAME, team, Position(7, 1), id++))
        figures.add(Figure(BISHOP_NAME, team, Position(3, 1), id++))
        figures.add(Figure(BISHOP_NAME, team, Position(6, 1), id++))
        figures.add(Figure(QUEEN_NAME, team, Position(4, 1), id++))
        figures.add(Figure(KING_NAME, team, Position(5, 1), id++))

        for (x in 1..8) figures.add(Figure(PAWN_NAME, enemyTeam, Position(x,7), id++))
        figures.add(Figure(ROOK_NAME, enemyTeam, Position(1, 8), id++))
        figures.add(Figure(ROOK_NAME, enemyTeam, Position(8, 8), id++))
        figures.add(Figure(HORSE_NAME, enemyTeam, Position(2, 8), id++))
        figures.add(Figure(HORSE_NAME, enemyTeam, Position(7, 8), id++))
        figures.add(Figure(BISHOP_NAME, enemyTeam, Position(3, 8), id++))
        figures.add(Figure(BISHOP_NAME, enemyTeam, Position(6, 8), id++))
        figures.add(Figure(QUEEN_NAME, enemyTeam, Position(4, 8), id++))
        figures.add(Figure(KING_NAME, enemyTeam, Position(5, 8), id))

        return figures
    }

    @SuppressLint("DiscouragedApi")
    fun drawFigure(figure: Figure, canvas: Canvas) {
        val resId = context.resources.getIdentifier("${figure.team}_${figure.name}", "drawable", context.packageName)

        val bitmap = BitmapFactory.decodeResource(resources, resId)
        val drawable = BitmapDrawable(resources, bitmap)
        val coordiantes: Coordinates = getCoordinates(figure.position)
        drawable.setBounds((coordiantes.x).toInt(), (coordiantes.y).toInt(),
            (coordiantes.x+cellSize).toInt(), (coordiantes.y+cellSize).toInt())
        drawable.draw(canvas)
        Log.d(TAG, "Done drawing ${figure.team}_${figure.name}!")
    }

    fun canFigureGo(figure: Figure, pos: Position): Boolean {
        Log.d("XXXX", "CanFigureGo: $figure to $pos")

        val index = getIndexFigureOnPos(pos)
        Log.d("XXXX", "Figure on pos: ${figures.getOrNull(index?: 100)}")

        if (index != null)
            if (figure.name != KING_NAME && figure.team == figures[index].team) {
                Log.d("XXXX", "same teams! WRONG move")
                return false
            }

        when (figure.name) {
            PAWN_NAME -> {
                // Attack
                if (pos.y == figure.position.y + 1 && abs(pos.x - figure.position.x) == 1 &&
                    index != null && figure.team == WHITE_TEAM) return true
                if (pos.y == figure.position.y - 1 && abs(pos.x - figure.position.x) == 1 &&
                    index != null && figure.team == BLACK_TEAM) return true

                // Move
                if (pos.x == figure.position.x){
                    if (figure.team == WHITE_TEAM) {
                        if (pos.y == figure.position.y + 1 && index == null) return true
                        if (pos.y == figure.position.y + 2 && figure.position.y == 2 &&
                            index == null &&
                            getIndexFigureOnPos(Position(pos.x, pos.y-1)) == null) return true
                    } else {
                        if (pos.y == figure.position.y - 1 && getIndexFigureOnPos(pos) == null) return true
                        if (pos.y == figure.position.y - 2 && figure.position.y == 7 &&
                            index == null &&
                            getIndexFigureOnPos(Position(pos.x, pos.y+1)) == null) return true
                    }
                }
            }
            HORSE_NAME -> {
                if ((figure.position.x == pos.x + 1 || figure.position.x == pos.x - 1) &&
                    (figure.position.y == pos.y + 2 || figure.position.y == pos.y - 2))
                    return true
                if ((figure.position.x == pos.x + 2 || figure.position.x == pos.x - 2) &&
                    (figure.position.y == pos.y + 1 || figure.position.y == pos.y - 1))
                    return true
            }
            ROOK_NAME -> {
                if (figure.position.x == pos.x) {
                    for (y in minOf(figure.position.y, pos.y)+1..<maxOf(figure.position.y, pos.y))
                        if (getIndexFigureOnPos(Position(pos.x, y)) != null) return false
                    return true
                }
                if (figure.position.y == pos.y) {
                    for (x in minOf(figure.position.x, pos.x)+1..<maxOf(figure.position.x, pos.x))
                        if (getIndexFigureOnPos(Position(x, pos.y)) != null) return false
                    return true
                }
            }
            BISHOP_NAME -> {
                if (abs(figure.position.x - pos.x) == abs(figure.position.y - pos.y)) {
                    val dx = sign((pos.x - figure.position.x).toDouble()).toInt()
                    val dy = sign((pos.y - figure.position.y).toDouble()).toInt()
                    for (i in 1..<abs(pos.x - figure.position.x))
                        if (getIndexFigureOnPos(Position(figure.position.x + dx, figure.position.y + dy)) != null)
                            return false
                    return true
                }
            }
            KING_NAME -> {
                if (abs(figure.position.x - pos.x) <= 1 && abs(figure.position.y - pos.y) <= 1)
                    if (getIndexFigureOnPos(position) == null) return true
                val index = getIndexFigureOnPos(pos)
                if (index != null)
                    if (figures[index].name == ROOK_NAME) {
                        if (figure.team == WHITE_TEAM && figures[index].position.y == 1 &&
                            (figures[index].position.x == 1 || figures[index].position.x == 8))
                            if (figure.position == Position(5, 1)) {
                                for (x in minOf(figure.position.x, pos.x)+1..<maxOf(figure.position.x, pos.x))
                                    if (getIndexFigureOnPos(Position(x, pos.y)) != null) return false
                                return true
                            }
                        if (figure.team == BLACK_TEAM && figures[index].position.y == 8 &&
                            (figures[index].position.x == 1 || figures[index].position.x == 8))
                            if (figure.position == Position(5, 8)) {
                                for (x in minOf(figure.position.x, pos.x)+1..<maxOf(figure.position.x, pos.x))
                                    if (getIndexFigureOnPos(Position(x, pos.y)) != null) return false
                                return true
                            }
                    }
            }
            QUEEN_NAME -> {
                if (abs(figure.position.x - pos.x) == abs(figure.position.y - pos.y)) {
                    val dx = sign((pos.x - figure.position.x).toDouble()).toInt()
                    val dy = sign((pos.y - figure.position.y).toDouble()).toInt()
                    for (i in 1..<abs(pos.x - figure.position.x))
                        if (getIndexFigureOnPos(Position(figure.position.x + dx, figure.position.y + dy)) != null)
                            return false
                    return true
                }
                if (figure.position.x == pos.x) {
                    for (y in minOf(figure.position.y, pos.y)+1..<maxOf(figure.position.y, pos.y))
                        if (getIndexFigureOnPos(Position(pos.x, y)) != null) return false
                    return true
                }
                if (figure.position.y == pos.y) {
                    for (x in minOf(figure.position.x, pos.x)+1..<maxOf(figure.position.x, pos.x))
                        if (getIndexFigureOnPos(Position(x, pos.y)) != null) return false
                    return true
                }
            }
        }
        return false
    }

    fun getPossibleMoves(figure: Figure): MutableList<Pair<Position, String>> {
        val moves = mutableListOf<Pair<Position, String>>()
        val moveColor = "#99FF00"
        val attackColor = "#FF3333"
        val castlingColor = "#B8860B"

        var x = figure.position.x
        var y = figure.position.y

        when (figure.name) {
            PAWN_NAME -> {
                if (figure.team == WHITE_TEAM) {
                    if (getIndexFigureOnPos(Position(x, y+1)) == null) moves.add(Pair(Position(x, y+1), moveColor))
                    if (getIndexFigureOnPos(Position(x, y+1)) == null &&
                        getIndexFigureOnPos(Position(x, y+2)) == null &&
                        figure.position.y == 2) moves.add(Pair(Position(x, y+2), moveColor))

                    if (getIndexFigureOnPos(Position(x+1, y+1)) != null)
                        if (figures[getIndexFigureOnPos(Position(x+1, y+1))!!].team != figure.team)
                            moves.add(Pair(Position(x+1, y+1), attackColor))
                    if (getIndexFigureOnPos(Position(x-1, y+1)) != null)
                        if (figures[getIndexFigureOnPos(Position(x-1, y+1))!!].team != figure.team)
                            moves.add(Pair(Position(x-1, y+1), attackColor))
                }
                if (figure.team == BLACK_TEAM) {
                    if (getIndexFigureOnPos(Position(x, y-1)) == null) moves.add(Pair(Position(x, y-1), moveColor))
                    if (getIndexFigureOnPos(Position(x, y-1)) == null &&
                        getIndexFigureOnPos(Position(x, y-2)) == null &&
                        figure.position.y == 7) moves.add(Pair(Position(x, y-2), moveColor))
                    if (getIndexFigureOnPos(Position(x+1, y-1)) != null)
                        if (figures[getIndexFigureOnPos(Position(x+1, y-1))!!].team != figure.team)
                            moves.add(Pair(Position(x+1, y-1), attackColor))
                    if (getIndexFigureOnPos(Position(x-1, y-1)) != null)
                        if (figures[getIndexFigureOnPos(Position(x-1, y-1))!!].team != figure.team)
                            moves.add(Pair(Position(x-1, y-1), attackColor))
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
                        if (x+dx in 1..8 && y+dy in 1..8)
                            if (getIndexFigureOnPos(Position(x+dx, y+dy)) == null)
                                moves.add(Pair(Position(x+dx, y+dy), moveColor))
                val positions = listOf<Position>(Position(1,1),Position(1,8),
                    Position(8,1),Position(8,8))
                for (pos in positions) {
                    val index = getIndexFigureOnPos(pos)
                    if (index != null)
                        if (figures[index].name == ROOK_NAME) {
                            if (figure.team == WHITE_TEAM && figures[index].position.y == 1 &&
                                (figures[index].position.x == 1 || figures[index].position.x == 8))
                                if (figure.position == Position(5, 1)) {
                                    var clear = true
                                    for (x in minOf(figure.position.x, pos.x) + 1..<maxOf(figure.position.x, pos.x))
                                        if (getIndexFigureOnPos(Position(x, pos.y)) != null) clear = false
                                    if (clear) moves.add(Pair(pos, castlingColor))
                                }
                            if (figure.team == BLACK_TEAM && figures[index].position.y == 8 &&
                                (figures[index].position.x == 1 || figures[index].position.x == 8)
                            )
                                if (figure.position == Position(5, 8)) {
                                    var clear = true
                                    for (x in minOf(figure.position.x, pos.x) + 1..<maxOf(figure.position.x, pos.x))
                                        if (getIndexFigureOnPos(Position(x, pos.y)) != null) clear = false
                                    if (clear) moves.add(Pair(pos, castlingColor))
                                }
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

    private fun getCoordinates(position: Position): Coordinates {
        return Coordinates(boardCoords.first.x+((position.x-1)*cellSize), boardCoords.first.y+((8-position.y)*cellSize))
    }

    private fun getPosition(coordinates: Coordinates): Position {
        return if (boardCoords.first.x < coordinates.x && coordinates.x < boardCoords.second.x &&
            boardCoords.first.y < coordinates.y && coordinates.y < boardCoords.second.y)
            Position(1+((coordinates.x-boardCoords.first.x)/cellSize).toInt(),
                (9-(coordinates.y-boardCoords.first.y)/cellSize).toInt())
        else Position(-1, -1)
    }

    companion object {
        const val TAG = "BoardCanvas"
    }

}