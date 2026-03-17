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
        if (name.isEmpty()) { b.tilNombre.error = "Ingresa tu nombre"; hasError = true }
        if (email.isEmpty()) { b.tilEmail.error = "Ingresa tu email"; hasError = true }
        if (country.isEmpty()) { b.tilPais.error = "Selecciona tu pais"; hasError = true }
        if (pass.isEmpty()) { b.tilPassword.error = "Ingresa una contrasena"; hasError = true }
        else if (pass.length < 6) { b.tilPassword.error = "Minimo 6 caracteres"; hasError = true }
        if (confirm != pass) { b.tilConfirmPassword.error = "Las contrasenas no coinciden"; hasError = true }
        if (hasError) return

        setLoading(true)
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.register(
                    RegisterRequest(name, email, pass, confirm, country)
                )
                if (resp.isSuccessful && resp.body()?.success == true) {
                    showToast("Registro exitoso! Revisa tu correo.")
                    val intent = Intent(this@RegisterActivity, VerifyEmailActivity::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                    finish()
                } else {
                    showToast(resp.body()?.message ?: "Error al registrarse")
                }
            } catch (e: Exception) {
                showToast("Error de conexion: ${e.message}")
            } finally { setLoading(false) }
        }
    }

    private fun setLoading(show: Boolean) {
        b.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        b.btnRegister.isEnabled = !show
    }
}
