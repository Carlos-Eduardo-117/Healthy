package com.example.healthyapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    lateinit var preferences: SharedPreferences
    private val PREFS_NAME = "medPrefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        btnLogin.setOnClickListener {
            val emailInput = etEmail.text.toString().trim()
            val passwordInput = etPassword.text.toString().trim()

            val savedEmail = preferences.getString("email", null)
            val savedPassword = preferences.getString("password", null)

            if (emailInput == savedEmail && passwordInput == savedPassword) {
                Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                // Aquí vamos a la pantalla principal (HomeActivity, la haremos pronto)
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }
}
