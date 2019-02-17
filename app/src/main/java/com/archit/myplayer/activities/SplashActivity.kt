package com.archit.myplayer.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

import com.archit.myplayer.R

class SplashActivity : AppCompatActivity() {
    internal var permissionString = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.RECORD_AUDIO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if (!hasPermission(this@SplashActivity, permissionString)) {
            ActivityCompat.requestPermissions(this@SplashActivity, permissionString, 131)
        } else {
            splashEndStartAct()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 131) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[3] == PackageManager.PERMISSION_GRANTED
                    && grantResults[4] == PackageManager.PERMISSION_GRANTED) {
                splashEndStartAct()
            } else {
                Toast.makeText(this@SplashActivity, "Plaese grant all the permissions", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            Toast.makeText(this@SplashActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    fun splashEndStartAct() {
        Handler().postDelayed({
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }, 1000)
    }

    internal fun hasPermission(context: Context, permissions: Array<String>): Boolean {
        var hasAllPermissions = true
        for (permission in permissions) {
            val res = context.checkCallingOrSelfPermission(permission)
            if (res != PackageManager.PERMISSION_GRANTED)
                hasAllPermissions = false
        }
        return hasAllPermissions
    }
}
