package com.example.jogoquizz

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class Welcome : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var buttonStartQuiz: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        editTextName = findViewById(R.id.et_name)
        buttonStartQuiz = findViewById(R.id.startBtn)

        buttonStartQuiz.setOnClickListener {

            if (isInternetAvailable()) {
                // Se tiver Internet, inicie o quiz
                val name = editTextName.text.toString().trim()
                if (name.isNotEmpty()) {
                    saveUserName(name)

                    val startQuizz = Intent(this, Game::class.java)
                    startQuizz.putExtra("name", name)
                    startActivity(startQuizz)

                } else {
                    Toast.makeText(
                        this,
                        "Por favor, digite seu nome ou apelido",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Se não tiver Internet, exiba uma mensagem de erro
                Toast.makeText(
                    this,
                    "Verifique sua conexão com a Internet e tente novamente.",
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    }

    private fun saveUserName(name: String) {
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("playerName", name)
        editor.apply()
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

        // Verifica se a rede está disponível e tem capacidade para conexão com a Internet
        return networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

}