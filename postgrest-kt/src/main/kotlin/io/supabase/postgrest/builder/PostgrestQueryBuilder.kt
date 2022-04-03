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
import io.supabase.postgrest.builder.Returning.REPRESENTATION
import io.supabase.postgrest.clean
import io.supabase.postgrest.util.Count
import io.supabase.postgrest.util.RequestMethod.*
import okhttp3.OkHttpClient

class PostgrestQueryBuilder<T : Any>(
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
     * Performs vertical filtering with SELECT.
     *
     * @param columns  The columns to retrieve, separated by commas.
     * @param head  When set to true, select will void data.
     * @param count  Count algorithm to use to count rows in a table.
     */
    fun select(
        columns: String = "*",
        head: Boolean = false,
        count: Count? = null
    ): PostgrestFilterBuilder<T> {
        method = GET
        searchParams["select"] = columns.clean()
        if (count != null)
            headers["Prefer"] = "count=${count.name}"
        if (head)
            method = HEAD
        return PostgrestFilterBuilder(this)
    }

    /**
     * Performs an INSERT into the table.
     *
     * @param values  The values to insert.
     * @param onConflict  By specifying the [onConflict] query parameter, you can make UPSERT work on a column(s) that has a UNIQUE constraint.
     * @param returning  By default, the new record is returned. Set this to [Returning.MINIMAL] if you don't need this value.
     * @param upsert Should it merge with any existing values or override them
     * @param count  Count algorithm to use to count rows in a table.
     */
    fun insert(
        vararg values: T,
        returning: Returning = REPRESENTATION,
        upsert: Boolean = false,
        onConflict: String? = null,
        count: Count? = null
    ): PostgrestFilterBuilder<T> {
        method = POST
        val prefersHeaders = mutableListOf("return=${returning}")
        body = values
        if (upsert)
            prefersHeaders.add(0, "resolution=merge-duplicates")
        if (upsert && onConflict != null)
            searchParams["on_conflict"] = onConflict
        if (count != null)
            prefersHeaders.add(0, "count=${count}")
        headers["Prefer"] = prefersHeaders.joinToString(",")
        return PostgrestFilterBuilder(this)
    }

    /**
     * Performs an UPSERT into the table.
     *
     * @param value  The values to insert.
     * @param onConflict  By specifying the [onConflict] query parameter, you can make UPSERT work on a column(s) that has a UNIQUE constraint.
     * @param returning  By default, the new record is returned. Set this to 'minimal' if you don't need this value.
     * @param count  Count algorithm to use to count rows in a table.
     * @param ignoreDuplicates  Specifies if duplicate rows should be ignored and not inserted.
     */
    fun upsert(
        value: Pair<String, T>,
        returning: Returning = REPRESENTATION,
        count: Count? = null,
        ignoreDuplicates: Boolean = false,
        onConflict: String? = null
    ): PostgrestFilterBuilder<T> {
        method = POST
        body = value
        val prefersHeaders = mutableListOf(
            "resolution=${if (ignoreDuplicates) "ignore" else "merge"}-duplicates",
            "return=${returning}",
        )
        if (onConflict != null)
            searchParams["on_conflict"] = onConflict
        if (count != null)
            prefersHeaders.add(0, "count=${count}")
        headers["Prefer"] = prefersHeaders.joinToString(",")
        return PostgrestFilterBuilder(this)
    }

    /**
     * Performs a DELETE on the table.
     *
     * @param returning  If `true`, return the deleted row(s) in the response.
     * @param count  Count algorithm to use to count rows in a table.
     */
    fun delete(
        returning: Returning = REPRESENTATION,
        count: Count? = null
    ) {
        method = DELETE
        val prefersHeaders = mutableListOf("return=${returning.name}")
        if (count != null)
            prefersHeaders.add(0, "count=${count}")
        this.headers["Prefer"] = prefersHeaders.joinToString(",")
    }
}