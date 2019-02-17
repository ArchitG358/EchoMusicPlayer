package com.archit.myplayer.fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView

import com.archit.myplayer.R
import com.archit.myplayer.Songs
import com.archit.myplayer.adapters.MainScreenAdapter

import java.util.ArrayList
import java.util.Collections
import java.util.Objects

class MainScreenFragment : Fragment() {
     var getSongsList: ArrayList<Songs>? = null
     var nowPlayingBottomBar: RelativeLayout?=null
     var visibleLayout: RelativeLayout?=null
     var noSongs: RelativeLayout?=null
     var recyclerView: RecyclerView?=null
     var playPauseButton: ImageButton?=null
     var songTitle: TextView?=null
     var myActivity: Activity? = null
     var _mainScreenAdapter: MainScreenAdapter?=null
     var trackPosition = 0

    val songsFromPhone: ArrayList<Songs>
        get() {
            val arrayList = ArrayList<Songs>()
            val contentResolver = myActivity!!.contentResolver
            val songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val songCursor = contentResolver.query(songUri, null, null, null, null)
            if (songCursor != null && songCursor.moveToFirst()) {
                val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                while (songCursor.moveToNext()) {
                    val currentId = songCursor.getLong(songId)
                    val currentTitle = songCursor.getString(songTitle)
                    val currentArtist = songCursor.getString(songArtist)
                    val currentData = songCursor.getString(songData)
                    val currentDate = songCursor.getLong(dateIndex)
                    arrayList.add(Songs(currentId, currentTitle, currentArtist, currentData, currentDate))
                }
            }
            return arrayList
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main_screen, container, false)
        setHasOptionsMenu(true)
        Objects.requireNonNull<FragmentActivity>(activity).setTitle("All Songs")
        visibleLayout = view.findViewById(R.id.visibleLayout)
        noSongs = view.findViewById(R.id.noSongs)
        nowPlayingBottomBar = view.findViewById(R.id.hiddenBarMainScreen)
        recyclerView = view.findViewById(R.id.contentMain)
        playPauseButton = view.findViewById(R.id.mainscreen_playPauseButton)
        songTitle = view.findViewById(R.id.songTitleMainScreen)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu!!.clear()
        inflater!!.inflate(R.menu.main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val switcher = item!!.itemId
        if (switcher == R.id.action_sort_ascending) {
            val editor = myActivity!!.getSharedPreferences("action_sort", Context.MODE_PRIVATE).edit()
            editor.putString("action_sort_ascending", "true")
            editor.putString("action_sort_recent", "false")
            editor.apply()
            if (getSongsList != null) {
                Collections.sort(getSongsList, Songs.nameComparator)
            }
            _mainScreenAdapter?.notifyDataSetChanged()
            return false
        } else if (switcher == R.id.action_sort_recent) {
            val editor = myActivity!!.getSharedPreferences("action_sort", Context.MODE_PRIVATE).edit()
            editor.putString("action_sort_recent", "true")
            editor.putString("action_sort_ascending", "false")
            editor.apply()
            if (getSongsList != null) {
                Collections.sort(getSongsList, Songs.dateComparator)
            }
            _mainScreenAdapter?.notifyDataSetChanged()
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity?
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getSongsList = songsFromPhone
        val prefs = myActivity!!.getSharedPreferences("action_sort", Context.MODE_PRIVATE)
        val action_sort_ascending = prefs.getString("action_sort_ascending", "true")
        val action_sort_recent = prefs.getString("action_sort_recent", "false")
        if (getSongsList == null) {
            visibleLayout?.visibility = View.INVISIBLE
            noSongs?.visibility = View.VISIBLE
        } else {
            _mainScreenAdapter = MainScreenAdapter(getSongsList, myActivity!!)
            val mLayoutManager = LinearLayoutManager(myActivity)
            recyclerView?.layoutManager = mLayoutManager
            recyclerView?.itemAnimator = DefaultItemAnimator()
            recyclerView?.adapter = _mainScreenAdapter
        }

        if (getSongsList != null) {
            if (action_sort_ascending == "true") {
                Collections.sort(getSongsList, Songs.nameComparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            } else if (action_sort_recent == "true") {
                Collections.sort(getSongsList, Songs.dateComparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            }
        }
        bottomBarSetup()
    }

    override fun onResume() {
        super.onResume()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun bottomBarSetup() {
        try {
            bottomBarClickHandler()
            songTitle?.text = SongPlayingFragment.currentSongHelper!!.songTitle
            SongPlayingFragment.mediaplayer!!.setOnCompletionListener {
                songTitle?.text = SongPlayingFragment.currentSongHelper!!.songTitle
                SongPlayingFragment.onSongComplete()
            }
            if (SongPlayingFragment.mediaplayer!!.isPlaying) {
                nowPlayingBottomBar?.visibility = View.VISIBLE
            } else {
                nowPlayingBottomBar?.visibility = View.INVISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun bottomBarClickHandler() {
        nowPlayingBottomBar?.setOnClickListener {
            mediaPlayer = SongPlayingFragment.mediaplayer
            val songPlayingFragment = SongPlayingFragment()
            val args = Bundle()
            args.putString("songArtist", SongPlayingFragment.currentSongHelper!!.songArtist)
            args.putString("path", SongPlayingFragment.currentSongHelper!!.songPath)
            args.putString("songTitle", SongPlayingFragment.currentSongHelper!!.songTitle)
            args.putInt("songId", Math.toIntExact(SongPlayingFragment.currentSongHelper!!.songId!!))
            args.putInt("songPosition", SongPlayingFragment.currentSongHelper!!.currentPosition)
            args.putParcelableArrayList("songData", SongPlayingFragment.fetchSongs)
            args.putString("MainScreenBottomBar", "success")
            songPlayingFragment.arguments = args
            Objects.requireNonNull<FragmentManager>(fragmentManager).beginTransaction().replace(R.id.details_fragment, songPlayingFragment).addToBackStack("SongPlayingFragment")
                    .commit()
        }
        playPauseButton?.setOnClickListener {
            if (SongPlayingFragment.mediaplayer!!.isPlaying) {
                SongPlayingFragment.mediaplayer!!.pause()
                trackPosition = SongPlayingFragment.mediaplayer!!.currentPosition
                playPauseButton!!.setBackgroundResource(R.drawable.play_icon)
                SongPlayingFragment.playpauseImageButton!!.setBackgroundResource(R.drawable.play_icon)
                SongPlayingFragment.currentSongHelper!!.isPlaying = false
            } else {
                SongPlayingFragment.mediaplayer!!.seekTo(trackPosition)
                SongPlayingFragment.mediaplayer!!.start()
                playPauseButton!!.setBackgroundResource(R.drawable.pause_icon)

                SongPlayingFragment.playpauseImageButton!!.setBackgroundResource(R.drawable.pause_icon)
                SongPlayingFragment.currentSongHelper!!.isPlaying = true
            }
        }
    }

    companion object {
        var mediaPlayer: MediaPlayer? = null
    }
}// Required empty public constructor
