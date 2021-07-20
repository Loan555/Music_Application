package com.loan555.musicapplication.ui.playsongactivity.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlaySongModel : ViewModel() {
private val _textDemo = MutableLiveData<String>().apply { value = "" }
    val textDemo : LiveData<String> = _textDemo

    fun setText(str: String){
        _textDemo.value = str
    }
}