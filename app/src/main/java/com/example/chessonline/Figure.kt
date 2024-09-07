package com.example.chessonline

data class Figure(
    var name: String,
    val team: String,
    var pos: Position,
    val id: Int = 0,
) {
    override fun toString(): String {
        return "${team}_$name $pos"
    }
}

data class Position(
    var x: Int = -1,
    var y: Int = -1,
) {
    override fun toString(): String {
        return "($x, $y)"
    }
}

data class Coordinates(
    val x: Float,
    val y: Float
)

data class Game(
    var started: Boolean,
    var team: String,
    var turn: String,
    var channel: Int = 0
)

data class Move(
    val fromPos: Position,
    val toPos: Position,
    val whiteTimer: Long,
    val blackTimer: Long,
)