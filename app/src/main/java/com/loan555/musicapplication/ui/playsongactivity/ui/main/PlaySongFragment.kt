package com.loan555.musicapplication.ui.playsongactivity.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.loan555.musicapplication.R
import com.loan555.musicapplication.databinding.PlaySongFragmentBinding
import com.loan555.musicapplication.model.Playlist
import com.loan555.musicapplication.model.SongCustom

class PlaySongFragment : Fragment() {


    companion object {
        fun newInstance() = PlaySongFragment()
    }

    private lateinit var viewModel: PlaySongModel
    private lateinit var binding: PlaySongFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.play_song_fragment, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(PlaySongModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        val bundle = activity?.intent?.getBundleExtra("play_playlist")
        val song = bundle?.getSerializable("play") as SongCustom
        viewModel.setText(song.toString())
    }

}