package com.loan555.musicapplication.ui.createplaylistactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.loan555.musicapplication.R
import com.loan555.musicapplication.ui.createplaylistactivity.ui.main.CreatePlaylistFragment

class CreatePlaylistActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_playlist_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, CreatePlaylistFragment.newInstance())
                .commitNow()
        }
    }
}