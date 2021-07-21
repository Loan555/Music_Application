package com.loan555.musicapplication.ui.playsongactivity

import android.app.Application
import android.content.*
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.util.Size
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.loan555.musicapplication.model.SongCustom
import com.loan555.musicapplication.repository.SongRepository
import com.loan555.musicapplication.service.*
import com.loan555.musicapplication.ui.mainactivity.activity.ACTION_MUSIC
import com.loan555.musicapplication.ui.mainactivity.activity.myTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.Exception

class PlaySongViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = SongRepository(application)
    lateinit var mService: MusicControllerService

    val mBinder: MutableLiveData<MusicControllerService.MusicControllerBinder?> =
        MutableLiveData<MusicControllerService.MusicControllerBinder?>()
    private val _mBound = MutableLiveData<Boolean>().apply { value = false }
    var mBound: LiveData<Boolean> = _mBound

    val conn = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            Log.d(myTag, "onServiceConnected")
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as MusicControllerService.MusicControllerBinder
            mBinder.value = binder
            _mBound.value = true
            mService = binder.getService()
            if (binder.getService().songs.size > 0) {
                setSong(mService.songs[mService.songPos])
                setIsPlaying(mService.isPlaying)
                getBitmap(mService.songs[mService.songPos])
                _statePlay.value = mService.statePlay
            }
            handSeekBar()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBinder.postValue(null)
            _mBound.value = false
        }
    }

    val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION).apply {
        addAction(ACTION_MUSIC)
    }

    val br: BroadcastReceiver = object : BroadcastReceiver() { //lang nghe service
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.getIntExtra(KEY_ACTION_MUSIC, 0)
            val bundle = intent?.getBundleExtra("SongBundle")
            var song: SongCustom? = null
            if (bundle != null) {
                song = bundle.getSerializable("Song") as SongCustom
            }
            Log.d(myTag, "onReceive activity ${intent?.action} ------------- $action")
            if (action != null) {
                handActionMusic(action)// lang nghe broast cast de cap nhat giao dien
            }
        }
    }

    private fun handActionMusic(action: Int) {
        when (action) {
            ACTION_RESUME -> {
                setIsPlaying(true)
            }
            ACTION_PAUSE -> {
                setIsPlaying(false)
            }
            ACTION_STOP -> {

            }
            ACTION_PLAY, ACTION_NEXT, ACTION_BACK -> {
                Log.d(myTag, "ACTION_PLAY activity $ACTION_PLAY")
                val songPlaying = mService.songs[mService.songPos]
                setSong(songPlaying)
                setIsPlaying(true)
                if (mService.songs[mService.songPos].thumbnail != null) {
                    try {
                        GlobalScope.launch(Dispatchers.Main) {
                            val result = async(Dispatchers.IO) {
                                return@async songPlaying.getBitmapFromURL()
                            }
                            setImg(result.await())
                        }
                    } catch (e: IOException) {
                        // Log exception
                    }
                } else {
                    setImg(songPlaying.getBitmapFromURI(getApplication()))
                }
            }
            ACTION_PLAY_PAUSE -> {
                if (isPlaying.value == false) handActionMusic(ACTION_RESUME)
                else handActionMusic(ACTION_PAUSE)
            }
        }
    }

    fun getBitmap(song: SongCustom) {
        if (song.thumbnail == null) {
            setImg(song.getBitmapFromURI(getApplication()))
        } else {
            GlobalScope.launch(Dispatchers.Main) {
                val result = async(Dispatchers.IO) {
                    return@async song.getBitmapFromURL()
                }
                setImg(result.await())
            }
        }
    }

    fun sentActionMusic(action: Int) {
        when (action) {
            0 -> {
            }
            ACTION_PLAY_PAUSE -> {
                if (mService.player != null)
                    controlMusic(ACTION_PLAY_PAUSE)
            }
            ACTION_BACK, ACTION_NEXT -> {
                if (mService.player != null) {
                    controlMusic(action)
                }
            }
        }
    }

    private fun controlMusic(action: Int) {
        val intent = Intent().also {
            it.action = ACTION_MUSIC
            it.putExtra(KEY_ACTION_MUSIC, action)
        }
        mService.sendBroadcast(intent)
    }

    fun loopClick() {
        var newState = statePlay.value
        if (newState != null)
            setStatePlay(newState + 1)
    }

    private val _title = MutableLiveData<String>().apply { value = "" }
    private val _artistsNames = MutableLiveData<String>().apply { value = "" }
    private val _timeMax = MutableLiveData<String>().apply { value = "00:00" }
    private val _timeCurrent = MutableLiveData<String>().apply { value = "00:00" }
    private val _isPlaying = MutableLiveData<Boolean>().apply { value = false }
    private val _imgPlaying = MutableLiveData<Bitmap?>().apply { value = null }
    private var _statePlay = MutableLiveData<Int>().apply { value = 0 }
    private val _duration = MutableLiveData<Int>().apply { value = 0 }
    private val _current = MutableLiveData<Int>().apply { value = 0 }

    val title: LiveData<String> = _title
    val artistsNames: LiveData<String> = _artistsNames
    val timeMax: LiveData<String> = _timeMax
    val timeCurrent: LiveData<String> = _timeCurrent
    val isPlaying: LiveData<Boolean> = _isPlaying
    val imgPlaying: LiveData<Bitmap?> = _imgPlaying
    val statePlay: LiveData<Int> = _statePlay
    val duration: LiveData<Int> = _duration
    val current: LiveData<Int> = _current

    fun setSong(song: SongCustom) {
        _title.value = song.title
        _artistsNames.value = song.artistsNames
        _timeMax.value = song.timeToString()
        _duration.value = song.duration.toInt()
        Log.d(myTag, "duration = ${_duration.value}")
    }

    fun postTimeToSeekbar(timeCurrent: Int, duration: Int) {
        _current.value = timeCurrent / 1000
        _timeCurrent.value = createTimerLabel(timeCurrent)
        _timeMax.value = createTimerLabel(duration)
    }

    fun handSeekBar() {
        val handler: Handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    if (mBound.value == true) {
                        val timeCurrent = mService.getPos()
                        val timeDuration = mService.getDur()
                        if (timeCurrent != null && timeDuration != null) {
                            postTimeToSeekbar(timeCurrent, timeDuration)
                        }
                        handler.postDelayed(this, 1000)
                    }
                } catch (e: Exception) {
                    Log.e(myTag, "error seek bar thread ${e.message}")
                }
            }
        }, 0)
    }

    fun seekTo(currentProcess: Int) {
        if (mBound.value == true) {
            try {
                mService.seek(currentProcess * 1000)
                Log.d(myTag, "seek to")
            } catch (e: IllegalStateException) {
                Log.e(myTag, "seek to error ${e.message}")
            }
        }
    }

    fun createTimerLabel(duration: Int): String {
        val sumSeconds = duration / 1000
        val hours = sumSeconds / 3600
        val minute = sumSeconds % 3600 / 60
        val seconds = sumSeconds % 60
        val hString = if (hours < 10) "0$hours" else "$hours"
        val mString = if (minute < 10) "0$minute" else "$minute"
        val sString = if (seconds < 10) "0$seconds" else "$seconds"
        return if (hours == 0) {
            "$mString:$sString"
        } else "$hString:$mString:$sString"
    }

    fun setImg(bitmap: Bitmap?) {
        _imgPlaying.value = bitmap
    }

    fun setIsPlaying(isPlay: Boolean) {
        _isPlaying.value = isPlay
    }

    private fun setStatePlay(state: Int) {
        _statePlay.value = state % 4
        mService.setStartPlay(state % 4)
    }
}