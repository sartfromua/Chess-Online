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
import androidx.lifecycle.ViewModelProvider
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

        if (chosenPos == Position(-1, -1)) chosenPos = position
        else {
            val index = getIndexFigureOnPos(chosenPos)
            chosenPos = Position(-1, -1)
            // TODO: FigureCanGO
            if (index != null) {
                figures[index].position = position
                invalidate() // Обновление canvas
            }
        }

        return super.onTouchEvent(event)
    }

    private fun getIndexFigureOnPos(position: Position): Int? {
        for (fig in figures) {
            if (fig.position == position) return figures.indexOf(fig)
        }
        return null
    }

    fun drawBoardGrid(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.BLACK
        paint.strokeWidth = 5F

        val boardStart = boardCoords.first
        val boardEnd = boardCoords.second
        for (i in 0..8) {
            canvas.drawLine(boardStart.x+i*cellSize, boardStart.y, boardStart.x+i*cellSize, boardEnd.y, paint)
            canvas.drawLine(boardStart.x, boardStart.y+i*cellSize, boardEnd.x, boardStart.y+i*cellSize, paint)
        }
        Log.d(TAG, "Done drawing board!")
    }

    fun getStartFigures(team: String): List<Figure> {
        val figures = mutableListOf<Figure>()
        val enemyTeam = if (team == WHITE_TEAM) {
            BLACK_TEAM
        } else WHITE_TEAM

        for (x in 1..8) figures.add(Figure(PAWN_NAME, team, Position(x,2)))
        figures.add(Figure(ROOK_NAME, team, Position(1, 1)))
        figures.add(Figure(ROOK_NAME, team, Position(8, 1)))
        figures.add(Figure(HORSE_NAME, team, Position(2, 1)))
        figures.add(Figure(HORSE_NAME, team, Position(7, 1)))
        figures.add(Figure(BISHOP_NAME, team, Position(3, 1)))
        figures.add(Figure(BISHOP_NAME, team, Position(6, 1)))
        figures.add(Figure(QUEEN_NAME, team, Position(5, 1)))
        figures.add(Figure(KING_NAME, team, Position(4, 1)))

        for (x in 1..8) figures.add(Figure(PAWN_NAME, enemyTeam, Position(x,7)))
        figures.add(Figure(ROOK_NAME, enemyTeam, Position(1, 8)))
        figures.add(Figure(ROOK_NAME, enemyTeam, Position(8, 8)))
        figures.add(Figure(HORSE_NAME, enemyTeam, Position(2, 8)))
        figures.add(Figure(HORSE_NAME, enemyTeam, Position(7, 8)))
        figures.add(Figure(BISHOP_NAME, enemyTeam, Position(3, 8)))
        figures.add(Figure(BISHOP_NAME, enemyTeam, Position(6, 8)))
        figures.add(Figure(QUEEN_NAME, enemyTeam, Position(4, 8)))
        figures.add(Figure(KING_NAME, enemyTeam, Position(5, 8)))

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

    fun moveFigureTo(figure: Figure, position: Position, canvas: Canvas) {
        if (figureCanGo(figure, position)) {
            val coords = getCoordinates(figure.position)
            val paint = Paint()
            paint.color = Color.parseColor("#fef5ff")
            paint.style = Paint.Style.FILL
            canvas.drawRect(coords.x, coords.y, coords.x+cellSize, coords.y+cellSize, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 5F
            paint.color = Color.BLACK
            canvas.drawRect(coords.x, coords.y, coords.x+cellSize, coords.y+cellSize, paint)
            drawFigure(figure, canvas)
        }
    }

    // TODO: figureCanGo
    fun figureCanGo(figure: Figure, position: Position): Boolean {
        return true
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