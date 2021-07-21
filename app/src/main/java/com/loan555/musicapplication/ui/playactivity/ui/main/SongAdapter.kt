package com.loan555.musicapplication.ui.playactivity.ui.main

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.loan555.musicapplication.R
import com.loan555.musicapplication.model.Playlist
import com.loan555.musicapplication.model.SongCustom
import com.loan555.musicapplication.ui.mainactivity.activity.myTag
import java.io.IOException

class SongAdapter(
    private val context: Context,
    private val application: Application?,
    private val playlist: Playlist,
    private val onClick: (Int) -> Unit
) :
    RecyclerView.Adapter<SongAdapter.PlaylistViewHolder>() {

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtName: TextView = itemView.findViewById(R.id.song_name)
        private val layoutItem: ConstraintLayout = itemView.findViewById(R.id.layoutItemSong)
        private val imgNote: ImageView = itemView.findViewById(R.id.img_song)
        private val artist: TextView = itemView.findViewById(R.id.artists_names)
        private val time: TextView = itemView.findViewById(R.id.duration)
        fun onBind(song: SongCustom) {
            txtName.text = song.title
            artist.text = song.artistsNames
            time.text = song.timeToString()
            if (song.thumbnail != null) {
//                Glide.with(context).load(song.thumbnail).into(imgNote)
                Glide.with(context).asBitmap().load(song.thumbnail)
                    .apply(RequestOptions().override(640, 480)).into(object :
                        CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            Glide.with(context).load(resource).into(imgNote)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }

                    })
            } else {
                if (application != null) {
                    var thumbnail: Bitmap? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            val thumb =
                                context.applicationContext.contentResolver.loadThumbnail(
                                    Uri.parse(song.uri), Size(640, 480), null
                                )
                            thumbnail = thumb
                        } catch (e: IOException) {
                            Log.e(myTag, "can't find bitmap: ${e.message}")
                        }
                    }
                    if (thumbnail != null)
                        Glide.with(context).load(thumbnail).into(imgNote)
                    else
                        Glide.with(context).load(R.drawable.ic_music_note).into(imgNote)
                }
            }
            layoutItem.setOnClickListener { onClick(layoutPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.song_adapter, parent, false)
        return PlaylistViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.onBind(playlist.songs[position])
    }

    override fun getItemCount(): Int = playlist.songs.size
}