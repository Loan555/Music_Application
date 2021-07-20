package com.loan555.musicapplication.ui.playactivity.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.loan555.musicapplication.model.Playlist

class PlayViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply { value = "" }
    private val _size = MutableLiveData<String>().apply { value = "" }
    private val _playList = MutableLiveData<Playlist>().apply { value = null }

    val text: LiveData<String> = _text
    val size: LiveData<String> = _size
    val playlist: LiveData<Playlist> = _playList

    fun setText(str: String) {
        _text.value = str
    }

    fun setList(list: Playlist) {
        _playList.value = list
        _size.value = "${list.songs.size} bài hát"
    }
}