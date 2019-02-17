package com.archit.myplayer.fragments


import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.archit.myplayer.CurrentSongHelper
import com.archit.myplayer.R
import com.archit.myplayer.Songs
import com.archit.myplayer.databases.EchoDatabase

import java.io.IOException
import java.util.ArrayList
import java.util.Objects
import java.util.Random
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass.
 */
class SongPlayingFragment : Fragment() {

    internal var mAcceleration = 0f
    internal var mAccelerationCurrent = 0f
    internal var mAccelerationLast = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_song_playing, container, false)
        setHasOptionsMenu(true)
        activity!!.title = "Now Playing"
        seekbar = v.findViewById(R.id.seekBar)
        startTimeText = v.findViewById(R.id.startTime)
        endTimeText = v.findViewById(R.id.endTime)
        playpauseImageButton = v.findViewById(R.id.playPauseButton)
        nextImageButton = v.findViewById(R.id.nextButton)
        previousImageButton = v.findViewById(R.id.previousButton)
        loopImageButton = v.findViewById(R.id.loopButton)
        shuffleImageButton = v.findViewById(R.id.shuffleButton)
        songArtistView = v.findViewById(R.id.songArtist)
        songTitleView = v.findViewById(R.id.songTitle)
        glView = v.findViewById(R.id.visualizer_view)
        fab = v.findViewById(R.id.favouriteIcon)
        fab!!.alpha = 0.8f
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioVisualization = glView
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onResume() {
        super.onResume()
        audioVisualization!!.onResume()
        mSensorManager!!.registerListener(mSensorListener, mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)

    }

    override fun onPause() {
        audioVisualization!!.onPause()
        super.onPause()

        mSensorManager!!.unregisterListener(mSensorListener)
    }

    override fun onDestroyView() {
        audioVisualization!!.release()
        super.onDestroyView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mSensorManager = myActivity!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAcceleration = 0.0f
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH
        mAccelerationLast = SensorManager.GRAVITY_EARTH
        bindShakeListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu!!.clear()
        inflater!!.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val item = menu!!.findItem(R.id.action_redirect)
        item.isVisible = true

        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_redirect -> {
                myActivity!!.onBackPressed()
                return false
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        favouriteContent = EchoDatabase(myActivity)
        currentSongHelper = CurrentSongHelper()
        currentSongHelper!!.isPlaying = true
        currentSongHelper!!.isLoop = false
        currentSongHelper!!.isShuffle = false

        var path: String? = null
        val _songTitle: String?
        val _songArtist: String?
        val songId: Long?
        try {
            path = Objects.requireNonNull<Bundle>(arguments).getString("path")
            _songTitle = arguments!!.getString("songTitle")
            _songArtist = arguments!!.getString("songArtist")
            songId = arguments!!.getInt("songId").toLong()
            currentPosition = arguments!!.getInt("songPosition")
            fetchSongs = arguments!!.getParcelableArrayList("songData")

            currentSongHelper!!.songPath = path
            currentSongHelper!!.songTitle = _songTitle
            currentSongHelper!!.songArtist = _songArtist
            currentSongHelper!!.songId = songId
            currentSongHelper!!.currentPosition = currentPosition

            updateTextViews(currentSongHelper!!.songTitle, currentSongHelper!!.songArtist)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        val fromFavBottomBar = arguments!!.getString("FavBottomBar")
        val fromMainScreenBottomBar = arguments!!.getString("MainScreenBottomBar")
        if (fromFavBottomBar != null) {
            mediaplayer = FavouriteFragment.mediaPlayer
            currentSongHelper!!.isPlaying = FavouriteFragment.mediaPlayer!!.isPlaying
        } else if (fromMainScreenBottomBar != null) {
            mediaplayer = MainScreenFragment.mediaPlayer
            currentSongHelper!!.isPlaying = MainScreenFragment.mediaPlayer!!.isPlaying
        } else {
            mediaplayer = MediaPlayer()
            mediaplayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                mediaplayer!!.setDataSource(myActivity!!, Uri.parse(path))
                mediaplayer!!.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            mediaplayer!!.start()
        }
        processInformation(mediaplayer!!)

        if (currentSongHelper!!.isPlaying) {
            playpauseImageButton!!.setBackgroundResource(R.drawable.pause_icon)
        } else {
            playpauseImageButton!!.setBackgroundResource(R.drawable.play_icon)
        }
        mediaplayer!!.setOnCompletionListener { onSongComplete() }
        clickHandler()
        val visualizationHandler = DbmHandler.Factory.newVisualizerHandler(myActivity!!, 0)
        audioVisualization!!.linkTo(visualizationHandler)

        val prefsForShuffle = myActivity!!.getSharedPreferences(MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        val isShuffleAllowed = prefsForShuffle.getBoolean("feature", false)
        if (isShuffleAllowed) {
            currentSongHelper!!.isShuffle = true
            currentSongHelper!!.isLoop = false
            shuffleImageButton!!.setBackgroundResource(R.drawable.shuffle_icon)
            loopImageButton!!.setBackgroundResource(R.drawable.loop_white_icon)
        } else {
            currentSongHelper!!.isShuffle = false
            shuffleImageButton!!.setBackgroundResource(R.drawable.shuffle_white_icon)
        }
        val prefsForLoop = myActivity!!.getSharedPreferences(MY_PREFS_LOOP, Context.MODE_PRIVATE)
        val isLoopAllowed = prefsForLoop.getBoolean("feature", false)
        if (isLoopAllowed) {
            currentSongHelper!!.isShuffle = false
            currentSongHelper!!.isLoop = true
            shuffleImageButton!!.setBackgroundResource(R.drawable.shuffle_white_icon)
            loopImageButton!!.setBackgroundResource(R.drawable.loop_icon)
        } else {
            currentSongHelper!!.isLoop = false
            shuffleImageButton!!.setBackgroundResource(R.drawable.shuffle_white_icon)
        }
        if (favouriteContent!!.checkIfIdExists(currentSongHelper!!.songId!!)) {
            fab!!.setImageDrawable(ContextCompat.getDrawable(myActivity!!, R.drawable.favorite_on))
        } else {
            fab!!.setImageDrawable(ContextCompat.getDrawable(myActivity!!, R.drawable.favorite_off))
        }

        seekBarProgresschangeHandler()
    }

    fun seekBarProgresschangeHandler() {
        seekbar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, ontouch: Boolean) {
                if (mediaplayer != null && ontouch) {
                    val ctimeinminutes = TimeUnit.MILLISECONDS.toMinutes(progress.toLong()).toInt()
                    val ctimeinsecs = TimeUnit.MILLISECONDS.toSeconds(progress.toLong()).toInt() - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(progress.toLong())).toInt()
                    val min: String
                    val secs: String
                    if (ctimeinminutes < 10) {
                        min = "0$ctimeinminutes"
                    } else {
                        min = ctimeinminutes.toString()
                    }
                    if (ctimeinsecs < 10) {
                        secs = "0$ctimeinsecs"
                    } else {
                        secs = ctimeinsecs.toString()
                    }
                    val time = "$min:$secs"
                    startTimeText!!.text = time
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (mediaplayer != null && mediaplayer!!.isPlaying) {
                    mediaplayer!!.seekTo(seekBar.progress)
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun clickHandler() {

        fab!!.setOnClickListener {
            if (favouriteContent!!.checkIfIdExists(Math.toIntExact(currentSongHelper!!.songId!!).toLong())) {
                fab!!.setImageDrawable(ContextCompat.getDrawable(myActivity!!, R.drawable.favorite_off))
                favouriteContent!!.deleteFavourite(Math.toIntExact(currentSongHelper!!.songId!!))
                Toast.makeText(myActivity, "Removed From Favourites", Toast.LENGTH_SHORT).show()
            } else {
                fab!!.setImageDrawable(ContextCompat.getDrawable(myActivity!!, R.drawable.favorite_on))
                favouriteContent!!.storeAsFavourite(Math.toIntExact(currentSongHelper!!.songId!!), currentSongHelper!!.songArtist!!, currentSongHelper!!.songTitle!!, currentSongHelper!!.songPath!!)
                Toast.makeText(myActivity, "Added to Favourites", Toast.LENGTH_SHORT).show()
            }
        }

        shuffleImageButton!!.setOnClickListener(object : View.OnClickListener {
            internal var editorShuffle: SharedPreferences.Editor = myActivity!!.getSharedPreferences(MY_PREFS_SHUFFLE, Context.MODE_PRIVATE).edit()
            internal var editorLoop: SharedPreferences.Editor = myActivity!!.getSharedPreferences(MY_PREFS_LOOP, Context.MODE_PRIVATE).edit()

            override fun onClick(view: View) {
                if (currentSongHelper!!.isShuffle) {
                    shuffleImageButton!!.setBackgroundResource(R.drawable.shuffle_white_icon)
                    currentSongHelper!!.isShuffle = false
                    editorShuffle.putBoolean("feature", false)
                    editorShuffle.apply()
                } else {
                    currentSongHelper!!.isShuffle = true
                    currentSongHelper!!.isLoop = false
                    shuffleImageButton!!.setBackgroundResource(R.drawable.shuffle_icon)
                    loopImageButton!!.setBackgroundResource(R.drawable.loop_white_icon)
                    editorShuffle.putBoolean("feature", true)
                    editorShuffle.apply()
                    editorLoop.putBoolean("feature", false)
                    editorLoop.apply()
                }
            }
        })

        nextImageButton!!.setOnClickListener {
            currentSongHelper!!.isPlaying = true
            playpauseImageButton!!.setBackgroundResource(R.drawable.pause_icon)
            if (currentSongHelper!!.isShuffle) {
                playNext("PlayNextLikeNormalShuffle")
            } else {
                playNext("PlayNextNormal")
            }
        }

        previousImageButton!!.setOnClickListener {
            currentSongHelper!!.isPlaying = true
            if (currentSongHelper!!.isLoop) {
                loopImageButton!!.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()
        }

        loopImageButton!!.setOnClickListener {
            val editorShuffle = myActivity!!.getSharedPreferences(MY_PREFS_SHUFFLE, Context.MODE_PRIVATE).edit()
            val editorLoop = myActivity!!.getSharedPreferences(MY_PREFS_LOOP, Context.MODE_PRIVATE).edit()
            if (currentSongHelper!!.isLoop) {
                currentSongHelper!!.isLoop = false
                loopImageButton!!.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop.putBoolean("feature", false)
                editorLoop.apply()
            } else {
                currentSongHelper!!.isLoop = true
                currentSongHelper!!.isShuffle = false
                loopImageButton!!.setBackgroundResource(R.drawable.loop_icon)
                shuffleImageButton!!.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle.putBoolean("feature", false)
                editorShuffle.apply()
                editorLoop.putBoolean("feature", true)
                editorLoop.apply()
            }
        }

        playpauseImageButton!!.setOnClickListener {
            if (mediaplayer!!.isPlaying) {
                mediaplayer!!.pause()
                currentSongHelper!!.isPlaying = false
                playpauseImageButton!!.setBackgroundResource(R.drawable.play_icon)
            } else {
                mediaplayer!!.start()
                currentSongHelper!!.isPlaying = true
                playpauseImageButton!!.setBackgroundResource(R.drawable.pause_icon)
            }
        }
    }

    fun playPrevious() {
        currentPosition = currentPosition - 1
        if (currentPosition == -1) {
            currentPosition = 0
        }
        if (currentSongHelper!!.isPlaying) {
            playpauseImageButton!!.setBackgroundResource(R.drawable.pause_icon)
        } else {
            playpauseImageButton!!.setBackgroundResource(R.drawable.play_icon)
        }
        currentSongHelper!!.isLoop = false
        val nextSong = fetchSongs!![currentPosition]
        currentSongHelper!!.songTitle = nextSong.songTitle
        currentSongHelper!!.songPath = nextSong.songData
        currentSongHelper!!.currentPosition = currentPosition
        currentSongHelper!!.songId = nextSong.songID

        updateTextViews(currentSongHelper!!.songTitle, currentSongHelper!!.songArtist)

        mediaplayer!!.reset()
        try {
            mediaplayer!!.setDataSource(myActivity!!, Uri.parse(currentSongHelper!!.songPath))
            mediaplayer!!.prepare()
            mediaplayer!!.start()
            processInformation(mediaplayer!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (favouriteContent!!.checkIfIdExists(currentSongHelper!!.songId!!)) {
            fab!!.setImageDrawable(ContextCompat.getDrawable(myActivity!!, R.drawable.favorite_on))
        } else {
            fab!!.setImageDrawable(ContextCompat.getDrawable(myActivity!!, R.drawable.favorite_off))
        }
    }

    fun bindShakeListener() {
        mSensorListener = object : SensorEventListener {
            override fun onSensorChanged(sensorEvent: SensorEvent) {
                val x = sensorEvent.values[0]
                val y = sensorEvent.values[1]
                val z = sensorEvent.values[2]

                mAccelerationLast = mAccelerationCurrent
                mAccelerationCurrent = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val delta = mAccelerationCurrent - mAccelerationLast
                mAcceleration = mAcceleration * 0.9f + delta

                if (mAcceleration > 12) {
                    val prefs = myActivity!!.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)
                    val isAllowed = prefs.getBoolean("feature", false)
                    if (isAllowed) {
                        playNext("PlayNextNormal")
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, i: Int) {

            }
        }
    }

    companion object {
        var myActivity: Activity? = null
        var mediaplayer: MediaPlayer? = null
        var startTimeText: TextView? = null
        var endTimeText: TextView? = null
        var songArtistView: TextView? = null
        var songTitleView: TextView? = null
        var playpauseImageButton: ImageButton? = null
        var previousImageButton: ImageButton? = null
        var nextImageButton: ImageButton? = null
        var loopImageButton: ImageButton? = null
        var shuffleImageButton: ImageButton? = null
        var fab: ImageButton? = null
        var seekbar: SeekBar? = null
        var audioVisualization: AudioVisualization? = null
        var glView: GLAudioVisualizationView? = null

        var currentPosition = 0
        var fetchSongs: ArrayList<Songs>? = null
        var currentSongHelper: CurrentSongHelper? = null
        var MY_PREFS_SHUFFLE = "Shuffle feature"
        var MY_PREFS_LOOP = "Loop feature"

        var mSensorManager: SensorManager? = null
        var mSensorListener: SensorEventListener? = null
        var MY_PREFS_NAME = "ShakeFeature"

        internal val updateSongTime: Runnable = object : Runnable {
            override fun run() {
                val getCurrent = mediaplayer!!.currentPosition
                val ctimeinminutes = TimeUnit.MILLISECONDS.toMinutes(getCurrent.toLong()).toInt()
                val ctimeinsecs = TimeUnit.MILLISECONDS.toSeconds(getCurrent.toLong()).toInt() - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent.toLong())).toInt()
                val min: String
                val secs: String
                if (ctimeinminutes < 10) {
                    min = "0$ctimeinminutes"
                } else {
                    min = ctimeinminutes.toString()
                }
                if (ctimeinsecs < 10) {
                    secs = "0$ctimeinsecs"
                } else {
                    secs = ctimeinsecs.toString()
                }
                val time = "$min:$secs"
                startTimeText!!.text = time
                seekbar!!.progress = getCurrent
                val timeUpdateHandler = Handler()
                timeUpdateHandler.postDelayed(this, 1000)
            }
        }

        var favouriteContent: EchoDatabase? = null

        fun playNext(check: String) {
            if (check == "PlayNextNormal") {
                currentPosition = currentPosition + 1
            } else if (check == "PlayNextLikeNormalShuffle") {
                val randomObject = Random()
                val randomPosition = randomObject.nextInt(fetchSongs!!.size + 1)
                currentPosition = randomPosition
            }
            if (currentPosition == fetchSongs!!.size) {
                currentPosition = 0
            }
            currentSongHelper!!.isLoop = false
            val nextSong = fetchSongs!![currentPosition]
            currentSongHelper!!.songTitle = nextSong.songTitle
            currentSongHelper!!.songPath = nextSong.songData
            currentSongHelper!!.currentPosition = currentPosition
            currentSongHelper!!.songId = nextSong.songID

            updateTextViews(currentSongHelper!!.songTitle, currentSongHelper!!.songArtist)

            mediaplayer!!.reset()
            try {
                mediaplayer!!.setDataSource(myActivity!!, Uri.parse(currentSongHelper!!.songPath))
                mediaplayer!!.prepare()
                mediaplayer!!.start()
                processInformation(mediaplayer!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (favouriteContent!!.checkIfIdExists(currentSongHelper!!.songId!!)) {
                fab!!.setImageDrawable(ContextCompat.getDrawable(myActivity!!, R.drawable.favorite_on))
            } else {
                fab!!.setImageDrawable(ContextCompat.getDrawable(myActivity!!, R.drawable.favorite_off))
            }
        }

        fun onSongComplete() {
            if (currentSongHelper!!.isShuffle) {
                playNext("PlayNextLikeNormalShuffle")
                currentSongHelper!!.isPlaying = true
            } else {
                if (currentSongHelper!!.isLoop) {
                    currentSongHelper!!.isPlaying = true
                    val nextSong = fetchSongs!![currentPosition]
                    currentSongHelper!!.songTitle = nextSong.songTitle
                    currentSongHelper!!.songPath = nextSong.songData
                    currentSongHelper!!.currentPosition = currentPosition
                    currentSongHelper!!.songId = nextSong.songID

                    updateTextViews(currentSongHelper!!.songTitle, currentSongHelper!!.songArtist)

                    mediaplayer!!.reset()
                    try {
                        mediaplayer!!.setDataSource(myActivity!!, Uri.parse(currentSongHelper!!.songPath))
                        mediaplayer!!.prepare()
                        mediaplayer!!.start()
                        processInformation(mediaplayer!!)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                } else {
                    playNext("PlayNextNormal")
                    currentSongHelper!!.isPlaying = true
                }
            }
            if (favouriteContent!!.checkIfIdExists(currentSongHelper!!.songId!!)) {
                fab!!.setImageDrawable(ContextCompat.getDrawable(myActivity!!, R.drawable.favorite_on))
            } else {
                fab!!.setImageDrawable(ContextCompat.getDrawable(myActivity!!, R.drawable.favorite_off))
            }
        }

        fun updateTextViews(songTitle: String?, songArtist: String?) {
            var songTitle = songTitle
            if (songTitle === "<unknown>") {
                songTitle = "unknown"
            }
            songTitleView!!.text = songTitle
            songArtistView!!.text = songArtist
        }

        fun processInformation(mediaPlayer: MediaPlayer) {
            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition
            seekbar!!.max = finalTime
            var ctimeinminutes = TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()).toInt()
            var ctimeinsecs = TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()).toInt() - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime.toLong())).toInt()
            var min: String
            var secs: String
            if (ctimeinminutes < 10) {
                min = "0$ctimeinminutes"
            } else {
                min = ctimeinminutes.toString()
            }
            if (ctimeinsecs < 10) {
                secs = "0$ctimeinsecs"
            } else {
                secs = ctimeinsecs.toString()
            }
            var time = "$min:$secs"
            startTimeText!!.text = time
            ctimeinminutes = TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()).toInt()
            ctimeinsecs = TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()).toInt() - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong())).toInt()
            if (ctimeinminutes < 10) {
                min = "0$ctimeinminutes"
            } else {
                min = ctimeinminutes.toString()
            }
            if (ctimeinsecs < 10) {
                secs = "0$ctimeinsecs"
            } else {
                secs = ctimeinsecs.toString()
            }
            time = "$min:$secs"
            endTimeText!!.text = time
            seekbar!!.progress = startTime
            val mHandler = Handler()
            mHandler.postDelayed(updateSongTime, 1000)
        }
    }
}// Required empty public constructor
