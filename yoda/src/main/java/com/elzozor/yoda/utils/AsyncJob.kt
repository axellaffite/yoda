package com.elzozor.yoda.utils

import android.os.AsyncTask

open class AsyncJob<T, U>(
    private val async: (List<T?>) -> U? = {null},
    private val callback: (U?) -> Unit = {}
) : AsyncTask<T, Unit, U>()
{
    var values = mutableMapOf<String, Any>()

    override fun doInBackground(vararg params: T?) = async(params.asList())

    override fun onPostExecute(result: U?) = callback(result)

    fun putValue(key: String, value : Any) = values.put(key, value)
    fun getValue(key: String, default: Any? = null) = values.getOrElse(key, {default})
}