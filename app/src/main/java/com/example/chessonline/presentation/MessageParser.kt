package com.example.chessonline.presentation

import com.example.chessonline.Move
import com.example.chessonline.Position

object MessageParser {
    fun parseMoveMessage(message: String): Move {
        val moveData = message.split("\n")[0].split("MOVE ")[1]
        val timeData = message.split("\n")[1].split("TIME ")[1]
        val fromPos = moveData.split(" to ")[0].split(",").map {it.toInt()}
        val toPos = moveData.split(" to ")[1].split(",").map {it.toInt()}
        val whiteTimer = timeData.split(",")[0].toLong()
        val blackTimer = timeData.split(",")[1].toLong()

        return Move(fromPos = Position(fromPos[0], fromPos[1]),
                    toPos = Position(toPos[0], toPos[1]),
                    whiteTimer = whiteTimer,
                    blackTimer = blackTimer)
    }

    fun parseFallMessage(message: String): Position {
        val moveData = message.split("FALL ")[1]
        val pos = moveData.split(" to ")[0].split(",").map {it.toInt()}
        return Position(pos[0], pos[1])
    }
}