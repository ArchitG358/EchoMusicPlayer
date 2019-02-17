package com.archit.myplayer

class CurrentSongHelper {
    var songArtist: String? = null
    var songTitle: String? = null
    var songPath: String? = null
    var songId: Long? = 0L
    var currentPosition = 0
    var trackPosition = 0
    var isPlaying = false
    var isLoop = false
    var isShuffle = false
}
