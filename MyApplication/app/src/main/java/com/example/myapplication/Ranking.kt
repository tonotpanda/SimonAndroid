package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.File

class Ranking : AppCompatActivity() {

    private lateinit var rankingListView: ListView
    private val rankings = mutableListOf<Pair<String, Int>>() // Lista para guardar nombres y puntajes
    private lateinit var returnButton: Button // Botón para volver al juego principal.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        rankingListView = findViewById(R.id.rankingListView)
        returnButton = findViewById(R.id.returnButton)

        loadRankings()

        // Ordenar los puntajes de mayor a menor
        rankings.sortByDescending { it.second } // Ordenar por el puntaje

        // Crear una lista de strings para mostrar en el ListView
        val displayRankings = rankings.map { "${it.first} - ${it.second}" }

        // Crear un adaptador y asignarlo al ListView
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayRankings)
        rankingListView.adapter = adapter

        // Configurar el botón "VOLVER"
        returnButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java) // Cambia a tu actividad principal
            startActivity(intent)
            finish() // Opcional: cerrar la actividad actual para no volver a ella al presionar "atrás"
        }
    }

    private fun loadRankings() {
        // Crear un objeto File que apunta al archivo de puntuaciones en el almacenamiento externo
        val file = File(getExternalFilesDir(null), "PuntuacioPartida.json")

        // Limpiar la lista de rankings antes de cargar nuevos datos
        rankings.clear()

        // Comprobar si el archivo existe
        if (file.exists()) {
            // Leer el archivo línea por línea
            file.forEachLine { line ->
                try {
                    // Convertir la línea actual en un objeto JSON
                    val json = JSONObject(line)

                    // Obtener el nombre del jugador del objeto JSON
                    val name = json.getString("nombre")

                    // Obtener el puntaje del jugador del objeto JSON
                    val score = json.getInt("puntuacion")

                    // Agregar el nombre y el puntaje a la lista de rankings como un par (nombre, puntaje)
                    rankings.add(Pair(name, score))
                } catch (e: Exception) {
                    // Registrar un error si hay un problema al leer la línea
                    Log.e("Error", "Error al leer línea del archivo: ${e.message}")
                }
            }
        }
    }
}
