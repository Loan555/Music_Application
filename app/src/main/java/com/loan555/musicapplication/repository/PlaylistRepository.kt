package com.loan555.musicapplication.repository

import android.app.Application
import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.loan555.musicapplication.R
import com.loan555.musicapplication.api.ApiChartService
import com.loan555.musicapplication.api.ApiRelatedSong
import com.loan555.musicapplication.api.ApiSearchService
import com.loan555.musicapplication.model.DataChartResult
import com.loan555.musicapplication.model.Playlist
import com.loan555.musicapplication.model.SongCustom
import com.loan555.musicapplication.ui.mainactivity.activity.myTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.lang.Exception

const val PLAYLIST_LOCAL = "PLAYLIST_LOCAL"
const val PLAYLIST_CHART = "PLAYLIST_CHART"
const val PLAYLIST_FAVORITE = "PLAYLIST_FAVORITE"

class PlaylistRepository(private val application: Application) {
    private val tempList = ArrayList<Playlist>().apply {
        this.add(Playlist(PLAYLIST_LOCAL, "Nhạc tải về", R.drawable.ic_offline, ArrayList()))
        this.add(Playlist(PLAYLIST_CHART, "Bảng xếp hạng", R.drawable.ic_bar_chart, ArrayList()))
        this.add(
            Playlist(
                PLAYLIST_FAVORITE,
                "Danh sách yêu thích",
                R.drawable.ic_favorite,
                ArrayList()
            )
        )
    }

    private val playlists =
        MutableLiveData<ArrayList<Playlist>>().apply {
            value = tempList
        }

    fun getAllPlayList() = playlists

    fun addPlayList(playList: Playlist) {
        this.playlists.value?.add(playList)
    }

    private fun setPlaylist(id: String, songs: ArrayList<SongCustom>) {
        this.playlists.value?.forEach {
            if (it.id == id) {
                it.songs = songs
                return
            }
        }
    }

    fun loadChartOnlineSong() {
        Log.d(myTag, "loadChartOnlineSong")
        val call = serviceApiGetChart.getCurrentData(
            songId,
            videoId,
            albumId,
            chart,
            time
        )
        call.enqueue(object : Callback<DataChartResult> {
            override fun onResponse(
                call: Call<DataChartResult>?,
                response: Response<DataChartResult>?
            ) {
                Log.d(myTag, "response.code() == ${response?.code()}")
                if (response?.code() == 200) {
                    val dataResponse = response?.body()!!
                    val listChart = dataResponse.data
                    val newList = ArrayList<SongCustom>()
                    listChart.song.forEach {
                        newList.add(
                            SongCustom(
                                it.id,
                                it.name,
                                it.artistsNames,
                                it.thumbnail,
                                it.duration,
                                it.title,
                                "http://api.mp3.zing.vn/api/streaming/audio/${it.id}/320"
                            )
                        )
                    }
                    setPlaylist(PLAYLIST_CHART, newList)
                    Log.d(myTag, "load data chart done! ${playlists.value?.get(1)}")
                }
            }

            override fun onFailure(call: Call<DataChartResult>?, t: Throwable?) {
                Log.e(myTag, "load data chart error: ${t?.message}")
            }
        })
    }

    fun loadOfflineSong() {
        val collection: Uri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
        val protection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM
        )
        val selection = null
        val selectionArgs = null
        val sortOder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"
        val newSongs = ArrayList<SongCustom>()
        GlobalScope.launch(Dispatchers.IO) {
            val query = application.contentResolver.query(
                collection,
                protection,
                selection,
                selectionArgs,
                sortOder
            )
            try {
                query?.use { cursor ->
                    // Cache column indices.
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val nameColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                    val artistsColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val durationColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                    val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val albumsColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    while (cursor.moveToNext()) {
                        while (cursor.moveToNext()) {
                            // Get values of columns for a given audio.
                            val id = cursor.getLong(idColumn)
                            val name = cursor.getString(nameColumn)
                            val artists = cursor.getString(artistsColumn)
                            val duration = cursor.getLong(durationColumn)
                            val size = cursor.getInt(sizeColumn)
                            val title = cursor.getString(titleColumn)
                            val albums = cursor.getString(albumsColumn)
                            //load content Uri
                            val contentUri: Uri =
                                ContentUris.withAppendedId(
                                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    id
                                )
                            newSongs.add(
                                SongCustom(
                                    id.toString(), name, artists,
                                    thumbnail = null, duration / 1000, title, contentUri.toString()
                                )
                            )
                        }
                    }
                }
                setPlaylist(PLAYLIST_LOCAL, newSongs)
                Log.d(myTag, "load data storage done! ${playlists.value?.get(0)}")
            } catch (e: Exception) {
                Log.e(myTag, "loadOfflineSong error: ${e.message}")
            }
        }

    }

    companion object {
        //http://mp3.zing.vn/xhr/chart-realtime?songId=0&videoId=0&albumId=0&chart=song&time=-1
        val serviceApiGetChart by lazy { ApiChartService.create() }
        var songId = 0
        var videoId = 0
        var albumId = 0
        var chart = "song"
        var time = -1

        ////http://ac.mp3.zing.vn/complete?type=artist,song,key,code&num=500&query=Anh Thế Giới Và Em
        val apiSearchService by lazy { ApiSearchService.create() }
        var typeSearch = "artist,song,key,code"
        var num = 500.toLong()

        //http://mp3.zing.vn/xhr/recommend?type=audio&id=ZW67OIA0
        val apiRelatedSong by lazy {
            ApiRelatedSong.create()
        }
    }
}