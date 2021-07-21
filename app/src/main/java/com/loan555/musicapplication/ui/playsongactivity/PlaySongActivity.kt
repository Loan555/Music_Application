package com.loan555.musicapplication.ui.playsongactivity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.loan555.musicapplication.R
import com.loan555.musicapplication.service.MusicControllerService
import com.loan555.musicapplication.ui.mainactivity.activity.myTag
import com.loan555.musicapplication.ui.playsongactivity.ui.main.PlaySongFragment

class PlaySongActivity : AppCompatActivity() {

    private lateinit var viewModel : PlaySongViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.play_song_activity)
        viewModel = ViewModelProvider(this).get(PlaySongViewModel::class.java)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, PlaySongFragment.newInstance())
                .commitNow()
        }
    }

    override fun onStart() {
        super.onStart()
        val intentService = Intent(this, MusicControllerService::class.java)
        bindService(intentService, viewModel.conn, Context.BIND_AUTO_CREATE)
        registerReceiver(viewModel.br, viewModel.filter)
        viewModel.mBound.observe(this, {
                Log.d(myTag,"mBound = $it")
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(viewModel.br)
        unbindService(viewModel.conn)
    }
}