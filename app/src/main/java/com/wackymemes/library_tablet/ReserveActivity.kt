package com.wackymemes.library_tablet

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView

class ReserveActivity : AppCompatActivity() {

    private val context: Context = this
    internal var preferences: PreferenceManager = PreferenceManager.getInstance(context)

    internal var reserveOnClickListener: View.OnClickListener = View.OnClickListener {
        val i = Intent(this@ReserveActivity, MainActivity::class.java)

        val library = LibraryAPI(context)
        val barcode = intent.extras.getString("scanResult")
        val userId = intent.extras.getInt("userId")
        val action = intent.extras.getString("action")

        Log.d("Response", "barcode: " + barcode + ", userid: " + userId)

        library.findBook(barcode, userId, action)

        startActivity(i)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reserve)
        val reserveButton = findViewById<Button>(R.id.reserveApplyButton)
        val extras = intent.extras

        reserveButton.setText(extras.getString("action"))

        var barcode = "invalid barcode"
        if (extras == null) {
            barcode = "barcode not found"
        } else {
            barcode = extras.getString("scanResult")
        }
        val barcodeTextView = findViewById<View>(R.id.barcodeTextView) as TextView
        barcodeTextView.text = barcode
        reserveButton.setOnClickListener(reserveOnClickListener)
    }
}
