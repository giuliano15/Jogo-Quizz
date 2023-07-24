package com.example.jogoquizz

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.jogoquizz.databinding.ActivityMainBinding
import com.example.jogoquizz.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Game : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private var selectedButton: Button? = null
    private var currentQuestion: Question? = null
    private var score: Int = 0
    private var questionCount: Int = 0
    private val MAX_QUESTION_COUNT = 10
    private var selectedOptionIndex: Int = -1
    private var playerName = ""
    private var correctAnswers: Int = 0

    private val resultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playerName = intent.getStringExtra("name") ?: ""

        // Se o nome do jogador ainda estiver vazio, tenta recuperá-lo das SharedPreferences
        if (playerName.isBlank()) {
            playerName = getSavedUserName()
        } else {
            // Caso o nome esteja presente na Intent, salva-o novamente nas SharedPreferences
            saveUserName(playerName)
        }

        binding.btnOptionOne.setOnClickListener(this)
        binding.btnOptionTwo.setOnClickListener(this)
        binding.btnOptionThree.setOnClickListener(this)
        binding.btnOptionFour.setOnClickListener(this)
        binding.btnOptionFive.setOnClickListener(this)

        // Carrega a primeira pergunta na TextView e nos botões
        loadRandomQuestion()

        binding.btnSubmit.setOnClickListener {
            chooseAnswer()
        }
    }

    override fun onClick(view: View?) {
        val clickedButton = view as Button

        // Lógica para o clique do botão
        val optionText = when (view.id) {
            R.id.btnOptionOne -> "Opção 1"
            R.id.btnOptionTwo -> "Opção 2"
            R.id.btnOptionThree -> "Opção 3"
            R.id.btnOptionFour -> "Opção 4"
            R.id.btnOptionFive -> "Opção 5"
            else -> "Opção desconhecida"
        }

        // Verifica se o botão clicado é o mesmo que já está selecionado
        if (clickedButton == selectedButton) {
            // Desmarca o botão
            clickedButton.setBackgroundColor(Color.TRANSPARENT)
            selectedButton = null
        } else {
            // Desmarca o botão atualmente selecionado, se houver
            selectedButton?.setBackgroundColor(Color.WHITE)

            // Marca o botão clicado como selecionado
            clickedButton.setBackgroundColor(Color.parseColor("#FFC107"))
            selectedButton = clickedButton
        }
    }

    private fun getSavedUserName(): String {
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        return sharedPreferences.getString("playerName", "") ?: ""
    }

    private fun saveUserName(name: String?) {
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("playerName", name)
        editor.apply()
    }

    private fun chooseAnswer() {
        // Verifica se uma opção foi selecionada antes de enviar a resposta
        if (selectedButton != null && currentQuestion != null) {
            // Verifica a opção selecionada pelo usuário
            selectedOptionIndex = when (selectedButton?.id) {
                R.id.btnOptionOne -> 0
                R.id.btnOptionTwo -> 1
                R.id.btnOptionThree -> 2
                R.id.btnOptionFour -> 3
                R.id.btnOptionFive -> 4
                else -> -1
            }

            score++

            // Verifica a resposta com o servidor
            if (currentQuestion!!.id != null) {
                verifyAnswerWithServer(
                    currentQuestion!!.id,
                    currentQuestion!!.options[selectedOptionIndex]
                )
            }

            //questionCount++
            if (questionCount == MAX_QUESTION_COUNT) {
                showResult()
                return
            }

            // Carrega uma nova pergunta
            loadRandomQuestion()
        } else {
            Toast.makeText(
                this,
                "Escolha uma opção antes de enviar sua resposta.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadRandomQuestion() {
        // Resetar a opção selecionada
        selectedButton?.setBackgroundColor(Color.TRANSPARENT)
        selectedButton = null
        val call: Call<Question> = RetrofitClient.apiService.getQuestion()
        call.enqueue(object : Callback<Question> {
            override fun onResponse(call: Call<Question>, response: Response<Question>) {
                if (response.isSuccessful) {
                    currentQuestion = response.body()
                    if (currentQuestion != null) {
                        // Preencha a pergunta na TextView
                        binding.tvQuestion.text = currentQuestion!!.statement
                        // Preencha as opções nos botões
                        setOptionsOnButtons(currentQuestion!!)
                        // Incrementa o contador de perguntas
                        questionCount++

                        binding.pb.progress = questionCount

                        binding.tvProgress.text = "$questionCount/$MAX_QUESTION_COUNT"

                        // Verifica se o quiz chegou ao final (10 perguntas)
                        if (questionCount > 10) {
                            showScore()
                        }
                    }
                } else {
                    // Tratar caso de erro na resposta
                    Toast.makeText(
                        this@Game,
                        "Erro na resposta da API: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Question>, t: Throwable) {
                // Tratar falha na requisição
                Toast.makeText(
                    this@Game,
                    "Falha na requisição: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun verifyAnswerWithServer(questionId: Int, selectedOption: String) {
        // Recebe a opção selecionada
        val answer = AnswerRequest(selectedOption)

        // Recebe a resposta do servidor atraves do metodo post
        RetrofitClient.apiService.submitAnswer(questionId.toString(), answer)
            .enqueue(object : Callback<AnswerResponse> {
                override fun onResponse(
                    call: Call<AnswerResponse>,
                    response: Response<AnswerResponse>
                ) {
                    if (response.isSuccessful) {

                        val answerResponse = response.body()
                        answerResponse?.let {
                            if (it.result) {
                                correctAnswers++
                                score++
                                showAnswerResult("Resposta correta!")

                            } else {
                                showAnswerResult("Resposta incorreta.")

                            }
                        }
                    }
                }

                override fun onFailure(call: Call<AnswerResponse>, t: Throwable) {
                    // Handle failure in the request
                    Toast.makeText(
                        this@Game,
                        "Falha na requisição: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun setOptionsOnButtons(question: Question) {
        val options = question.options
        if (options.size >= 5) {
            binding.btnOptionOne.text = options[0]
            binding.btnOptionTwo.text = options[1]
            binding.btnOptionThree.text = options[2]
            binding.btnOptionFour.text = options[3]
            binding.btnOptionFive.text = options[4]
        }
    }

    private fun showAnswerResult(message: String) {
        binding.tvResult.text = message
        if (message == "Resposta correta!") {
            binding.tvResult.setTextColor(Color.GREEN)
        } else {
            binding.tvResult.setTextColor(Color.RED)
        }
    }

    private fun showResult() {
        // Recupera o nome do usuário das SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val playerName = sharedPreferences.getString("playerName", "")

        // Inicia a tela de resultados (ResultActivity)
        val resultIntent = Intent(this, ResultActivity::class.java)
        resultIntent.putExtra("playerName", playerName)
        resultIntent.putExtra("correctAnswers", correctAnswers)

        resultLauncher.launch(resultIntent)
    }

    private fun showScore() {
        // Exibe a pontuação do usuário
        Toast.makeText(this, "Pontuação: $score", Toast.LENGTH_LONG).show()
        // Reseta a pontuação e o contador de perguntas
        score = 0
        questionCount = 0
    }

    override fun onBackPressed() {

    }
}
