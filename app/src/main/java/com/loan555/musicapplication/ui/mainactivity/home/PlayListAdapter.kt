package com.loan555.musicapplication.ui.mainactivity.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.loan555.musicapplication.R
import com.loan555.musicapplication.model.Playlist

class PlayListAdapter(
    private val context: Context,
    private val onClick: (Playlist) -> Unit
) :
    RecyclerView.Adapter<PlayListAdapter.PlaylistViewHolder>() {
    private var playlist: ArrayList<Playlist> = ArrayList()

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtName: TextView = itemView.findViewById(R.id.txtName)
        private val layoutItem: ConstraintLayout = itemView.findViewById(R.id.layoutItem)
        private val imgNote: ImageView = itemView.findViewById(R.id.image)
        fun onBind(playlist: Playlist) {
            txtName.text = playlist.name
//            Glide.with(context).load(note.imgPath).into(imgNote)
            if (playlist.img!= null){
                imgNote.setImageResource(playlist.img)
            }
            layoutItem.setOnClickListener { onClick(playlist) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.playlist_view_adapter, parent, false)
        return PlaylistViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.onBind(playlist[position])
    }

    override fun getItemCount(): Int = playlist.size

    fun setPlaylists(lists: ArrayList<Playlist>) {
        this.playlist = lists
        notifyDataSetChanged()
    }
}