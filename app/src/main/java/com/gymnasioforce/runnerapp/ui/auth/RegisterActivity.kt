package com.gymnasioforce.runnerapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.gymnasioforce.runnerapp.R
import com.gymnasioforce.runnerapp.ui.BaseActivity
import androidx.lifecycle.lifecycleScope
import com.gymnasioforce.runnerapp.databinding.ActivityRegisterBinding
import com.gymnasioforce.runnerapp.network.RegisterRequest
import com.gymnasioforce.runnerapp.network.RetrofitClient
import com.gymnasioforce.runnerapp.ui.main.MainActivity
import com.gymnasioforce.runnerapp.utils.Prefs
import com.gymnasioforce.runnerapp.utils.showToast
import kotlinx.coroutines.launch

class RegisterActivity : BaseActivity() {

    private lateinit var b: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(b.root)

        setupCountryDropdown()
        b.btnRegister.setOnClickListener { doRegister() }
        b.btnGoLogin.setOnClickListener { finish() }
    }

    private fun setupCountryDropdown() {
        val countries = resources.getStringArray(R.array.countries)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, countries)
        b.actvPais.setAdapter(adapter)
    }

    private fun clearErrors() {
        b.tilNombre.error = null
        b.tilEmail.error = null
        b.tilPassword.error = null
        b.tilConfirmPassword.error = null
        b.tilPais.error = null
    }

    private fun doRegister() {
        clearErrors()
        val name = b.etNombre.text.toString().trim()
        val email = b.etEmail.text.toString().trim()
        val pass = b.etPassword.text.toString()
        val confirm = b.etConfirmPassword.text.toString()
        val country = b.actvPais.text.toString().trim()

        var hasError = false
        if (name.isEmpty()) { b.tilNombre.error = getString(R.string.validation_enter_name); hasError = true }
        if (email.isEmpty()) { b.tilEmail.error = getString(R.string.validation_enter_email); hasError = true }
        if (country.isEmpty()) { b.tilPais.error = getString(R.string.validation_select_country); hasError = true }
        if (pass.isEmpty()) { b.tilPassword.error = getString(R.string.validation_enter_password); hasError = true }
        else if (pass.length < 6) { b.tilPassword.error = getString(R.string.validation_min_password); hasError = true }
        if (confirm != pass) { b.tilConfirmPassword.error = getString(R.string.validation_passwords_mismatch); hasError = true }
        if (hasError) return

        setLoading(true)
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.register(
                    RegisterRequest(name, email, pass, confirm, country)
                )
                if (resp.isSuccessful && resp.body()?.success == true) {
                    val auth = resp.body()?.data ?: run {
                        showToast("Error procesando respuesta")
                        return@launch
                    }
                    Prefs(this@RegisterActivity).apply {
                        token = auth.token
                        userId = auth.user.id
                        userName = auth.user.name
                    }
                    RetrofitClient.setToken(auth.token)
                    showToast(getString(R.string.success_register))
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                    finishAffinity()
                } else {
                    showToast(resp.body()?.message ?: "Error al registrarse")
                }
            } catch (e: Exception) {
                showToast(getString(R.string.error_connection))
            } finally { setLoading(false) }
        }
    }

    private fun setLoading(show: Boolean) {
        b.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        b.btnRegister.isEnabled = !show
    }
}
