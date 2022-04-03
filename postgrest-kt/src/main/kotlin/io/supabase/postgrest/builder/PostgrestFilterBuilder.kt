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

import io.supabase.postgrest.clean
import io.supabase.postgrest.util.Filter
import io.supabase.postgrest.withDot
import io.supabase.postgrest.wrapIfReserved

class PostgrestFilterBuilder<T : Any>(other: PostgrestBuilder<T>) : PostgrestBuilder<T>(other) {

    /**
     * Performs vertical filtering with SELECT.
     *
     * @param columns  The columns to retrieve, separated by commas.
     */
    fun select(columns: String = "*") = apply {
        searchParams["select"] = columns.clean()
    }

    /**
     * Orders the result with the specified [column].
     *
     * @param column  The column to order on.
     * @param ascending  If true, the result will be in ascending order.
     * @param nullsFirst  If true, nulls appear first.
     * @param foreignTable  The foreign table to use (if [column] is a foreign column).
     */
    fun order(
        column: String,
        ascending: Boolean = true,
        nullsFirst: Boolean = false,
        foreignTable: String? = null
    ) = apply {
        val order = foreignTable.withDot("order")
        val existingOrder = searchParams[order]
        searchParams[order] = buildString {
            if (existingOrder != null) append("$existingOrder,")
            append(column)
            append('.')
            append(if (ascending) "asc" else "desc")
            append('.')
            append(if (nullsFirst) "nullsfirst" else "nullslast")
        }
    }

    /**
     * Limits the result with the specified [count].
     *
     * @param count  The maximum number of rows to limit to.
     * @param foreignTable  The foreign table to use (for foreign columns).
     */
    fun limit(
        count: Int,
        foreignTable: String? = null
    ) = apply {
        val limit = foreignTable.withDot("limit")
        searchParams[limit] = count.toString()
    }

    /**
     * Limits the result to rows within the specified range, inclusive.
     *
     * @param from  The starting index from which to limit the result, inclusive.
     * @param to  The last index to which to limit the result, inclusive.
     * @param foreignTable  The foreign table to use (for foreign columns).
     */
    fun range(
        from: Int,
        to: Int,
        foreignTable: String? = null
    ) = apply {
        val offset = foreignTable.withDot("offset")
        val limit = foreignTable.withDot("limit")
        searchParams[offset] = from.toString()
        searchParams[limit] = "${to - from + 1}"
    }

    /**
     * Retrieves only one row from the result. Result must be one row (e.g. using
     * "limit"), otherwise this will result in an error.
     */
    fun single() = apply {
        this.headers["Accept"] = "application/vnd.pgrst.object+json"
    }

    /**
     * Set the response type to CSV.
     */
    fun csv() = apply {
        this.headers["Accept"] = "text/csv"
    }

    /**
     * Finds all rows which don't satisfy the filter.
     *
     * @param column  The column to filter on.
     * @param filter  The operator to filter with.
     * @param value  The value to filter with.
     */
    fun not(column: String, filter: Filter, value: Any?) = apply {
        searchParams[column] = filter.not(value)
    }

    /**
     * Finds all rows satisfying at least one of the filters.
     *
     * @param filters  The filters to use, separated by commas.
     * @param foreignTable  The foreign table to use (if "column" is a foreign column).
     */
    fun or(filters: String, foreignTable: String? = null): PostgrestFilterBuilder<T> {
        searchParams[foreignTable.withDot("or")] = "(${filters})"
        return this
    }

    /**
     * Finds all rows satisfying at least one of the filters.
     *
     * @param filters  The filters to use, separated by commas.
     * @param foreignTable  The foreign table to use (if the column is a foreign column).
     */
    fun or(vararg filters: Filter, foreignTable: String? = null): PostgrestFilterBuilder<T> {
        searchParams[foreignTable.withDot("or")] = "(${filters.joinToString(",") { it.filterName }})"
        return this
    }

    /**
     * Finds all rows whose value on the stated "column" exactly matches the
     * specified "value".
     *
     * @param column  The column to filter on.
     * @param value  The value to filter with.
     */
    fun eq(column: String, value: Any) = apply {
        searchParams[column] = "eq.${value}"
    }

    /**
     * Finds all rows whose value on the stated [column] doesn't match the
     * specified [value].
     *
     * @param column  The column to filter on.
     * @param value  The value to filter with.
     */
    fun neq(column: String, value: Any) = apply {
        searchParams[column] = "neq.${value}"
    }

    /**
     * Finds all rows whose value on the stated [column] is greater than the
     * specified [value].
     *
     * @param column  The column to filter on.
     * @param value  The value to filter with.
     */
    fun gt(column: String, value: Any) = apply {
        searchParams[column] = "gt.$value"
    }

    /**
     * Finds all rows whose value on the stated [column] is greater than or
     * equal to the specified [value].
     *
     * @param column  The column to filter on.
     * @param value  The value to filter with.
     */
    fun gte(column: String, value: Any) = apply {
        searchParams[column] = "gt.$value"
    }

    /**
     * Finds all rows whose value on the stated [column] is less than the
     * specified [value].
     *
     * @param column  The column to filter on.
     * @param value  The value to filter with.
     */
    fun lt(column: String, value: Any) = apply {
        searchParams[column] = "lt.$value"
    }

    /**
     * Finds all rows whose value on the stated [column] is less than or
     * equal to the specified [value].
     *
     * @param column  The column to filter on.
     * @param value  The value to filter with.
     */
    fun lte(column: String, value: Any) = apply {
        searchParams[column] = "lte.$value"
    }

