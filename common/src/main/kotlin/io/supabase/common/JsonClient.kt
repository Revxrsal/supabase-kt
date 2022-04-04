/*
 * This file is part of supabase-kt, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package io.supabase.common

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.supabase.common.json.typeOf
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody

abstract class JsonClient(
    protected val client: OkHttpClient,
    protected val moshi: Moshi,
    protected val headers: Headers,
    protected val wrapException: (String) -> Exception
) {

    /**
     * Generates a request and provides a friendly DSL for configuring it
     */
    protected fun request(url: String, init: Request.Builder.() -> Unit): Request {
        val builder = Request.Builder()
            .headers(headers)
            .url(url)
        builder.init()
        return builder.build()
    }

    /**
     * Sends the request to the specified endpoint and provides a callback for
     * processing the response
     */
    protected inline infix fun <R> Request.whenResponds(onResponse: (Response) -> R): R {
        client.newCall(this).execute().use {
            if (it.code.isAccepted) {
                return onResponse(it)
            }
            val map = adapter<Map<String, Any>>().fromJson(it)
            val message = (map["error_description"] ?: map["message"] ?: map["error"] ?: map["msg"] ?: map).toString()
            throw wrapException(message)
        }
    }

    /**
     * Sends the request to the specified endpoint
     */
    protected fun Request.send() {
        client.newCall(this).execute().use {
            if (!it.code.isAccepted) {
                val map = adapter<Map<String, Any>>().fromJson(it)
                val message = (map["msg"] ?: map["message"] ?: map["error"] ?: map).toString()
                throw wrapException(message)
            }
        }
    }

    protected inline fun <reified T> adapter(): JsonAdapter<T> = moshi.adapter(typeOf<T>())

    protected inline fun <reified T> T.toRequestBody(): RequestBody {
        return adapter<T>().toJson(this).toRequestBody(APPLICATION_JSON)
    }

    protected inline fun <reified K, reified V> Request.Builder.post(vararg body: Pair<K, V>) {
        val adapter: JsonAdapter<Map<K, V>> =
            moshi.adapter(Types.newParameterizedType(Map::class.java, typeOf<K>(), typeOf<V>()))
        post(adapter.toJson(body.toMap()).toRequestBody(APPLICATION_JSON))
    }

    protected fun <T> JsonAdapter<T>.fromJson(it: Response): T {
        @Suppress("UNCHECKED_CAST")
        return fromJson(it.body!!.source()) as T
    }

}