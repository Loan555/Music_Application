package com.loan555.musicapplication.ui.playactivity.ui.main

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.loan555.musicapplication.R
import com.loan555.musicapplication.databinding.PlayFragmentBinding
import com.loan555.musicapplication.model.Playlist
import com.loan555.musicapplication.ui.playsongactivity.PlaySongActivity

class PlayFragment : Fragment() {

    companion object {
        fun newInstance() = PlayFragment()
    }

    private lateinit var viewModel: PlayViewModel
    private lateinit var binding: PlayFragmentBinding
    private lateinit var playlist: Playlist

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.play_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(PlayViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        val bundle = activity?.intent?.getBundleExtra("play_playlist")
        playlist = bundle?.getSerializable("playlist") as Playlist
        viewModel.setList(playlist)
        viewModel.setText(playlist.name)
        val adapter = SongAdapter(this.requireContext(),this.activity?.application,playlist,onItemClick)
        binding.recycleSong.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        binding.recycleSong.adapter = adapter
    }

    private val onItemClick: (Int) -> Unit = {
        //ham call back
        val intent = Intent(this.requireContext(), PlaySongActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable("play", playlist.songs[it])
        intent.putExtra("play_playlist", bundle)
        startActivity(intent)
    }
}