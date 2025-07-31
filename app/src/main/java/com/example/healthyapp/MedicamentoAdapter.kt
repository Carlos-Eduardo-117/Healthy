package com.example.healthyapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class MedicamentoAdapter(private val lista: List<Medicamento>) :
    RecyclerView.Adapter<MedicamentoAdapter.MedicamentoViewHolder>() {

    class MedicamentoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvDosis: TextView = itemView.findViewById(R.id.tvDosis)
        val tvHora: TextView = itemView.findViewById(R.id.tvHora)
        val btnConfirmar: Button = itemView.findViewById(R.id.btnConfirmarToma)
        val btnEditar: Button = itemView.findViewById(R.id.btnEditar)
        val btnEliminar: Button = itemView.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicamentoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicamento, parent, false)
        return MedicamentoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicamentoViewHolder, position: Int) {
        val med = lista[position]
        holder.tvNombre.text = med.nombre
        holder.tvDosis.text = "Dosis: ${med.dosis}"
        holder.tvHora.text = "Hora: ${med.hora}"

        holder.btnConfirmar.setOnClickListener {
            val context = holder.itemView.context
            val db = AppDatabase.getDatabase(context)
            val dao = db.tomaDao()

            val toma = TomaMedicamento(
                medicamentoId = med.id,
                fechaHora = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                    Date()
                )
            )

            (context as AppCompatActivity).lifecycleScope.launch {
                dao.registrarToma(toma)
                Toast.makeText(context, "Toma registrada", Toast.LENGTH_SHORT).show()
            }
        }

        holder.btnEditar.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, AddMedicamentoActivity::class.java)
            intent.putExtra("medicamentoId", med.id)
            context.startActivity(intent)
        }

        holder.btnEliminar.setOnClickListener {
            val context = holder.itemView.context
            val db = AppDatabase.getDatabase(context)
            val dao = db.medicamentoDao()

            (context as AppCompatActivity).lifecycleScope.launch {
                dao.eliminar(med)
                (context as AppCompatActivity).runOnUiThread {
                    Toast.makeText(context, "Medicamento eliminado", Toast.LENGTH_SHORT).show() //Aqui me hace falta agregar algo pq no se elimina hasta que recargo la pantalla
                }
            }
        }
    }

    override fun getItemCount(): Int = lista.size
}
