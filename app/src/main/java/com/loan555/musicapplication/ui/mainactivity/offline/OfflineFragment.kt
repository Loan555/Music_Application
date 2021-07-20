package com.loan555.musicapplication.ui.mainactivity.offline

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.loan555.musicapplication.R
import com.loan555.musicapplication.databinding.OfflineFragmentBinding
import com.loan555.musicapplication.model.Playlist
import com.loan555.musicapplication.ui.mainactivity.activity.MainViewModel
import com.loan555.musicapplication.ui.mainactivity.activity.myTag
import com.loan555.musicapplication.ui.playsongactivity.PlaySongActivity
import java.io.IOException
import java.lang.Exception

class OfflineFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: OfflineFragmentBinding
    private lateinit var playlist: Playlist

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.offline_fragment, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = activity?.let {
            ViewModelProviders.of(it).get(MainViewModel::class.java)
        } ?: throw Exception("Activity is null")

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        playlist = viewModel.getAllPlayList().value?.get(0)!!
        viewModel.setList(playlist!!)
        viewModel.setText(playlist!!.name)
        val adapter =
            SongOfflineAdapter(
                this.requireContext(),
                this.activity?.application,
                onItemClick
            )
        viewModel.playList.observe(viewLifecycleOwner,{
            adapter.setList(it)
        })
        binding.recycleSong.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        binding.recycleSong.adapter = adapter
    }

    private val onItemClick: (Int) -> Unit = {
        viewModel.playSong(viewModel.playList.value!!, it)
        viewModel.setVisibility(true)
        viewModel.setViewPlaying(playlist.songs[it])
        if (playlist.songs[it].thumbnail != null) {
            Glide.with(requireContext()).asBitmap().load(playlist.songs[it].thumbnail)
                .apply(RequestOptions().override(640, 480)).into(object :
                    CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        viewModel.setImg(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {

                    }

                })
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val thumb =
                        activity?.application?.contentResolver?.loadThumbnail(
                            Uri.parse(playlist.songs[it].uri), Size(640, 480), null
                        )
                    viewModel.setImg(thumb)
                } catch (e: IOException) {
                    Log.e(myTag, "can't find bitmap: ${e.message}")
                }
            }
        }
        //ham call back
        val intent = Intent(this.requireContext(), PlaySongActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable("play", playlist.songs[it])
        intent.putExtra("play_playlist", bundle)
        startActivity(intent)
    }
}