package com.mywings.reactnativerunner

import android.os.AsyncTask
import org.json.JSONObject

class FromContactAsync : AsyncTask<JSONObject, Void, String>() {

    private lateinit var onContactListener: OnContactListener
    private val httpConnectionUtil = HttpConnectionUtil()

    override fun doInBackground(vararg params: JSONObject?): String {
        return httpConnectionUtil.requestPost(
            "http://globemindstechnologies.com/MyRestService.svc/FromContacts",
            params[0]
        )
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        onContactListener.onContactSuccess(result)
    }

    fun setOnContactListener(onContactListener: OnContactListener, request: JSONObject) {
        this.onContactListener = onContactListener
        executeOnExecutor(THREAD_POOL_EXECUTOR, request)
    }

}