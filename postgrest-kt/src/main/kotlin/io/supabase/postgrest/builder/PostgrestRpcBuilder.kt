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
import io.supabase.postgrest.util.Count
import io.supabase.postgrest.util.RequestMethod
import okhttp3.OkHttpClient

class PostgrestRpcBuilder<T : Any>(
    url: String,
    moshi: Moshi,
    schema: String?,
    client: OkHttpClient,
    headers: Map<String, String>
) : PostgrestBuilder<T>(
    url,
    moshi,
    schema,
    client,
    headers
) {

    /**
     * Perform a function call.
     */
    fun rpc(
        params: Map<String, Any> = emptyMap(),
        head: Boolean = false,
        count: Count? = null,
    ): PostgrestFilterBuilder<T> {
        if (head) {
            method = RequestMethod.HEAD
            params.forEach { (t, u) -> searchParams[t] = u.toString() }
        } else {
            method = RequestMethod.POST
            body = params
        }

        if (count != null) {
            val currentPrefer = headers["Prefer"]
            if (currentPrefer != null)
                headers["Prefer"] = "$currentPrefer,count=${count}"
            else
                headers["Prefer"] = "count=${count}"
        }
        return PostgrestFilterBuilder(this)
    }

}