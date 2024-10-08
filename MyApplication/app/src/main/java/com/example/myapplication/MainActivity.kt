package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.simon.ImgAdapter
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    // Definición de variables necesarias para la UI y la lógica del juego
    private lateinit var gridView: GridView
    private lateinit var imgAdapter: ImgAdapter
    private lateinit var startButton: Button
    private lateinit var viewScore: TextView
    private lateinit var nombreEditText: EditText
    private lateinit var rankingButton: Button

    // Handler para manejar la ejecución de tareas en el hilo principal
    private val handler = Handler(Looper.getMainLooper())
    private val gameSequence = mutableListOf<Int>() // Secuencia generada por el juego
    private var playerSequence = mutableListOf<Int>() // Secuencia ingresada por el jugador
    private var playerTurn = false // Indica si es el turno del jugador
    private var gameStarted = false // Indica si el juego ha comenzado
    private var score = 0 // Puntaje del jugador
    private var playerName: String = "" // Nombre del jugador

    // Método que se llama al crear la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Establecer el layout de la actividad

        // Inicializar las vistas
        gridView = findViewById(R.id.gridView)
        startButton = findViewById(R.id.startButton)
        viewScore = findViewById(R.id.score)
        nombreEditText = findViewById(R.id.nombre)
        rankingButton = findViewById(R.id.rankingButton)

        // Inicializar el adaptador de imágenes con un click listener
        imgAdapter = ImgAdapter(this) { position ->
            if (playerTurn && gameStarted) {
                handlePlayerClick(position) // Manejar clic del jugador
            }
        }

        // Configurar el botón de ranking para iniciar la actividad de ranking
        rankingButton.setOnClickListener {
            val intent = Intent(this, Ranking::class.java)
            startActivity(intent)
        }

        gridView.adapter = imgAdapter // Asignar el adaptador al GridView

        // Configurar el botón de inicio del juego
        startButton.setOnClickListener {
            playerName = nombreEditText.text.toString() // Obtener nombre de jugador
            if (playerName.isNotEmpty()) {
                gameStarted = true
                startButton.isEnabled = false // Deshabilitar el botón de inicio
                nombreEditText.visibility = EditText.GONE // Ocultar EditText
                startButton.visibility = Button.GONE // Ocultar botón de inicio
                rankingButton.visibility = Button.GONE // Ocultar botón de ranking

                Toast.makeText(
                    this,
                    "¡Bienvenido, $playerName! El juego ha comenzado.",
                    Toast.LENGTH_SHORT
                ).show()
                startGame() // Iniciar el juego
            } else {
                Toast.makeText(this, "Por favor, ingresa tu nombre.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Método para manejar clics del jugador en el GridView
    private fun handlePlayerClick(position: Int) {
        playerSequence.add(position) // Agregar la posición clickeada a la secuencia del jugador

        // Comprobar si la secuencia ingresada es correcta
        if (playerSequence.size <= gameSequence.size) {
            if (playerSequence[playerSequence.lastIndex] != gameSequence[playerSequence.lastIndex]) {
                // Si el jugador falla, mostrar un mensaje y reiniciar el juego
                Toast.makeText(this, "¡Fallaste! Intenta de nuevo.", Toast.LENGTH_SHORT).show()
                resetGame() // Reiniciar el juego
            } else if (playerSequence.size == gameSequence.size) {
                // El jugador ha completado la secuencia correctamente
                playerTurn = false // Terminar el turno del jugador
                score++ // Incrementar el puntaje
                viewScore.text = "Score: $score" // Actualizar el puntaje en la UI
                handler.postDelayed(
                    { startNextRound() },
                    1000
                ) // Iniciar la siguiente ronda después de un delay
            }
        }
    }

    // Método para reiniciar el juego
    private fun resetGame() {
        // Guardar el puntaje antes de reiniciar
        guardarPuntuacioPartida(score)

        // Reiniciar el juego
        gameSequence.clear() // Limpiar la secuencia del juego
        playerSequence.clear() // Limpiar la secuencia del jugador
        gameStarted = false // Marcar que el juego no ha comenzado
        startButton.isEnabled = true // Habilitar el botón de inicio
        startButton.visibility = Button.VISIBLE // Mostrar el botón de inicio
        nombreEditText.visibility = EditText.VISIBLE // Mostrar EditText
        rankingButton.visibility = Button.VISIBLE // Mostrar botón de ranking
        score = 0 // Reiniciar puntaje
        viewScore.text = "Puntaje: $score" // Actualizar el puntaje en la UI
    }

    // Método para guardar el puntaje de la partida
    private fun guardarPuntuacioPartida(puntuacion: Int) {
        val file = File(
            getExternalFilesDir(null),
            "PuntuacioPartida.json"
        ) // Crear o acceder al archivo JSON
        val existingScores =
            mutableListOf<JSONObject>() // Lista para guardar las puntuaciones existentes
        var scoreUpdated = false // Variable para verificar si se actualizó el puntaje

        // Leer el archivo existente
        if (file.exists()) {
            file.forEachLine { line ->
                try {
                    val json = JSONObject(line) // Parsear la línea como JSON
                    if (json.getString("nombre") == playerName) { // Verificar si el nombre coincide
                        if (puntuacion > json.getInt("puntuacion")) { // Comparar puntajes
                            json.put("puntuacion", puntuacion) // Actualizar el puntaje
                            scoreUpdated = true // Marcar que se actualizó
                        }
                    }
                    existingScores.add(json) // Agregar JSON a la lista
                } catch (e: Exception) {
                    Log.e(
                        "Error",
                        "Error al leer línea del archivo: ${e.message}"
                    ) // Manejo de errores
                }
            }
        }

        // Si no se actualizó el puntaje, agregar un nuevo JSON
        if (!scoreUpdated) {
            val json = JSONObject().apply {
                put("puntuacion", puntuacion) // Guardar nuevo puntaje
                put("nombre", playerName) // Guardar nombre del jugador
            }
            existingScores.add(json) // Agregar a la lista de puntuaciones
        }

        // Guardar el archivo
        try {
            FileOutputStream(file).use { output ->
                existingScores.forEach { json ->
                    output.write((json.toString() + System.lineSeparator()).toByteArray()) // Escribir cada JSON en el archivo
                }
            }
        } catch (e: IOException) {
            Log.e("Error", "ERROR AL GUARDAR EL FICHERO: ${e.message}") // Manejo de errores
        }
    }

    // Método para iniciar el juego
    private fun startGame() {
        handler.postDelayed(
            { startNextRound() },
            2000
        ) // Esperar 2 segundos y luego iniciar la siguiente ronda
    }

    // Método para iniciar la siguiente ronda
    private fun startNextRound() {
        playerSequence.clear() // Limpiar la secuencia del jugador
        val newColor = Random.nextInt(0, 4) // Generar un nuevo color aleatorio (0-3)
        gameSequence.add(newColor) // Agregar el nuevo color a la secuencia del juego
        showSequence() // Mostrar la secuencia al jugador
    }

    // Método para mostrar la secuencia al jugador
    private fun showSequence() {
        playerTurn = false // Deshabilitar el turno del jugador
        var delay = 0L // Inicializar el delay
        gridView.isEnabled = false // Deshabilitar clic durante la secuencia

        // Iterar sobre la secuencia del juego
        gameSequence.forEachIndexed { index, position ->
            handler.postDelayed({
                // Actualizar la imagen en función de la posición
                when (position) {
                    0 -> imgAdapter.updateImg(position, R.drawable.lightred) // Luz roja por el número 0
                    1 -> imgAdapter.updateImg(position, R.drawable.lightblue) // Luz azul por el número 1
                    2 -> imgAdapter.updateImg(position, R.drawable.lightgreen) // Luz verde por el número 2
                    3 -> imgAdapter.updateImg(position, R.drawable.lightyellow) // Luz amarilla por el número 3
                }
                handler.postDelayed({
                    imgAdapter.resetImg(position) // Restablecer a la imagen normal
                    if (index == gameSequence.size - 1) {
                        playerTurn = true // Habilitar turno del jugador
                        gridView.isEnabled = true // Habilitar clics
                        Toast.makeText(this, "¡Tu turno!", Toast.LENGTH_SHORT)
                            .show() // Avisar al jugador
                    }
                }, 500) // Tiempo de espera para mostrar la luz
            }, delay)
            delay += 1000
        }
    }
}