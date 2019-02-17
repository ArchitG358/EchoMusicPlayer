package com.archit.myplayer.adapters

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView

import com.archit.myplayer.R
import com.archit.myplayer.Songs
import com.archit.myplayer.fragments.SongPlayingFragment

import java.util.ArrayList

class MainScreenAdapter(internal var songDetails: ArrayList<Songs>?, internal var mContext: Context) : RecyclerView.Adapter<MainScreenAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context).inflate(R.layout.row_custom_mainscreen_adapter, viewGroup, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
        val songObject = songDetails!![i]
        myViewHolder.trackTitle.text = songObject.songTitle
        myViewHolder.trackArtist.text = songObject.artist
        myViewHolder.contentHolder.setOnClickListener {
            if (SongPlayingFragment.mediaplayer != null) {
                if (SongPlayingFragment.mediaplayer!!.isPlaying) {
                    SongPlayingFragment.mediaplayer!!.pause()
                }
            }
            val songPlayingFragment = SongPlayingFragment()
            val args = Bundle()
            Log.d("Adapter Position", "position =" + myViewHolder.adapterPosition)
            Log.d("songArtist", songObject.artist)
            Log.d("songTitle", songObject.songTitle)
            args.putString("songArtist", songObject.artist)
            args.putString("path", songObject.songData)
            args.putString("songTitle", songObject.songTitle)
            args.putInt("songId", songObject.songID!!.toInt())
            args.putInt("songPosition", myViewHolder.adapterPosition)
            args.putParcelableArrayList("songData", songDetails)
            songPlayingFragment.arguments = args
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.details_fragment, songPlayingFragment)
                    .addToBackStack("SongPlayingFragment")
                    .commit()
        }
    }

    override fun getItemCount(): Int {
        return if (songDetails == null)
            0
        else
            songDetails!!.size
    }

    class MyViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        internal var trackTitle: TextView
        internal var trackArtist: TextView
        internal var contentHolder: RelativeLayout

        init {
            this.trackTitle = view.findViewById(R.id.trackTitle)
            this.trackArtist = view.findViewById(R.id.trackArtist)
            this.contentHolder = view.findViewById(R.id.contentRow)
        }
    }
}
