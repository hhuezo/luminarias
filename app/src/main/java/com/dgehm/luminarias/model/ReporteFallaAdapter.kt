package com.dgehm.luminarias.model

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dgehm.luminarias.R

class ReporteFallaAdapter(
    private val reporteFallas: List<ReporteFalla>,
    private val listener: OnReporteFallaClickListener
) : RecyclerView.Adapter<ReporteFallaAdapter.ReporteFallaViewHolder>() {

    // Interfaz para manejar los clics en los ítems
    interface OnReporteFallaClickListener {
        fun onReporteFallaClick(id: Int)
    }

    // ViewHolder para cada ítem
    class ReporteFallaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fechaTextView: TextView = itemView.findViewById(R.id.fechaTextView)
        val tipoTextView: TextView = itemView.findViewById(R.id.tipoTextView)
        val nombreContactoTextView: TextView = itemView.findViewById(R.id.nombreContactoTextView)
        val telefonoContactoTextView: TextView = itemView.findViewById(R.id.telefonoContactoTextView)
        val descripcionTextView: TextView = itemView.findViewById(R.id.descripcionTextView)
        val distritoTextView: TextView = itemView.findViewById(R.id.distritoTextView)
        //val municipioTextView: TextView = itemView.findViewById(R.id.municipioTextView)
        val departamentoTextView: TextView = itemView.findViewById(R.id.departamentoTextView)
        val estadoReporteView: View = itemView.findViewById(R.id.estadoReporteView)
    }

    // Crear el ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReporteFallaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reporte_falla_item, parent, false)
        return ReporteFallaViewHolder(view)
    }

    // Asignar los datos a cada ViewHolder
    override fun onBindViewHolder(holder: ReporteFallaViewHolder, position: Int) {
        val reporteFalla = reporteFallas[position]

        // Asignar valores a las vistas
        holder.fechaTextView.text = reporteFalla.fecha ?: "Fecha no disponible"
        holder.tipoTextView.text = "Tipo falla: "+reporteFalla.tipo ?: "Tipo no disponible"
        holder.nombreContactoTextView.text = reporteFalla.nombre_contacto ?: "Nombre no disponible"
        holder.telefonoContactoTextView.text = reporteFalla.telefono_contacto ?: "Teléfono no disponible"
        holder.descripcionTextView.text = reporteFalla.descripcion ?: "Descripción no disponible"
        holder.distritoTextView.text = reporteFalla.distrito ?: "Distrito no disponible"
        //holder.municipioTextView.text = reporteFalla.municipio ?: "Municipio no disponible"
        holder.departamentoTextView.text = reporteFalla.departamento ?: "Departamento no disponible"

        // Cambiar el color dependiendo del estado del reporte
        if (reporteFalla.estado_reporte_id == 1) {
            holder.estadoReporteView.setBackgroundColor(Color.parseColor("#4CAF50")) // Verde
        } else {
            holder.estadoReporteView.setBackgroundColor(Color.parseColor("#FFA500")) // Naranja
        }

        // Configurar el listener para el clic en el ítem
        holder.itemView.setOnClickListener {
            reporteFalla.estado_reporte_id?.let { id -> listener.onReporteFallaClick(id) }
        }
    }

    // Retornar el tamaño de la lista
    override fun getItemCount(): Int {
        return reporteFallas.size
    }
}
