package com.gymnasioforce.runnerapp.ui.auth

import android.os.Bundle
import android.view.View
import com.gymnasioforce.runnerapp.ui.BaseActivity
import androidx.lifecycle.lifecycleScope
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.databinding.ActivityForgotPasswordBinding
import com.gymnasioforce.runnerapp.network.RetrofitClient
import com.gymnasioforce.runnerapp.utils.showToast
import kotlinx.coroutines.launch

class ForgotPasswordActivity : BaseActivity() {

    private lateinit var b: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnSend.setOnClickListener { doSend() }
        b.tvBack.setOnClickListener { finish() }
    }

    private fun doSend() {
        val email = b.etEmail.text.toString().trim()
        if (email.isEmpty()) { showToast(getString(R.string.validation_enter_email)); return }

        b.progressBar.visibility = View.VISIBLE
        b.btnSend.isEnabled = false

        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.forgotPassword(mapOf("email" to email))
                if (resp.isSuccessful && resp.body()?.success == true) {
                    showToast(getString(R.string.success_token_sent))
                } else {
                    showToast(resp.body()?.message ?: getString(R.string.error_email_not_found))
                }
            } catch (e: Exception) {
                showToast(getString(R.string.error_connection))
            } finally {
                b.progressBar.visibility = View.GONE
                b.btnSend.isEnabled = true
            }
        }
    }
}
