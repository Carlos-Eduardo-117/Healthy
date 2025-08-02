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
            val tomaDao = db.tomaDao()
            val medDao = db.medicamentoDao()

            (context as AppCompatActivity).lifecycleScope.launch {
                val fechaActual = Date()
                val formato = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val toma = TomaMedicamento(
                    medicamentoId = med.id,
                    fechaHora = formato.format(fechaActual)
                )
                tomaDao.registrarToma(toma)

                val calendar = java.util.Calendar.getInstance().apply {
                    time = fechaActual
                    add(java.util.Calendar.HOUR_OF_DAY, med.intervaloHoras)
                }
                val nuevaHora = String.format("%02d:%02d", calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE))

                val medActualizado = med.copy(hora = nuevaHora)
                medDao.actualizar(medActualizado)

                Toast.makeText(context, "Toma registrada. Próxima: $nuevaHora", Toast.LENGTH_SHORT).show()

                (context as HomeActivity).runOnUiThread {
                    context.cargarMedicamentos()
                }
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
