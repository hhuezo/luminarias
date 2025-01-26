package com.dgehm.luminarias.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dgehm.luminarias.R

class CensoAdapter(
    private val censos: List<CensoIndex>,
    private val listener: OnCensoClickListener
) : RecyclerView.Adapter<CensoAdapter.CensoViewHolder>() {

    // Interfaz para manejar los clics en los ítems
    interface OnCensoClickListener {
        fun onCensoClick(id: Int)
    }

    // ViewHolder para cada ítem
    class CensoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fechaTextView: TextView = itemView.findViewById(R.id.fechaTextView)
        val tipoLuminariaTextView: TextView = itemView.findViewById(R.id.nombreLuminariaTextView)
        val potenciaNominalTextView: TextView = itemView.findViewById(R.id.potenciaNominalTextView)
        val consumoMensualTextView: TextView = itemView.findViewById(R.id.consumoMensualTextView)
        val direccionTextView: TextView = itemView.findViewById(R.id.direccionTextView)
        val observacionTextView: TextView = itemView.findViewById(R.id.observacionTextView)
        val companiaTextView: TextView = itemView.findViewById(R.id.nombreCompaniaTextView)
        val distritoTextView: TextView = itemView.findViewById(R.id.distritoTextView)
        // Puedes agregar más vistas según sea necesario
    }

    // Crear el ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CensoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.censo_item, parent, false)
        return CensoViewHolder(view)
    }

    // Asignar los datos a cada ViewHolder
    override fun onBindViewHolder(holder: CensoViewHolder, position: Int) {
        val censo = censos[position]

        // Asignar valores a las vistas
        holder.fechaTextView.text = "Fecha: ${censo.fecha ?: "Fecha no disponible"}"
        holder.tipoLuminariaTextView.text = "Tipo luminaria: ${censo.tipoLuminaria}"
        holder.potenciaNominalTextView.text = "Potencia: ${censo.potenciaNominal}W"
        holder.consumoMensualTextView.text = "Consumo Mensual: ${censo.consumoMensual} kWh"
        holder.direccionTextView.text = "Dirección: ${censo.direccion}"
        holder.observacionTextView.text = "Observación: ${censo.observacion}"

        // Compañía y Distrito
        holder.companiaTextView.text = "Compañía: ${censo.compania}"
        holder.distritoTextView.text = "DISTRITO: ${censo.distrito}"

        // Configurar el listener para el clic en el ítem
        holder.itemView.setOnClickListener {
            listener.onCensoClick(censo.id)
        }
    }

    // Retornar el tamaño de la lista
    override fun getItemCount(): Int {
        return censos.size
    }
}
