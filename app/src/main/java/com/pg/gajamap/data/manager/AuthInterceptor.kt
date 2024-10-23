package com.pg.gajamap.data.manager

import android.app.Application
import android.content.Intent
import android.widget.Toast
import com.pg.gajamap.data.local.GJMSharedPreference
import com.pg.gajamap.presentation.login.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val storage: GJMSharedPreference,
    private val context: Application
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        when (response.code) {
            401 -> {
                CoroutineScope(Dispatchers.Main).launch {
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)

                    Toast.makeText(context, "세션 만료. 다시 로그인 해주세요.", Toast.LENGTH_SHORT).show()
                    storage.clear()
                }
            }
        }
        return response
    }
}