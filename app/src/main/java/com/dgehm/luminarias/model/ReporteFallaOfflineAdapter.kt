package com.dgehm.luminarias.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dgehm.luminarias.R

class ReporteFallaOfflineAdapter(
    private val reportesFalla: List<ReporteFallaOfflineList>
) : RecyclerView.Adapter<ReporteFallaOfflineAdapter.ReporteFallaViewHolder>() {

    inner class ReporteFallaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDescripcion: TextView = itemView.findViewById(R.id.descripcionTextView)
        val tvFecha: TextView = itemView.findViewById(R.id.fechaTextView)
        val tvDistrito: TextView = itemView.findViewById(R.id.distritoTextView)


        val tvTipo: TextView = itemView.findViewById(R.id.tipoTextView)
        val tvNombreContacto: TextView = itemView.findViewById(R.id.nombreContactoTextView)
        val tvTelefonoContacto: TextView = itemView.findViewById(R.id.telefonoContactoTextView)
        val tvDepartamento: TextView = itemView.findViewById(R.id.departamentoTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReporteFallaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.reporte_falla_item, parent, false)
        return ReporteFallaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReporteFallaViewHolder, position: Int) {
        val reporteFalla = reportesFalla[position]
        holder.tvDescripcion.text = reporteFalla.descripcion
        holder.tvFecha.text = "Fecha: ${reporteFalla.fechaCreacion}"
        holder.tvDistrito.text = "DISTRITO: ${reporteFalla.distrito}"
        holder.tvTipo.text = "Tipo falla: ${reporteFalla.tipoFalla}"
        holder.tvNombreContacto.text = "${reporteFalla.nombreContacto}"
        holder.tvTelefonoContacto.text = "${reporteFalla.telefonoContacto}"
        holder.tvDepartamento.text = "DEPARTAMENTO: ${reporteFalla.departamento}"
    }

    override fun getItemCount(): Int = reportesFalla.size
}
