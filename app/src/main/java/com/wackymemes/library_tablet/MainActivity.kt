package com.wackymemes.library_tablet

import android.app.AlertDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ActionMenuView
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity(), MenuItem.OnMenuItemClickListener {

    var settingsMenu: MenuItem? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        settingsMenu = menu.add("Settings").setOnMenuItemClickListener(this)
        val pref = android.R.drawable.ic_menu_preferences
        if (pref != null)
            settingsMenu?.setIcon(pref)
        return true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (item === settingsMenu) {
            val i = Intent(this, SettingsActivity::class.java)
            startActivity(i)
        }
        return true
    }

    internal var recognizeOnClickListerner: View.OnClickListener = View.OnClickListener {
        val i = Intent(this@MainActivity, CameraActivity::class.java)
        startActivity(i)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //val i = Intent(this@MainActivity, CameraActivity::class.java)
        //startActivity(i)
        val recognizeButton = findViewById<Button>(R.id.recognizeButton)
        recognizeButton.setOnClickListener(recognizeOnClickListerner)
    }
}
