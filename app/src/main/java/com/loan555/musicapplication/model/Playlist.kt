package com.loan555.musicapplication.model

import java.io.Serializable

data class Playlist(val id: String, var name: String,val img: Int?, var songs: ArrayList<SongCustom>): Serializable
