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
                    Log.d(TAG, response.toString())
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
                    Log.d(TAG, books.toString())

                    var bookViewIterator = 0

                    for (i in 0..(loans.length() - 1)) {
                        val loan = loans.getJSONObject(i)
                        for (j in 0..(books.length() - 1)) {
                            val book = books.getJSONObject(j)

                            if (loan.getInt("item") == book.getInt("id")) {
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

                        if (action == "reserve") {
                            loanBook(bookId, userId, action)
                        } else { // action == "return"
                            returnBook(bookId, userId, action)
                        }
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

    fun loanBook (bookId: Int, userId: Int, action: String) {
        Log.d(TAG, "Varataan kirja")

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this.context)
        val url = "http://naamataulu-backend.herokuapp.com/api/v1/library/loans/"

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        val formattedDate = sdf.format(Date())

        val json = JSONObject(mapOf(
                "date_loaned" to formattedDate,
                "returned" to false,
                "item" to bookId,
                "person" to userId
        ))
        Log.d(TAG, json.toString())

        val request = object : JsonObjectRequest(com.android.volley.Request.Method.POST, url, json,
                com.android.volley.Response.Listener { response ->
                    // response
                    Log.d(TAG, response.toString())
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

    fun returnBook (bookId: Int, userId: Int, action: String) {
        Log.d(TAG, "Palautetaan kirja")

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this.context)
        val url = "http://naamataulu-backend.herokuapp.com/api/v1/library/loans/"

        val stringRequest = object : StringRequest(com.android.volley.Request.Method.GET, url,
                com.android.volley.Response.Listener { response ->
                    // response
                    val loans = JSONArray(response)

                    for (index in 0..(loans.length() - 1)) {
                        var loan = loans.getJSONObject(index)
                        if (loan.getInt("item") == bookId && loan.getInt("person") == userId) {
                            val loanId = loan.getInt("id")
                            Log.d(TAG, loanId.toString())

                            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                            val formattedDate = sdf.format(Date())

                            val json = JSONObject(mapOf(
                                    "date_loaned" to formattedDate,
                                    "returned" to true,
                                    "item" to bookId,
                                    "person" to userId
                            ))
                            Log.d(TAG, json.toString())

                            var returnUrl = url + loanId + "/"
                            Log.d(TAG, returnUrl)

                            val request = object : JsonObjectRequest(com.android.volley.Request.Method.PUT, returnUrl, json,
                                    com.android.volley.Response.Listener { response ->
                                        // response
                                        Log.d(TAG, response.toString())
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

                            break;
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
}