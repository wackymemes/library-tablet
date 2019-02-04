package com.wackymemes.library_tablet

import android.content.Context
import android.util.Log
import android.widget.Button

import com.android.volley.AuthFailureError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.*
import java.text.SimpleDateFormat

class LibraryAPI(context: Context) {

    val TAG = "LibraryAPI"

    var context = context
    internal var preferences = PreferenceManager.getInstance(context)

    init {
    }

    fun getLoans (userId: Int, bookView: Array<Button>) {
        Log.d(TAG, "Haetaan lainat")

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this.context)
        val url = "http://naamataulu-backend.herokuapp.com/api/v1/library/loans/"

        val stringRequest = object : StringRequest(com.android.volley.Request.Method.GET, url,
                com.android.volley.Response.Listener { response ->
                    // response
                    getBooks(JSONArray(response), bookView)
                },
                com.android.volley.Response.ErrorListener { error ->
                    Log.e(TAG, "error => " + error.toString())
                }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = "Token " + preferences.getToken()

                return params
            }
        }
        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    fun getBooks (loans: JSONArray, bookView: Array<Button>) {
        Log.d(TAG, "Haetaan lainatut kirjat")

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this.context)
        val url = "http://naamataulu-backend.herokuapp.com/api/v1/library/books/"

        val stringRequest = object : StringRequest(com.android.volley.Request.Method.GET, url,
                com.android.volley.Response.Listener { response ->
                    // response
                    val books = JSONArray(response)

                    var bookViewIterator = 0

                    for (i in 0..(loans.length() - 1)) {
                        val loan = loans.getJSONObject(i)
                        for (j in 0..(books.length() - 1)) {
                            val book = books.getJSONObject(j)

                            if (loan.getInt("item") == loan.getInt("id")) {
                                if (bookViewIterator < bookView.size) {
                                    bookView[bookViewIterator].setText(book.getString("title"))
                                    bookViewIterator += 1
                                } else {
                                    break
                                }
                            }
                        }
                    }
                },
                com.android.volley.Response.ErrorListener { error ->
                    Log.e(TAG, "error => " + error.toString())
                }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = "Token " + preferences.getToken()

                return params
            }
        }
        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    fun findBook (isbn: String, userId: Int, action: String) {
        Log.d(TAG, "Haetaan kirjan id")

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this.context)
        val url = "http://naamataulu-backend.herokuapp.com/api/v1/library/books/?ISBN=$isbn"

        val stringRequest = object : StringRequest(com.android.volley.Request.Method.GET, url,
                com.android.volley.Response.Listener { response ->
                    // response
                    val book = JSONArray(response)
                    try {
                        val bookId = book.getJSONObject(0).getInt("id")

                        loanOrReturn(bookId, userId, action)
                    } catch (e: JSONException) {
                        Log.e(TAG, "Cannot read JSON $e")
                    }
                },
                com.android.volley.Response.ErrorListener { error ->
                    Log.e(TAG, "error => " + error.toString())
                }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = "Token " + preferences.getToken()

                return params
            }
        }
        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    fun loanOrReturn (bookId: Int, userId: Int, action: String) {
        var returned: Boolean
        if (action.equals("reserve")) {
            Log.d(TAG, "Varataan kirja")
            returned = false
        } else { // action.equals("return")
            Log.d(TAG, "Palautetaan kirja")
            returned = true
        }

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this.context)
        val url = "http://naamataulu-backend.herokuapp.com/api/v1/library/loans/"

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        val formattedDate = sdf.format(Date())

        val json = JSONObject(mapOf(
                "date_loaned" to formattedDate,
                "returned" to returned,
                "item" to bookId,
                "person" to userId
        ))

        val request = object : JsonObjectRequest(url, json,
                com.android.volley.Response.Listener { response ->
                    // response
                },
                com.android.volley.Response.ErrorListener { error ->
                    Log.e(TAG, "error => " + error.toString())
                }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = "Token " + preferences.getToken()

                return params
            }
        }
        // Add the request to the RequestQueue.
        queue.add(request)
    }
}