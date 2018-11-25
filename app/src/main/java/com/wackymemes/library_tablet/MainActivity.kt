package com.wackymemes.library_tablet

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity() {

    internal var recognizeOnClickListerner: View.OnClickListener = View.OnClickListener {
        val i = Intent(this@MainActivity, PersonalActivity::class.java)
        startActivity(i)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val recognizeButton = findViewById<Button>(R.id.recognizeButton)
        recognizeButton.setOnClickListener(recognizeOnClickListerner)
    }
}
