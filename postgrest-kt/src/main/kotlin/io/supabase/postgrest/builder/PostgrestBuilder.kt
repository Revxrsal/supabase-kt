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
package io.supabase.postgrest.builder

import com.squareup.moshi.Moshi
import io.supabase.common.APPLICATION_JSON
import io.supabase.common.adapterOf
import io.supabase.common.encodeURI
import io.supabase.common.isAccepted
import io.supabase.postgrest.PostgrestException
import io.supabase.postgrest.util.RequestMethod
import io.supabase.postgrest.util.RequestMethod.GET
import io.supabase.postgrest.util.RequestMethod.HEAD
import okhttp3.Headers
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

open class PostgrestBuilder<T : Any> {

    private val url: String
    protected val moshi: Moshi
    private val client: OkHttpClient
    private val schema: String?
    protected var body: Any? = null
    protected val headers: MutableMap<String, String>
    protected val searchParams = mutableMapOf<String, String>()
    protected var method: RequestMethod = GET

    constructor(other: PostgrestBuilder<T>) {
        url = other.url
        moshi = other.moshi
        schema = other.schema
        body = other.body
        headers = other.headers.toMutableMap()
        searchParams.putAll(other.searchParams)
        method = other.method
        client = other.client
    }

    constructor(
        url: String,
        moshi: Moshi,
        schema: String? = null,
        client: OkHttpClient,
        headers: Map<String, String>
    ) {
        this.url = url
        this.moshi = moshi
        this.schema = schema
        this.client = client
        this.headers = headers.toMutableMap()
    }

    private fun toRequest(): Request {
        val parameters = searchParams.entries.joinToString("&") { (name, value) ->
            "${name.encodeURI()}=${value.encodeURI()}"
        }
        val requestBody = if (body != null) moshi.adapter(body!!.javaClass).toJson(body) else null
        val headers = Headers.Builder()
        headers.addAll(this.headers.toHeaders())

        val builder = Request.Builder()
            .url(url + if (parameters.isEmpty()) "" else "?$parameters")
        if (method.takesBody)
            builder.method(method.name, requestBody?.toRequestBody(APPLICATION_JSON))
        else
            builder.method(method.name, null)
        if (schema != null) when (method) {
            GET, HEAD -> headers.add("Accept-Profile", schema)
            else -> headers.add("Content-Type", schema)
        }
        builder.headers(headers.build())
        return builder.build()
    }

    fun execute(): PostgrestResponse {
        val response = client.newCall(toRequest()).execute()
        if (!response.code.isAccepted) {
            val map = moshi.adapterOf<Map<String, Any>>().fromJson(response.body!!.source())!!
            val message = buildString {
                append(map["msg"] ?: map["message"] ?: map["error"] ?: map).toString()
                if (map["details"].toString() != "null")
                    append(": ${map["details"]}")
                if (map.containsKey("hint"))
                    append(": ${map["hint"]}")
            }
            throw PostgrestException(message)
        }
        return PostgrestResponse(moshi, response)

    }

}