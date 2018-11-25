package com.wackymemes.library_tablet

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class ReserveActivity : AppCompatActivity() {

    internal var reserveOnClickListener: View.OnClickListener = View.OnClickListener {
        val i = Intent(this@ReserveActivity, MainActivity::class.java)
        startActivity(i)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reserve)
        val reserveButton = findViewById<Button>(R.id.reserveApplyButton)
        reserveButton.setOnClickListener(reserveOnClickListener)
    }
}
