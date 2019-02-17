package com.archit.myplayer.fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView

import com.archit.myplayer.R
import com.archit.myplayer.Songs
import com.archit.myplayer.adapters.FavouriteAdapter
import com.archit.myplayer.databases.EchoDatabase

import java.util.ArrayList
import java.util.Objects

class FavouriteFragment : Fragment() {
    internal var myActivity: Activity? = null

    internal var nowPlayingBottomBar: RelativeLayout? = null
    internal var recyclerView: RecyclerView? = null
    internal var playPauseButton: ImageButton? = null
    internal var noFavourites: TextView? = null
    internal var songTitle: TextView? = null
    internal var trackPosition = 0
    internal var favouriteContent: EchoDatabase?=null

    internal var refreshList: ArrayList<Songs>? = null
    internal var getListFromDatabase: ArrayList<Songs>? = null

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
        val view = inflater.inflate(R.layout.fragment_favourite, container, false)
        activity!!.title = "Favourites"
        noFavourites = view.findViewById(R.id.noFavourites)
        nowPlayingBottomBar = view.findViewById(R.id.hiddenBarFavScreen)
        songTitle = view.findViewById(R.id.songTitleFavScreen)
        playPauseButton = view.findViewById(R.id.playpauseButton)
        recyclerView = view.findViewById(R.id.favouriteRecycler)
        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity?
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        favouriteContent = EchoDatabase(myActivity)
        display_favourites_by_searching()
        bottomBarSetup()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item = menu!!.findItem(R.id.action_sort)
        item.isVisible = false
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun bottomBarSetup() {
        try {
            bottomBarClickHandler()
            songTitle!!.text = SongPlayingFragment.currentSongHelper!!.songTitle
            SongPlayingFragment.mediaplayer!!.setOnCompletionListener {
                songTitle!!.text = SongPlayingFragment.currentSongHelper!!.songTitle
                SongPlayingFragment.onSongComplete()
            }
            if (SongPlayingFragment.mediaplayer!!.isPlaying) {
                nowPlayingBottomBar!!.visibility = View.VISIBLE
            } else {
                nowPlayingBottomBar!!.visibility = View.INVISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun bottomBarClickHandler() {
        nowPlayingBottomBar!!.setOnClickListener {
            mediaPlayer = SongPlayingFragment.mediaplayer
            val songPlayingFragment = SongPlayingFragment()
            val args = Bundle()
            args.putString("songArtist", SongPlayingFragment.currentSongHelper!!.songArtist)
            args.putString("path", SongPlayingFragment.currentSongHelper!!.songPath)
            args.putString("songTitle", SongPlayingFragment.currentSongHelper!!.songTitle)
            args.putInt("songId", Math.toIntExact(SongPlayingFragment.currentSongHelper!!.songId!!))
            args.putInt("songPosition", SongPlayingFragment.currentSongHelper!!.currentPosition)
            args.putParcelableArrayList("songData", SongPlayingFragment.fetchSongs)
            args.putString("FavBottomBar", "success")
            songPlayingFragment.arguments = args
            Objects.requireNonNull<FragmentManager>(fragmentManager).beginTransaction().replace(R.id.details_fragment, songPlayingFragment).addToBackStack("SongPlayingFragment")
                    .commit()
        }
        playPauseButton!!.setOnClickListener {
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

    fun display_favourites_by_searching() {
        if (favouriteContent?.checkSize()!! > 0) {
            refreshList = ArrayList()
            getListFromDatabase = favouriteContent?.queryDBList()
            val fetchListfromDevice = songsFromPhone
            if (fetchListfromDevice != null) {
                for (i in fetchListfromDevice.indices) {
                    for (j in getListFromDatabase!!.indices) {
                        if (getListFromDatabase!![j].songID == fetchListfromDevice[i].songID) {
                            refreshList!!.add(getListFromDatabase!![j])
                        }
                    }
                }
            } else {

            }
            if (refreshList == null) {
                recyclerView!!.visibility = View.INVISIBLE
                noFavourites!!.visibility = View.VISIBLE
            } else {
                val favouriteAdapter = FavouriteAdapter(refreshList, myActivity)
                val mLayoutManager = LinearLayoutManager(activity)
                recyclerView!!.layoutManager = mLayoutManager
                recyclerView!!.itemAnimator = DefaultItemAnimator()
                recyclerView!!.adapter = favouriteAdapter
                recyclerView!!.setHasFixedSize(true)
            }
        } else {
            recyclerView!!.visibility = View.INVISIBLE
            noFavourites!!.visibility = View.VISIBLE
        }
    }

    companion object {
        var mediaPlayer: MediaPlayer? = null
    }
}// Required empty public constructor
