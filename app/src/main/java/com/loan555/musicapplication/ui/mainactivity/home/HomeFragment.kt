package com.loan555.musicapplication.ui.mainactivity.home

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.loan555.musicapplication.R
import com.loan555.musicapplication.databinding.HomeFragmentBinding
import com.loan555.musicapplication.model.Playlist
import com.loan555.musicapplication.ui.createplaylistactivity.CreatePlaylistActivity
import com.loan555.musicapplication.ui.playactivity.PlayActivity

class HomeFragment(
    private val allPlayList: MutableLiveData<ArrayList<Playlist>>
) : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: HomeFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        viewModel.setAllPlaylist(allPlayList)
        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment, container, false)
        binding.lifecycleOwner = this
        binding.homeViewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initController()
        initevent()
    }

    private fun initevent() {
        binding.btnAddPlaylist.setOnClickListener { btnAddClick() }
    }

    private fun initController() {
        val adapter = PlayListAdapter(this.requireContext(), onItemClick)
        adapter.setPlaylists(allPlayList.value!!)
        binding.recyclePlaylistView.adapter = adapter
        binding.recyclePlaylistView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    private fun btnAddClick() {
        val intent = Intent(this.requireContext(), CreatePlaylistActivity::class.java)
        startActivity(intent)
    }

    private val onItemClick: (Playlist) -> Unit = {
        //ham call back
        val intent = Intent(this.requireContext(), PlayActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable("playlist", it)
        intent.putExtra("play_playlist", bundle)
        startActivity(intent)
    }
}