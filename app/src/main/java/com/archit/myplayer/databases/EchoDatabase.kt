package com.archit.myplayer.databases

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import com.archit.myplayer.Songs

import java.util.ArrayList

class EchoDatabase : SQLiteOpenHelper {
    internal var _songList = ArrayList<Songs>()

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE $TABLE_NAME ( $COLUMN_ID INTEGER PRIMARY KEY,$COLUMN_SONG_ARTIST TEXT,$COLUMN_SONG_TITLE TEXT,$COLUMN_SONG_PATH TEXT);")
    }

    override fun onUpgrade(sqliteDatabase: SQLiteDatabase, i: Int, i1: Int) {

    }

    fun storeAsFavourite(id: Int, artist: String, songTitle: String, path: String) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_ID, id)
        contentValues.put(COLUMN_SONG_ARTIST, artist)
        contentValues.put(COLUMN_SONG_TITLE, songTitle)
        contentValues.put(COLUMN_SONG_PATH, path)
        db.insert(TABLE_NAME, null, contentValues)
        db.close()
    }

    fun queryDBList(): ArrayList<Songs>? {
        try {
            val db = this.readableDatabase
            val query_params = "SELECT * FROM $TABLE_NAME"
            val cSor = db.rawQuery(query_params, null)
            if (cSor.moveToFirst()) {
                do {
                    val _id = cSor.getInt(cSor.getColumnIndexOrThrow(COLUMN_ID))
                    val _artist = cSor.getString(cSor.getColumnIndexOrThrow(COLUMN_SONG_ARTIST))
                    val _title = cSor.getString(cSor.getColumnIndexOrThrow(COLUMN_SONG_TITLE))
                    val _songPath = cSor.getString(cSor.getColumnIndexOrThrow(COLUMN_SONG_PATH))
                    _songList.add(Songs(_id.toLong(), _title, _artist, _songPath, 0.toLong()))
                } while (cSor.moveToNext())
            } else {
                return null
            }
            cSor.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return _songList
    }

    fun checkIfIdExists(_id: Long): Boolean {
        var storeId = -1090
        val db = this.readableDatabase
        val query_params = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = " + _id.toInt()
        val cSor = db.rawQuery(query_params, null)
        if (cSor.moveToFirst()) {
            do {
                storeId = cSor.getColumnIndexOrThrow(COLUMN_ID)
            } while (cSor.moveToNext())
        } else {
            return false
        }
        cSor.close()
        return storeId != -1090
    }

    fun deleteFavourite(_id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID=$_id", null)
        db.close()
    }

    fun checkSize(): Int {
        var counter = 0
        val db = this.readableDatabase
        val query_params = "SELECT * FROM $TABLE_NAME"
        val cSor = db.rawQuery(query_params, null)
        if (cSor.moveToFirst()) {
            do {
                counter = counter + 1
            } while (cSor.moveToNext())
        } else {
            return 0
        }
        cSor.close()
        return counter
    }

    constructor(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : super(context, name, factory, version) {}

    constructor(context: Context?) : super(context, DB_NAME, null, DB_VERSION) {}

    companion object {
        var DB_NAME = "FavouriteDatabase"
        var TABLE_NAME = "FavouriteTable"
        var COLUMN_ID = "SongID"
        var COLUMN_SONG_TITLE = "SongTitle"
        var COLUMN_SONG_ARTIST = "SongArtist"
        var COLUMN_SONG_PATH = "SongPath"
        var DB_VERSION = 1
    }
}
