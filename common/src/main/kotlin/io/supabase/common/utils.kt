/*
 * This file is part of gotrue-kt, licensed under the MIT License.
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
import io.supabase.common.json.CaseInsensitiveEnumsAdapterFactory
import io.supabase.common.json.OffsetDateAdapter
import io.supabase.common.json.UUIDAdapter
import io.supabase.common.json.typeOf
import okhttp3.MediaType.Companion.toMediaType
import java.net.URLDecoder
import java.net.URLEncoder
import java.time.OffsetDateTime
import java.util.*

fun String.encodeURI() = URLEncoder.encode(this, "UTF-8").replace("+", "%20")

fun currentTimeSeconds() = System.currentTimeMillis() / 1000

val MOSHI: Moshi = Moshi.Builder()
    .add(CaseInsensitiveEnumsAdapterFactory)
    .add(UUID::class.java, UUIDAdapter)
    .add(OffsetDateTime::class.java, OffsetDateAdapter)
    .build()

inline fun <reified T> Moshi.adapterOf(): JsonAdapter<T> {
    return adapter(typeOf<T>())
}

val Int.isAccepted get() = this in 200..299

val APPLICATION_JSON = "application/json".toMediaType()

val String.queryParams: Map<String, List<String>>
    get() {
        val urlParts = split('?')
        val map = buildMap<String, MutableList<String>> {
            if (urlParts.size > 1) {
                val query = urlParts[1]
                for (param in query.split('&')) {
                    val pair = param.split('=')
                    val key = URLDecoder.decode(pair[0], "UTF-8")
                    val value = pair.getOrNull(1)?.run { URLDecoder.decode(this, "UTF-8") }.orEmpty()
                    this.getOrPut(key) { mutableListOf() }.add(value)
                }
            }
        }
        return map
    }
