package com.pg.gajamap.data.manager

import com.pg.gajamap.data.local.GJMSharedPreference
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CookieManager @Inject constructor(
    private val storage: GJMSharedPreference
) : CookieJar {

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val sessionId: String = runBlocking {
            storage.session.first()
        }

        Timber.d(sessionId)

        return sessionId.let {
            val sessionCookie = Cookie.Builder()
                .domain(url.host)
                .path("/")
                .name("SESSION")
                .value(it)
                .build()
            listOf(sessionCookie)
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val cookie = extractSessionValue(cookies)
        Timber.d(cookie.toString())
    }

    private fun extractSessionValue(cookieString: List<Cookie>): String? {
        val regex = Regex("SESSION=([A-Za-z0-9]+);")
        val matchResult = regex.find(cookieString.toString())
        return matchResult?.value
    }
}