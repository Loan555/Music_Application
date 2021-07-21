package com.loan555.musicapplication.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.loan555.musicapplication.database.SongDatabase
import com.loan555.musicapplication.database.dao.SongDao
import com.loan555.musicapplication.model.SongCustom

class SongRepository(application: Application) {
    private val songDao: SongDao
    init {
        val songDatabase : SongDatabase = SongDatabase.getInstance(application)
        songDao =songDatabase.getSongDao()
    }

}