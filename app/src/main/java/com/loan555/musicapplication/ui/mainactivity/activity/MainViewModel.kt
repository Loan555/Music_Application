package com.loan555.musicapplication.ui.mainactivity.activity

import android.app.Application
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.util.Size
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.loan555.musicapplication.model.Playlist
import com.loan555.musicapplication.model.SongCustom
import com.loan555.musicapplication.repository.PlaylistRepository
import com.loan555.musicapplication.service.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class MainViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var mService: MusicControllerService

    private val playListsRepository: PlaylistRepository = PlaylistRepository(application)

    fun getAllPlayList() = playListsRepository.getAllPlayList()

    fun loadDataStorage() = playListsRepository.loadOfflineSong()

    fun loadChartOnline() = playListsRepository.loadChartOnlineSong()

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
                setVisibility(true)
                setViewPlaying(
                    binder.getService().songs[binder.getService().songPos]
                )
                setIsPlaying(mService.isPlaying)
                getBitmap(mService.songs[mService.songPos])
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBinder.postValue(null)
            _mBound.value = false
        }
    }

    fun getBitmap(song: SongCustom){
        if (song.thumbnail== null){
            setImg(song.getBitmapFromURI(getApplication()))
        }else {
            GlobalScope.launch(Dispatchers.Main) {
                val result = async(Dispatchers.IO) {
                    return@async song.getBitmapFromURL()
                }
                setImg(result.await())
            }
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
            if (action != null)
                handActionMusic(action)// lang nghe broast cast de cap nhat giao dien
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
                _visibility.value = true
                Log.d(myTag, "ACTION_PLAY activity $ACTION_PLAY")
                setIsPlaying(true)
                val songPlaying = mService.songs[mService.songPos]
                setViewPlaying(songPlaying)
                if (mService.songs[mService.songPos].thumbnail != null) {
                    try {
                        GlobalScope.launch(Dispatchers.Main) {
                            val result = async(Dispatchers.IO) {
                                return@async getBitmapFromURL(mService.songs[mService.songPos].thumbnail)
                            }
                            setImg(result.await())
                        }
                    } catch (e: IOException) {
                        // Log exception
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            val thumb =
                                this.getApplication<Application>().contentResolver?.loadThumbnail(
                                    Uri.parse(mService.songs[mService.songPos].uri), Size(640, 480), null
                                )
                            setImg(thumb)
                        } catch (e: IOException) {
                            Log.e(myTag, "can't find bitmap: ${e.message}")
                        }
                    }
                }
            }
            ACTION_PLAY_PAUSE -> {
                if (mService.isPng() == false) handActionMusic(ACTION_RESUME)
                else handActionMusic(ACTION_PAUSE)
            }
        }
    }

    fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            // Log exception
            null
        }
    }

    fun callServiceDemo(str: String) {
        Log.d(myTag, "call viewmodel nay")
        mService = mBinder.value?.getService()!!
        mService?.callServiceDemo(str)
    }

    fun playSong(list: Playlist, p: Int) {
        if (list.id != mService.listPlaying) {
            mService.songs = list.songs!!
        }
        mService.playSong(p)
    }

    fun playOrPauseClick() {
        handBtnPlayPause()
    }

    private fun handBtnPlayPause() {
        if (mBound.value== true) {
            if (mService.isPng() == true) {
                controlMusic(ACTION_PAUSE)
                setIsPlaying(true)
            } else {
                controlMusic(ACTION_RESUME)
                setIsPlaying(false)
            }
        }
    }

    fun btnSkipNextClick() {
        controlMusic(ACTION_NEXT)
    }

    fun btnSkipBackClick() {
        controlMusic(ACTION_BACK)
    }

    private fun controlMusic(action: Int) {
        val intent = Intent().also {
            it.action = ACTION_MUSIC
            it.putExtra(KEY_ACTION_MUSIC, action)
        }
        mService.sendBroadcast(intent)
    }

    private val _visibility = MutableLiveData<Boolean>().apply { value = false }
    val visibility: LiveData<Boolean> = _visibility
    private val _textSongPlaying = MutableLiveData<String>().apply { value = "" }
    val textSongPlaying: LiveData<String> = _textSongPlaying
    private val _textSingerPlaying = MutableLiveData<String>().apply { value = "" }
    val textSingerPlaying: LiveData<String> = _textSingerPlaying
    private val _imgPlaying = MutableLiveData<Bitmap?>().apply { value = null }
    val imgPlaying: LiveData<Bitmap?> = _imgPlaying
    private val _isPlaying = MutableLiveData<Boolean>().apply { value = false }
    val isPlaying: LiveData<Boolean> = _isPlaying

    fun setIsPlaying(isPlay: Boolean) {
        _isPlaying.value = isPlay
    }

    fun setViewPlaying(song: SongCustom) {
        _textSongPlaying.value = song.title
        _textSingerPlaying.value = song.artistsNames
    }

    fun setImg(bitmap: Bitmap?) {
        _imgPlaying.value = bitmap
    }

    fun setVisibility(vi: Boolean) {
        _visibility.value = vi
    }

    private val _text = MutableLiveData<String>().apply { value = "" }
    private val _size = MutableLiveData<String>().apply { value = "" }
    val playList = MutableLiveData<Playlist>().apply { value = null }

    val text: LiveData<String> = _text
    val size: LiveData<String> = _size

    fun setText(str: String) {
        _text.value = str
    }

    fun setList(list: Playlist) {
        playList.value = list
        _size.value = "${list.songs.size} bài hát"
    }

    //----------- chart
    private val _textChart = MutableLiveData<String>().apply { value = "" }
    private val _sizeChart = MutableLiveData<String>().apply { value = "" }
    private val _playListChart = MutableLiveData<Playlist>().apply { value = null }

    val textChart: LiveData<String> = _textChart
    val sizeChart: LiveData<String> = _sizeChart
    val playlistChart: LiveData<Playlist> = _playListChart

    fun setTextChart(str: String) {
        _textChart.value = str
    }

    fun setListChart(list: Playlist) {
        _playListChart.value = list
        _sizeChart.value = "${list.songs.size} bài hát"
    }
}