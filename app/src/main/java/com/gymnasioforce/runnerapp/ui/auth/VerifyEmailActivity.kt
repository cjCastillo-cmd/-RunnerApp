package com.gymnasioforce.runnerapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import com.gymnasioforce.runnerapp.ui.BaseActivity
import androidx.lifecycle.lifecycleScope
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.databinding.ActivityVerifyEmailBinding
import com.gymnasioforce.runnerapp.network.RetrofitClient
import com.gymnasioforce.runnerapp.network.VerifyEmailRequest
import com.gymnasioforce.runnerapp.ui.main.MainActivity
import com.gymnasioforce.runnerapp.utils.Prefs
import com.gymnasioforce.runnerapp.utils.showToast
import kotlinx.coroutines.launch

class VerifyEmailActivity : BaseActivity() {

    private lateinit var b: ActivityVerifyEmailBinding
    private lateinit var email: String
    private lateinit var codeFields: List<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityVerifyEmailBinding.inflate(layoutInflater)
        setContentView(b.root)

        email = intent.getStringExtra("email") ?: ""
        b.tvEmail.text = email

        codeFields = listOf(b.etCode1, b.etCode2, b.etCode3, b.etCode4, b.etCode5, b.etCode6)
        setupCodeFields()

        b.btnVerify.setOnClickListener { doVerify() }
        b.btnResend.setOnClickListener { doResend() }
    }

    private fun setupCodeFields() {
        for (i in codeFields.indices) {
            codeFields[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && i < codeFields.size - 1) {
                        codeFields[i + 1].requestFocus()
                    }
                }
            })
        }
    }

    private fun getCode(): String = codeFields.joinToString("") { it.text.toString() }

    private fun doVerify() {
        val code = getCode()
        if (code.length != 6) { showToast(getString(R.string.validation_enter_code)); return }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.verifyEmail(VerifyEmailRequest(email, code))
                if (resp.isSuccessful && resp.body()?.success == true) {
                    val auth = resp.body()?.data ?: run {
                        showToast("Error procesando respuesta")
                        return@launch
                    }
                    Prefs(this@VerifyEmailActivity).apply {
                        token = auth.token
                        userId = auth.user.id
                        userName = auth.user.name
                    }
                    RetrofitClient.setToken(auth.token)
                    showToast(getString(R.string.success_verified))
                    startActivity(Intent(this@VerifyEmailActivity, MainActivity::class.java))
                    finishAffinity()
                } else {
                    showToast(resp.body()?.message ?: getString(R.string.error_wrong_code))
                }
            } catch (e: Exception) {
                showToast(getString(R.string.error_connection))
            } finally { setLoading(false) }
        }
    }

    private fun doResend() {
        lifecycleScope.launch {
            try {
                RetrofitClient.api.resendCode(mapOf("email" to email))
                showToast(getString(R.string.msg_code_resent, email))
            } catch (e: Exception) { showToast(getString(R.string.error_connection)) }
        }
    }

    private fun setLoading(show: Boolean) {
        b.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        b.btnVerify.isEnabled = !show
    }
}