    /**
     * Finds all rows whose value in the stated [column] matches the supplied
     * [pattern] (case sensitive).
     *
     * @param column  The column to filter on.
     * @param pattern  The pattern to filter with.
     */
    fun like(column: String, pattern: String) = apply {
        searchParams[column] = "like.$pattern"
    }

    /**
     * Finds all rows whose value in the stated [column] matches the supplied
     * [pattern] (case insensitive).
     *
     * @param column  The column to filter on.
     * @param pattern  The pattern to filter with.
     */
    fun ilike(column: String, pattern: String) = apply {
        searchParams[column] = "ilike.$pattern"
    }

    /**
     * A check for exact equality (null, true, false), finds all rows whose
     * value on the stated [column] exactly match the specified [value].
     *
     * @param column  The column to filter on.
     * @param value  The value to filter with.
     */
    fun isExactly(column: String, value: Boolean) = apply {
        searchParams[column] = "is.$value"
    }

    /**
     * Finds all rows whose value on the stated [column] is found on the
     * specified [values].
     *
     * @param column  The column to filter on.
     * @param values  The values to filter with.
     */
    fun isIn(column: String, vararg values: String) = apply {
        searchParams[column] = "in.(${values.joinToString(",") { it.wrapIfReserved() }})"
    }

    /**
     * Finds all rows whose json, array, or range value on the stated [column]
     * contains the values specified in [value].
     *
     * @param column  The column to filter on.
     * @param value  The value to filter with.
     */
    fun contains(column: String, value: Any) = apply {
        if (value is String)
            searchParams[column] = "cs.$value"
        else if (value.javaClass.isArray)
            searchParams[column] = "cs.{${(value as Array<*>).joinToString(",")}}"
        else
            searchParams[column] = "cs.${moshi.adapter(value.javaClass).toJson(value)}"
    }

    /**
     * Finds all rows whose json, array, or range value on the stated [column] is
     * contained by the specified [value].
     *
     * @param column  The column to filter on.
     * @param value  The value to filter with.
     */
    fun containedBy(column: String, value: Any) = apply {
        if (value is String)
            searchParams[column] = "cd.$value"
        else if (value.javaClass.isArray)
            searchParams[column] = "cd.{${(value as Array<*>).joinToString(",")}}"
        else
            searchParams[column] = "cd.{${moshi.adapter(value.javaClass).toJson(value)}}"
    }

    /**
     * Finds all rows whose range value on the stated [column] is strictly to the
     * left of the specified [range].
     *
     * @param column  The column to filter on.
     * @param range  The range to filter with.
     */
    fun rangeLt(column: String, range: String) = apply {
        searchParams[column] = "sl.${range}"
    }

    /**
     * Finds all rows whose range value on the stated [column] is strictly to the
     * right of the specified [range].
     *
     * @param column  The column to filter on.
     * @param range  The range to filter with.
     */
    fun rangeGt(column: String, range: String) = apply {
        searchParams[column] = "ss.${range}"
    }

    /**
     * Finds all rows whose range value on the stated [column] does not extend
     * to the left of the specified [range].
     *
     * @param column  The column to filter on.
     * @param range  The range to filter with.
     */
    fun rangeGte(column: String, range: String) = apply {
        searchParams[column] = "nxl.${range}"
    }

    /**
     * Finds all rows whose range value on the stated [column] does not extend
     * to the right of the specified [range].
     *
     * @param column  The column to filter on.
     * @param range  The range to filter with.
     */
    fun rangeLte(column: String, range: String) = apply {
        searchParams[column] = "nxr.${range}"
    }

    /**
     * Finds all rows whose range value on the stated [column] is adjacent to
     * the specified [range].
     *
     * @param column  The column to filter on.
     * @param range  The range to filter with.
     */
    fun rangeAdjacent(column: String, range: String) = apply {
        searchParams[column] = "adj.${range}"
    }

    /**
     * Finds all rows whose array or range value on the stated [column] overlaps
     * (has a value in common) with the specified [value].
     *
     * @param column  The column to filter on.
     * @param value  The value to filter with.
     */
    fun overlaps(column: String, vararg value: String) = apply {
        if (value.size == 1)
            searchParams[column] = "ov.${value[0]}"
        else
            searchParams[column] = "adj.{${value.joinToString(",")}}"
    }

    /**
     * Finds all rows whose text or tsvector value on the stated [column] matches
     * the tsquery in [query].
     *
     * @param column  The column to filter on.
     * @param query  The Postgres tsquery string to filter with.
     * @param type  The type of query conversion to use on [query].
     * @param config  The text search configuration to use.
     */
    fun textSearch(
        column: String,
        query: String,
        type: SearchType? = null,
        config: String? = null
    ) = apply {
        val configPart = if (config == null) "" else "($config)"
        val typePart = type?.searchName.orEmpty()
        searchParams[column] = "${typePart}fts${configPart}.${query}"
    }

    /**
     * Finds all rows whose [column] satisfies the filter.
     *
     * @param column  The column to filter on.
     * @param filter  The operator to filter with.
     * @param value  The value to filter with.
     */
    fun filter(column: String, filter: Filter, value: Any) = apply {
        searchParams[column] = "${filter.filterName}.$value"
    }

    /**
     * Finds all rows whose columns match the specified [query] object.
     *
     * @param query  The object to filter with, with column names as keys mapped
     *               to their filter values.
     */
    fun match(query: Map<String, Any>) = apply {
        for ((name, value) in query.entries) {
            searchParams[name] = "eq.$value"
        }
    }
}