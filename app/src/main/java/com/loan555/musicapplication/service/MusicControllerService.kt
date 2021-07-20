package com.loan555.musicapplication.service

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.loan555.musicapplication.R
import com.loan555.musicapplication.model.SongCustom
import com.loan555.musicapplication.ui.mainactivity.activity.myTag
import kotlinx.coroutines.*
import java.io.IOException

const val ACTION_PAUSE = -1
const val ACTION_RESUME = 1
const val ACTION_NEXT = 2
const val ACTION_BACK = -2
const val ACTION_PLAY = 3
const val ACTION_STOP = -3
const val ACTION_PLAY_PAUSE = 4

const val CHANNEL_ID = "channel_music_app"
const val ONGOING_NOTIFICATION_ID = 1
const val ACTION_MUSIC = "android.intent.action.MY_MUSIC_ACTION"
const val ACTION_MUSIC_NOTIFICATION = "android.intent.action.MY_MUSIC_ACTION_NOTY"
const val KEY_ACTION_MUSIC = "action_music"

class MusicControllerService : Service() {
    var isPlaying = false
    var statePlay: Int = 0
    private lateinit var br: BroadcastReceiver
    private val binder = MusicControllerBinder()
    var player: MediaPlayer? = null
    lateinit var songIDPlaying: String
    var songPos: Int = -1
    var songs = ArrayList<SongCustom>()
    var looping = false
    private lateinit var prevPendingIntent: PendingIntent
    private lateinit var pausePendingIntent: PendingIntent
    private lateinit var nextPendingIntent: PendingIntent
    private lateinit var stopPendingIntent: PendingIntent
    var listPlaying = ""

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class MusicControllerBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): MusicControllerService = this@MusicControllerService
    }

    override fun onCreate() {
        Log.d("aaa", "service onCreate")
        super.onCreate()
        val filter = IntentFilter(ACTION_MUSIC)
        br = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.getIntExtra(KEY_ACTION_MUSIC, 0)!!
                Log.d(myTag, "onReceive service ${intent?.action} ------------- $action")
                handAction(action)
                if (action != ACTION_STOP)// gap may su kien nay thi moi update notification
                    Intent(this@MusicControllerService, MusicControllerService::class.java).also {
                        startService(it)
                    }
            }
        }
        registerReceiver(br, filter)
        val prevIntent = Intent(ACTION_MUSIC).apply {
            putExtra(KEY_ACTION_MUSIC, ACTION_BACK)
        }
        prevPendingIntent = PendingIntent.getBroadcast(
            this,
            ACTION_BACK,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val nextIntent = Intent(ACTION_MUSIC).apply {
            putExtra(KEY_ACTION_MUSIC, ACTION_NEXT)
        }
        nextPendingIntent = PendingIntent.getBroadcast(
            this,
            ACTION_NEXT,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = Intent(ACTION_MUSIC).apply {
            putExtra(KEY_ACTION_MUSIC, ACTION_STOP)
        }
        stopPendingIntent = PendingIntent.getBroadcast(
            this,
            ACTION_STOP,
            stopIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val pauseIntent = Intent(ACTION_MUSIC).apply {
            putExtra(KEY_ACTION_MUSIC, ACTION_PLAY_PAUSE)
        }
        pausePendingIntent = PendingIntent.getBroadcast(
            this,
            ACTION_PLAY_PAUSE,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(myTag, "service onBind")
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(myTag, "service onStartCommand")
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, flags)
        val mediaSession = MediaSessionCompat(this, "tag")
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            // Show controls on lock screen even when user hides sensitive content.
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentTitle(songs[songPos].title)
            .setContentText(songs[songPos].artistsNames)
            .setSmallIcon(R.drawable.ic_music_note)
            // Add media control buttons that invoke intents in your media service
            .addAction(
                R.drawable.ic_skip_previous,
                "Previous",
                prevPendingIntent
            ) // #0
            .addAction(getBtnImg(), "Pause", pausePendingIntent) // #1
            .addAction(
                R.drawable.ic_skip_next,
                "Next",
                nextPendingIntent
            ) // #2
            .addAction(R.drawable.ic_close, "Stop", stopPendingIntent) // #3
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2 /* #1: pause button \*/)
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setSound(null)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(ONGOING_NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    private fun getBtnImg(): Int {
        return if (isPlaying) {
            R.drawable.ic_pause
        } else R.drawable.ic_play
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
        listPlaying = ""
        unregisterReceiver(br)
        Log.d(myTag, "service onDestroy")
    }

    /**     music controller */
    fun playSong(position: Int) {
        Log.d(myTag, "service playSong")
        songPos = position
        val uri: Uri = Uri.parse(songs[position].uri)
        player?.release()
        player = MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                setDataSource(applicationContext, uri)
            }catch (e: IOException){
                Log.e(myTag,"error play media: ${e.message}")
            }
            setOnCompletionListener {
                playNext()
            }
            isLooping = looping
            prepareAsync()
            setOnPreparedListener { start() }
            setOnErrorListener { mp, _, _ ->
                mp.reset()
                Log.e(myTag,"error get data")
                false
            }
        }
        isPlaying = true
        Intent(this, MusicControllerService::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startService(it)
        }
        sentMyBroadcast(ACTION_PLAY)
        songIDPlaying = songs[songPos].id
    }

    private fun handAction(action: Int) {
        when (action) {
            ACTION_PAUSE -> {
                pausePlayer()
            }
            ACTION_RESUME -> {
                resumePlayer()
            }
            ACTION_BACK -> {
                playPrev()
            }
            ACTION_NEXT -> {
                playNext()
            }
            ACTION_STOP -> {
                stopSelf()
            }
            ACTION_PLAY_PAUSE -> {
                when (isPng()) {
                    true -> handAction(ACTION_PAUSE)
                    false -> handAction(ACTION_RESUME)
                }
            }
        }
    }

    private fun sentMyBroadcast(action: Int) {
        val intent = Intent().also {
            it.action = ACTION_MUSIC
            it.putExtra(KEY_ACTION_MUSIC, action)
        }
        sendBroadcast(intent)
    }

    private fun playNext(): String? {
        Toast.makeText(this, "next", Toast.LENGTH_SHORT).show()
        songPos++;
        if (songPos == songs.size) songPos = 0
        this.playSong(songPos)
        songIDPlaying = songs[songPos].uri
        return songIDPlaying
    }

    private fun playPrev(): String? {
        Toast.makeText(this, "back", Toast.LENGTH_SHORT).show()
        songPos--
        if (songPos == -1) songPos = songs.size - 1;
        this.playSong(songPos)
        songIDPlaying = songs[songPos].uri
        return songIDPlaying
    }

    fun getPos(): Int? {
        return player?.currentPosition
    }

    fun getDur(): Int? {
        return player?.duration
    }

    fun isPng(): Boolean? {
        return player?.isPlaying
    }

    private fun pausePlayer() {
        player?.pause()
        isPlaying = false
    }

    private fun resumePlayer() {
        player?.start()
        isPlaying = true
    }

    fun seek(position: Int) {
        player?.seekTo(position)
    }

    fun callServiceDemo(str: String) {
        Log.d(myTag, " call service demo $str")
    }
}