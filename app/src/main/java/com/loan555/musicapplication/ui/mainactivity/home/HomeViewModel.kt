package com.loan555.musicapplication.ui.mainactivity.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.loan555.musicapplication.model.Playlist

class HomeViewModel : ViewModel() {
    private var _allPlaylist = MutableLiveData<ArrayList<Playlist>>().apply {
        value = ArrayList()
    }

    val allPlaylist: LiveData<ArrayList<Playlist>> = _allPlaylist

    fun setAllPlaylist(list: MutableLiveData<ArrayList<Playlist>>) {
        _allPlaylist = list
    }
}