package com.example.chessonline.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface FigureDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFigure(figure: FigureEntity)

    @Update
    suspend fun updateFigure(figure: FigureEntity)

    @Query("DELETE FROM figure_table WHERE x==:x AND y==:y")
    suspend fun removeFigure(x: Int, y: Int)

    @Query("SELECT * FROM figure_table")
    suspend fun getFiguresList(): List<FigureEntity>

    @Query("SELECT * FROM figure_table WHERE x==:x AND y==:y LIMIT 1")
    suspend fun getFigureOnPosition(x: Int, y: Int): FigureEntity


}