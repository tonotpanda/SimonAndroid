package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class Ranking : AppCompatActivity() {

    private lateinit var rankingListView: ListView
    private val rankings = mutableListOf<String>() // Lista para guardar los puntajes
    private lateinit var returnButton: Button // Botón para volver al juego principal.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        rankingListView = findViewById(R.id.rankingListView)
        returnButton = findViewById(R.id.returnButton)

        loadRankings()

        // Crear un adaptador y asignarlo al ListView
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, rankings)
        rankingListView.adapter = adapter

        // Configurar el botón "VOLVER"
        returnButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java) // Cambia a tu actividad principal
            startActivity(intent)
            finish() // Opcional: cerrar la actividad actual para no volver a ella al presionar "atrás"
        }
    }

    private fun loadRankings() {
        val sharedPreferences = getSharedPreferences("game_data", MODE_PRIVATE)
        val rankingsString = sharedPreferences.getString("rankings", "")
        rankings.clear()
        rankings.addAll(rankingsString?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList())
    }
}
