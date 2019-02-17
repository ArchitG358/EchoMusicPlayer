package com.archit.myplayer

import android.os.Parcel
import android.os.Parcelable

import java.util.Comparator

class Songs : Parcelable {
    var songID: Long? = null
    var songTitle: String? = null
    var artist: String? = null
    var songData: String? = null
    var dateAdded: Long? = null

    constructor(currentId: Long?, currentTitle: String, currentArtist: String, currentData: String, currentDate: Long?) {
        this.songID = currentId
        this.songTitle = currentTitle
        this.artist = currentArtist
        this.songData = currentData
        this.dateAdded = currentDate
    }

    protected constructor(`in`: Parcel) {
        if (`in`.readByte().toInt() == 0) {
            songID = null
        } else {
            songID = `in`.readLong()
        }
        songTitle = `in`.readString()
        artist = `in`.readString()
        songData = `in`.readString()
        if (`in`.readByte().toInt() == 0) {
            dateAdded = null
        } else {
            dateAdded = `in`.readLong()
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        if (songID == null) {
            parcel.writeByte(0.toByte())
        } else {
            parcel.writeByte(1.toByte())
            parcel.writeLong(songID!!)
        }
        parcel.writeString(songTitle)
        parcel.writeString(artist)
        parcel.writeString(songData)
        if (dateAdded == null) {
            parcel.writeByte(0.toByte())
        } else {
            parcel.writeByte(1.toByte())
            parcel.writeLong(dateAdded!!)
        }
    }

    companion object {

        var nameComparator: Comparator<Songs> = Comparator { song1, song2 ->
            val songOne = song1.songTitle?.toUpperCase()
            val songTwo = song2.songTitle?.toUpperCase()
            songOne!!.compareTo(songTwo!!)
        }
        var dateComparator: Comparator<Songs> = Comparator { song1, song2 ->
            val songOne = song1.dateAdded.toString()
            val songTwo = song2.dateAdded.toString()
            songTwo.compareTo(songOne)
        }

        val CREATOR: Parcelable.Creator<Songs> = object : Parcelable.Creator<Songs> {
            override fun createFromParcel(`in`: Parcel): Songs {
                return Songs(`in`)
            }

            override fun newArray(size: Int): Array<Songs?> {
                return arrayOfNulls(size)
            }
        }
    }

     object CREATOR : Parcelable.Creator<Songs> {
        override fun createFromParcel(parcel: Parcel): Songs {
            return Songs(parcel)
        }

        override fun newArray(size: Int): Array<Songs?> {
            return arrayOfNulls(size)
        }
    }
}
