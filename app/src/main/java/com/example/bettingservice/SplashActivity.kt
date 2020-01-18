package com.example.bettingservice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SplashActivity : AppCompatActivity() {

    private val DEBUG_TAG = "Splash Activity"
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        setupPermissions()
    }

    private fun start_app() {
        Handler().postDelayed(Runnable(){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }

    private val PERM_REQUEST_CODE = 100

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun setupPermissions(){
        if (!hasPermissions(this, permissions)) {
            makeRequest()
        }
        else
            start_app()
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this, permissions, PERM_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERM_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.i(DEBUG_TAG, "Permission has been granted by user")
                    start_app()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.i(DEBUG_TAG, "Permission has been denied by user")
                    finish()
                }
                return
            }

            else -> {
                // Ignore all other requests.
            }
        }
    }
}
