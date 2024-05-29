package com.example.chessonline.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "figure_table")
data class FigureEntity (
    @ColumnInfo(name="figure_name")
    val name: String,
    @ColumnInfo(name="figure_team")
    val team: String,
    @Embedded
    val position: PositionEntity,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)

data class PositionEntity (
    var x: Int,
    var y: Int
)
