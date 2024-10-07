package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.simon.ImgAdapter
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var gridView: GridView
    private lateinit var imgAdapter: ImgAdapter
    private lateinit var startButton: Button
    private lateinit var viewScore: TextView
    private lateinit var nombreEditText: EditText
    private lateinit var rankingButton: Button

    private val handler = Handler(Looper.getMainLooper())
    private val gameSequence = mutableListOf<Int>()
    private var playerSequence = mutableListOf<Int>()
    private var playerTurn = false
    private var gameStarted = false
    private var score = 0
    private var playerName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gridView = findViewById(R.id.gridView)
        startButton = findViewById(R.id.startButton)
        viewScore = findViewById(R.id.score)
        nombreEditText = findViewById(R.id.nombre)
        rankingButton = findViewById(R.id.rankingButton)

        imgAdapter = ImgAdapter(this) { position ->
            if (playerTurn && gameStarted) {
                handlePlayerClick(position)
            }
        }
        rankingButton.setOnClickListener {
            val intent = Intent(this, Ranking::class.java)
            startActivity(intent)
        }

        gridView.adapter = imgAdapter

        startButton.setOnClickListener {
            playerName = nombreEditText.text.toString() // Obtener nombre de jugador
            if (playerName.isNotEmpty()) {
                gameStarted = true
                startButton.isEnabled = false
                nombreEditText.visibility = EditText.GONE // Ocultar EditText
                startButton.visibility = Button.GONE // Ocultar botón de inicio
                rankingButton.visibility = Button.GONE // Ocultar botón de ranking

                Toast.makeText(this, "¡Bienvenido, $playerName! El juego ha comenzado.", Toast.LENGTH_SHORT).show()

                startGame()
            } else {
                Toast.makeText(this, "Por favor, ingresa tu nombre.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handlePlayerClick(position: Int) {
        playerSequence.add(position)

        // Comprobar si el clic del jugador es correcto o incorrecto
        if (playerSequence.size <= gameSequence.size) {
            if (playerSequence[playerSequence.lastIndex] != gameSequence[playerSequence.lastIndex]) {
                // Si el jugador falla, mostrar un mensaje y reiniciar el juego
                Toast.makeText(this, "¡Fallaste! Intenta de nuevo.", Toast.LENGTH_SHORT).show()
                resetGame() // Reiniciar el juego
            } else if (playerSequence.size == gameSequence.size) {
                // El jugador ha completado la secuencia correctamente
                playerTurn = false
                score++ // Incrementar el puntaje aquí
                viewScore.text = "Score: $score" // Actualizar el puntaje en la UI
                handler.postDelayed({ startNextRound() }, 1000)
            }
        }
    }

    private fun resetGame() {
        val sharedPreferences = getSharedPreferences("game_data", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Guarda el puntaje actual en SharedPreferences
        val currentRankings = sharedPreferences.getString("rankings", "") ?: ""
        editor.putString("rankings", "$currentRankings$playerName - $score,")
        editor.putString("last_score", score.toString())
        editor.apply()

        // Reiniciar el juego
        gameSequence.clear()
        playerSequence.clear()
        gameStarted = false
        startButton.isEnabled = true
        startButton.visibility = Button.VISIBLE
        nombreEditText.visibility = EditText.VISIBLE
        rankingButton.visibility = Button.VISIBLE
        score = 0
        viewScore.text = "Puntaje: $score"
    }


    private fun startGame() {
        handler.postDelayed({ startNextRound() }, 2000)
    }

    private fun startNextRound() {
        playerSequence.clear()
        val newColor = Random.nextInt(0, 4) // Generar un nuevo color aleatorio (0-3)
        gameSequence.add(newColor)
        showSequence()
    }

    private fun showSequence() {
        playerTurn = false
        var delay = 0L
        gridView.isEnabled = false // Deshabilitar click durante la secuencia

        gameSequence.forEachIndexed { index, position ->
            handler.postDelayed({
                // Esta parte del código utiliza una estructura `when` para determinar qué imagen brillante mostrar
                // en función del índice del color que se está procesando en la secuencia del juego.

                when (position) {
                    // Si el índice es 0, actualiza la imagen en la posición actual del adaptador
                    // a la versión brillante de "red" (luz roja).
                    0 -> imgAdapter.updateImg(position, R.drawable.lightred)

                    // Si el índice es 1, actualiza la imagen en la posición actual del adaptador
                    // a la versión brillante de "blue" (luz azul).
                    1 -> imgAdapter.updateImg(position, R.drawable.lightblue)

                    // Si el índice es 2, actualiza la imagen en la posición actual del adaptador
                    // a la versión brillante de "green" (luz verde).
                    2 -> imgAdapter.updateImg(position, R.drawable.lightgreen)

                    // Si el índice es 3, actualiza la imagen en la posición actual del adaptador
                    // a la versión brillante de "yellow" (luz amarilla).
                    3 -> imgAdapter.updateImg(position, R.drawable.lightyellow)
                }
                handler.postDelayed({
                    imgAdapter.resetImg(position) // Restablecer a la versión normal
                    if (index == gameSequence.size - 1) {
                        playerTurn = true // Habilitar turno del jugador
                        gridView.isEnabled = true // Habilitar clicks
                        Toast.makeText(this, "¡Tu turno!", Toast.LENGTH_SHORT).show()
                    }
                }, 500)
            }, delay)
            delay += 1000 // Delay entre colores
        }
    }
}
