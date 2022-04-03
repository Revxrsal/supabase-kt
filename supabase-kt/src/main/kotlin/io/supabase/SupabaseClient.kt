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
package io.supabase

import com.squareup.moshi.Moshi
import io.supabase.common.MemoryStorage
import io.supabase.common.SupportedStorage
import io.supabase.gotrue.DEFAULT_MOSHI
import io.supabase.gotrue.GoTrueClient
import io.supabase.postgrest.PostgrestClient
import okhttp3.OkHttpClient

class SupabaseClient(
    url: String,
    authorization: String,
    schema: String? = null,
    authSettings: SupabaseAuthSettings = SupabaseAuthSettings(),
    storage: SupportedStorage = MemoryStorage(),
    moshi: Moshi = DEFAULT_MOSHI,
    client: OkHttpClient = OkHttpClient(),
) {

    val auth = GoTrueClient(
        url = "$url/auth/v1",
        authorization = authorization,
        serviceRole = authSettings.serviceRole,
        persistSessions = authSettings.persistSessions,
        headers = authSettings.headers,
        autoRefreshToken = authSettings.autoRefreshTokens,
        client = client,
        moshi = moshi,
        storage = storage
    )

    val data = PostgrestClient(
        url = "$url/rest/v1",
        authorization = authorization,
        schema = schema,
        moshi = moshi,
        client = client
    )

    init {
        auth.recoverSession()
    }

}