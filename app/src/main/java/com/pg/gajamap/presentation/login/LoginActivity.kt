package com.pg.gajamap.presentation.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.pg.gajamap.R
import com.pg.gajamap.data.local.GJMSharedPreference
import com.pg.gajamap.data.service.KakaoAuthService
import com.pg.gajamap.databinding.ActivityLoginBinding
import com.pg.gajamap.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    @Inject
    lateinit var storage: GJMSharedPreference

    @Inject
    lateinit var kakaoAuthService: KakaoAuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.kakaoLoginBtn.setOnClickListener {
            kakaoLogin()
        }

        binding.locationInfo.setOnClickListener {
            val navController = findNavController(R.id.nav_host_fragment)
            navController.navigateUp()
            navController.navigate(R.id.locationInfoFragment)
        }

        lifecycleScope.launch {
            storage.isLogin.collect { isLogin ->
                if (isLogin) {
                    delay(100)
                    startMain()
                }
            }
        }
    }

    private fun startMain() {
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }

    private fun kakaoLogin() {
        kakaoAuthService.kakaoLogin(
            ::startMain
        )
    }
}