package com.archit.myplayer.activities

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.MenuItem

import com.archit.myplayer.R
import com.archit.myplayer.adapters.NavigationDrawerAdapter
import com.archit.myplayer.fragments.MainScreenFragment
import com.archit.myplayer.fragments.SongPlayingFragment

import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    internal var navigationDrawerIconsList = ArrayList<String>()
    internal var images_for_navdrawer = intArrayOf(R.drawable.navigation_allsongs, R.drawable.navigation_favorites, R.drawable.navigation_settings, R.drawable.navigation_aboutus)
    internal var trackNotificationBuilder: Notification? = null
    internal var toggle: ActionBarDrawerToggle?=null
    internal var notify_ID = 1978
    internal var CHANNEL_ID = "echo_channel"
    internal var channelname: CharSequence = "Echo Player"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)

        navigationDrawerIconsList.add("All Songs")
        navigationDrawerIconsList.add("Favourites")
        navigationDrawerIconsList.add("Settings")
        navigationDrawerIconsList.add("About Us")

        toggle = ActionBarDrawerToggle(this@MainActivity, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout!!.addDrawerListener(toggle!!)
        toggle!!.syncState()
        val mainScreenFragment = MainScreenFragment()
        this.supportFragmentManager.beginTransaction()
                .add(R.id.details_fragment, mainScreenFragment, "MainScreenFragment")
                .commit()

        val _navigationAdapter = NavigationDrawerAdapter(navigationDrawerIconsList, images_for_navdrawer, this)
        _navigationAdapter.notifyDataSetChanged()

        val navigation_recycler_view = findViewById<RecyclerView>(R.id.navigation_recycler_view)
        navigation_recycler_view.layoutManager = LinearLayoutManager(this)
        navigation_recycler_view.itemAnimator = DefaultItemAnimator()
        navigation_recycler_view.adapter = _navigationAdapter
        navigation_recycler_view.setHasFixedSize(true)

        val intent = Intent(this@MainActivity, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(this@MainActivity, System.currentTimeMillis().toInt(), intent, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            trackNotificationBuilder = NotificationCompat.Builder(this@MainActivity, CHANNEL_ID)
                    .setContentTitle("A track is playing in the background")
                    .setSmallIcon(R.drawable.echo_icon)
                    .setChannelId(CHANNEL_ID)
                    .setContentIntent(pIntent)
                    .setOngoing(true)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID, channelname, importance)
            notificationManager!!.createNotificationChannel(mChannel)
        } else {
            trackNotificationBuilder = Notification.Builder(this@MainActivity)
                    .setContentTitle("A track is playing in the background")
                    .setSmallIcon(R.drawable.echo_logo)
                    .setContentIntent(pIntent)
                    .setOngoing(true)
                    .setAutoCancel(true)
                    .build()
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }

    }

    override fun onStart() {
        super.onStart()
        try {
            notificationManager!!.cancel(notify_ID)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onStop() {
        super.onStop()
        try {
            if (SongPlayingFragment.mediaplayer!!.isPlaying) {
                notificationManager!!.notify(notify_ID, trackNotificationBuilder)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onDestroy() {
        if (this.isFinishing) {
            try {
                notificationManager!!.cancel(notify_ID)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            super.onDestroy()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            if (SongPlayingFragment.mediaplayer!!.isPlaying) {
                notificationManager!!.notify(notify_ID, trackNotificationBuilder)
                onStop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onResume() {
        super.onResume()
        try {
            notificationManager!!.cancel(notify_ID)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle?.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle?.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle!!.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    companion object {
        var drawerLayout: DrawerLayout? = null
        var notificationManager: NotificationManager? = null
    }

}
