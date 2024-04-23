package com.example.chessonline.presentation

import android.util.Log
import com.example.chessonline.Position

object MessageParser {
    fun parseMoveMessage(message: String): Pair<Position, Position> {
        val move = message.subSequence(5, message.length)
        val fromPos = move.split(" to ")[0].split(",").map {it.toInt()}
        val toPos = move.split(" to ")[1].split(",").map {it.toInt()}
        return Pair(Position(fromPos[0], fromPos[1]), Position(toPos[0], toPos[1]))
    }

    fun parseLookingMessage(message: String): Int {
        return message.split(":")[1].toInt()
    }
}