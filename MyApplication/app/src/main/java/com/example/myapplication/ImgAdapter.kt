package com.example.simon

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.example.myapplication.R

class ImgAdapter(
    private val context: Context,
    private val onItemClick: (Int) -> Unit // Callback para el clic
) : BaseAdapter() {

    private val images = intArrayOf(
        R.drawable.red,
        R.drawable.blue,
        R.drawable.green,
        R.drawable.yellow
    )

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val gridView: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.img, parent, false)
        val imageView = gridView.findViewById<ImageView>(R.id.img)

        imageView.setImageResource(images[position]) // Usar el índice directamente

        // Configurar el clic en el gridView
        gridView.setOnClickListener {
            onItemClick(position) // Llama al callback
        }

        return gridView
    }

    override fun getCount(): Int = images.size

    override fun getItem(position: Int): Any = images[position]

    override fun getItemId(position: Int): Long = 0

    // Cambia la imagen temporalmente a una versión brillante
    fun updateImg(position: Int, newImage: Int) {
        images[position] = newImage
        notifyDataSetChanged() // Refresca la vista
    }

    // Restaura la imagen original
    fun resetImg(position: Int) {
        val originalImages = listOf(R.drawable.red, R.drawable.blue, R.drawable.green, R.drawable.yellow)
        images[position] = originalImages[position]
        notifyDataSetChanged()
    }
}
