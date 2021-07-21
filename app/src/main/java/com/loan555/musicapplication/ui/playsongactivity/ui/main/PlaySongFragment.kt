package com.loan555.musicapplication.ui.playsongactivity.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.loan555.musicapplication.R
import com.loan555.musicapplication.databinding.PlaySongFragmentBinding
import com.loan555.musicapplication.model.SongCustom
import com.loan555.musicapplication.ui.playsongactivity.PlaySongViewModel
import java.lang.Exception

class PlaySongFragment : Fragment() {

    companion object {
        fun newInstance() = PlaySongFragment()
    }

    private lateinit var viewModel: PlaySongViewModel
    private lateinit var binding: PlaySongFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.play_song_fragment, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = activity?.let {
            ViewModelProviders.of(it).get(PlaySongViewModel::class.java)
        } ?: throw Exception("Activity is null")

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        val bundle = activity?.intent?.getBundleExtra("play_playlist")
        val song = bundle?.getSerializable("play") as SongCustom

        viewModel.isPlaying.observe(viewLifecycleOwner, {
            if (it) binding.play.setBackgroundResource(R.drawable.ic_pause)
            else binding.play.setBackgroundResource(R.drawable.ic_play)
        })
        viewModel.imgPlaying.observe(viewLifecycleOwner, {
            binding.imgSrc.setImageBitmap(it)
        })
        binding.btnDownload.setOnClickListener {
            val songPlaying = viewModel.mService.songs[viewModel.mService.songPos]
            if (songPlaying.thumbnail != null)
                songPlaying.downLoad(requireContext())
            else Toast.makeText(this.context, "Bài hát đã có trong thiết bị", Toast.LENGTH_SHORT)
                .show()
        }
        viewModel.statePlay.observe(viewLifecycleOwner, {
            when (it) {
                //0 la tuan tu roi ket thuc
                //1 la lap lai list
                //2 la phat ngau nhien
                //3 lap lai 1 bai
                0 -> {
                    binding.loop.setBackgroundResource(R.drawable.ic_repeat)
                }
                1 -> {
                    binding.loop.setBackgroundResource(R.drawable.ic_baseline_repeat_24_color)
                }
                2 -> {
                    binding.loop.setBackgroundResource(R.drawable.ic_baseline_shuffle_24)
                }
                3 -> {
                    binding.loop.setBackgroundResource(R.drawable.ic_baseline_repeat_one_24)
                }
            }
        })

        var currentProcess = 0
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentProcess = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                viewModel.seekTo(currentProcess)
            }

        })
    }
}