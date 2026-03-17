package com.gymnasioforce.runnerapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.databinding.ActivityLoginBinding
import com.gymnasioforce.runnerapp.network.LoginRequest
import com.gymnasioforce.runnerapp.network.RetrofitClient
import com.gymnasioforce.runnerapp.ui.main.MainActivity
import com.gymnasioforce.runnerapp.utils.Prefs
import com.gymnasioforce.runnerapp.utils.showToast
import kotlinx.coroutines.launch

class LoginActivity : com.gymnasioforce.runnerapp.ui.BaseActivity() {

    private lateinit var b: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        if (Prefs(this).token.isNotEmpty()) {
            RetrofitClient.setToken(Prefs(this).token)
            goToMain()
            return
        }

        b.btnLogin.setOnClickListener { doLogin() }
        b.btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun clearErrors() {
        b.tilEmail.error = null
        b.tilPassword.error = null
    }

    private fun doLogin() {
        clearErrors()
        val email = b.etEmail.text.toString().trim()
        val password = b.etPassword.text.toString()

        var hasError = false
        if (email.isEmpty()) { b.tilEmail.error = getString(R.string.validation_enter_email); hasError = true }
        if (password.isEmpty()) { b.tilPassword.error = getString(R.string.validation_enter_password); hasError = true }
        if (hasError) return

        setLoading(true)
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.login(LoginRequest(email, password))
                if (resp.isSuccessful && resp.body()?.success == true) {
                    val auth = resp.body()?.data ?: run {
                        showToast("Error procesando respuesta")
                        return@launch
                    }
                    Prefs(this@LoginActivity).apply {
                        token = auth.token
                        userId = auth.user.id
                        userName = auth.user.name
                    }
                    RetrofitClient.setToken(auth.token)
                    goToMain()
                } else {
                    showToast(resp.body()?.message ?: getString(R.string.error_wrong_credentials))
                }
            } catch (e: Exception) {
                showToast(getString(R.string.error_connection))
            } finally { setLoading(false) }
        }
    }

    private fun setLoading(show: Boolean) {
        b.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        b.btnLogin.isEnabled = !show
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
