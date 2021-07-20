package com.loan555.musicapplication.ui.mainactivity.activity

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.loan555.musicapplication.R
import com.loan555.musicapplication.databinding.ActivityMainBinding
import com.loan555.musicapplication.service.KEY_ACTION_MUSIC
import com.loan555.musicapplication.service.MusicControllerService
import com.loan555.musicapplication.ui.mainactivity.chart.ChartFragment
import com.loan555.musicapplication.ui.mainactivity.home.HomeFragment
import com.loan555.musicapplication.ui.mainactivity.offline.OfflineFragment
import com.loan555.musicapplication.ui.playactivity.ui.main.PlayFragment
import com.loan555.musicapplication.ui.playsongactivity.PlaySongActivity

const val ACTION_MUSIC = "android.intent.action.MY_MUSIC_ACTION"
private const val NUM_PAGES = 3

const val STORAGE_REQUEST_CODE = 1
const val myTag = "myTagDebug"

class MainActivity : FragmentActivity() {
    //permission
    private val storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding.lifecycleOwner = this
        binding.mainViewModel = viewModel
        if (checkStoragePermission()) {
            viewModel.loadDataStorage()
        } else requestStoragePermission()
        viewModel.loadChartOnline()

        initController()
    }

    override fun onStart() {
        super.onStart()
        Log.d(myTag, "onStart activity dang ky service va broast cast")
        val intentService = Intent(this, MusicControllerService::class.java)
        bindService(intentService, viewModel.conn, Context.BIND_AUTO_CREATE)
        registerReceiver(viewModel.br, viewModel.filter)
        viewModel.mBound.observe(this, {
            if (it) {
                viewModel.callServiceDemo("day la service cua toi")
            }
        })
    }

    override fun onBackPressed() {
        if (binding.viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            binding.viewPager.currentItem = binding.viewPager.currentItem - 1
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(
            myTag, "onRequestPermissionsResult"
        )
        when (requestCode) {
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted
                    Toast.makeText(this, "Allow...", Toast.LENGTH_SHORT)
                        .show()
                    viewModel.loadDataStorage()
                } else {
                    //permission denied
                    Toast.makeText(this, "Storage permission required...", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
            }
        }
    }

    private fun checkStoragePermission(): Boolean {
        Log.e(myTag, "check permission")
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == (PackageManager.PERMISSION_GRANTED)
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE)
    }

    private fun initController() {
        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = PagerAdapter(supportFragmentManager)
        binding.viewPager.adapter = pagerAdapter
        viewModel.visibility.observe(this, {
            if (it) binding.itemPlaying.visibility = View.VISIBLE
            else binding.itemPlaying.visibility = View.GONE
        })
        viewModel.imgPlaying.observe(this, {
            binding.imgSong.setImageBitmap(it)
        })
        binding.itemPlaying.setOnClickListener {
            val intent = Intent(this, PlaySongActivity::class.java)
            val mService = viewModel.mBinder.value?.getService()
            val bundle = Bundle()
            if (mService != null)
                bundle.putSerializable("play", mService?.songs[mService?.songPos])
            intent.putExtra("play_playlist", bundle)
            startActivity(intent)
        }
        viewModel.isPlaying.observe(this,{
            if (it) binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
            else binding.btnPlayPause.setImageResource(R.drawable.ic_play)
        })
    }

    private inner class PagerAdapter(fa: FragmentManager) : FragmentPagerAdapter(fa) {

        override fun getCount(): Int = NUM_PAGES

        override fun getItem(position: Int): Fragment {
            return when (position) {
                1 -> OfflineFragment()
                2 -> ChartFragment()
                else -> HomeFragment(viewModel.getAllPlayList())
            }
        }
    }

}