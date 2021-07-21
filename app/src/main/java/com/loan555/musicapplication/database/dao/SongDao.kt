package com.loan555.musicapplication.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.loan555.musicapplication.model.SongCustom

@Dao
interface SongDao {
    @Insert
    suspend fun insertSong(song: SongCustom)

    @Update
    suspend fun updatePlaylist(song: SongCustom)

    @Delete
    suspend fun deleteSong(song: SongCustom)

    @Query("select * from songs_favorite_table")
    fun getAllPlaylist(): LiveData<List<SongCustom>>

}