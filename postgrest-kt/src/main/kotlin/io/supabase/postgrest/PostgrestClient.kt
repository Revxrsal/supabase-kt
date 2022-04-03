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
package io.supabase.postgrest

import com.squareup.moshi.Moshi
import io.supabase.common.MOSHI
import io.supabase.postgrest.builder.PostgrestFilterBuilder
import io.supabase.postgrest.builder.PostgrestQueryBuilder
import io.supabase.postgrest.builder.PostgrestRpcBuilder
import io.supabase.postgrest.util.Count
import okhttp3.OkHttpClient

class PostgrestClient(
    private val url: String,
    private val authorization: String,
    private val schema: String? = null,
    private val client: OkHttpClient = OkHttpClient(),
    private val moshi: Moshi = MOSHI,
    headers: Map<String, String> = emptyMap()
) {

    private val headers = buildMap {
        putAll(headers)
        put("Authorization", "Bearer $authorization")
        put("apikey", authorization)
    }

    /**
     * Perform a table operation.
     *
     * @param table  The table name to operate on.
     */
    fun <T : Any> from(table: String): PostgrestQueryBuilder<T> {
        val url = "${this.url}/${table}"
        return PostgrestQueryBuilder(url, moshi, schema, client, headers)
    }

    /**
     * Perform a function call.
     *
     * @param fn  The function name to call.
     * @param params  The parameters to pass to the function call.
     * @param head  When set to true, no data will be returned.
     * @param count  Count algorithm to use to count rows in a table.
     */
    fun <T : Any> rpc(
        fn: String,
        params: Map<String, Any> = emptyMap(),
        head: Boolean = false,
        count: Count? = null
    ): PostgrestFilterBuilder<T> {
        val url = "${this.url}/rpc/${fn}"
        return PostgrestRpcBuilder<T>(url, moshi, schema, client, headers).rpc(params, head, count)
    }

}