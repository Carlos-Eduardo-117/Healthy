package com.example.healthyapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistorialAdapter(private val lista: List<TomaMedicamento>, private val nombres: Map<Int, String>) :
    RecyclerView.Adapter<HistorialAdapter.TomaViewHolder>() {

    class TomaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombreMed: TextView = itemView.findViewById(R.id.tvNombreMed)
        val tvFechaHora: TextView = itemView.findViewById(R.id.tvFechaHora)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TomaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_historial, parent, false)
        return TomaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TomaViewHolder, position: Int) {
        val toma = lista[position]
        holder.tvNombreMed.text = nombres[toma.medicamentoId] ?: "Medicamento desconocido"
        holder.tvFechaHora.text = "Tomado el ${toma.fechaHora}"
    }

    override fun getItemCount(): Int = lista.size
}
