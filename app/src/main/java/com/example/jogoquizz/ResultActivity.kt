package com.example.jogoquizz


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.jogoquizz.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtém os dados enviados da MainActivity
        val playerName = intent.getStringExtra("playerName")
        val correctAnswers = intent.getIntExtra("correctAnswers", 0)

        // Exibe os dados na tela de resultados
        binding.displayResult.text =
            "Parabéns, $playerName!\nVocê acertou $correctAnswers de 10 perguntas."

        // Configura o clique do botão "Reiniciar"
        binding.btnRestart.setOnClickListener {
            // Inicia a MainActivity novamente
            val intent = Intent(this, Game::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            // Finaliza a ResultActivity para que ela não fique na pilha de atividades
            finish()
        }

        binding.btnFinish.setOnClickListener {
            finishAffinity()
        }

    }

    //Não permite que usuario tente voltar depois de estar na tela de resultados
    override fun onBackPressed() {

    }

}
