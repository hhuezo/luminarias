package com.dgehm.luminarias.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dgehm.luminarias.R

class CensoOfflineAdapter(
    private val censos: List<CensoOfflineList>
) : RecyclerView.Adapter<CensoOfflineAdapter.CensoOfflineViewHolder>() {

    inner class CensoOfflineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombreLuminaria: TextView = itemView.findViewById(R.id.nombreLuminariaTextView)
        val tvFecha: TextView = itemView.findViewById(R.id.fechaTextView)
        val tvPotenciaNominal: TextView = itemView.findViewById(R.id.potenciaNominalTextView)
        val tvConsumoMensual: TextView = itemView.findViewById(R.id.consumoMensualTextView)
        val tvDireccion: TextView = itemView.findViewById(R.id.direccionTextView)
        val tvObservacion: TextView = itemView.findViewById(R.id.observacionTextView)
        val tvNombreFalla: TextView = itemView.findViewById(R.id.nombreFallaTextView)
        val tvCondicionLampara: TextView = itemView.findViewById(R.id.condicionLamparaTextView)
        val tvNombreCompania: TextView = itemView.findViewById(R.id.nombreCompaniaTextView)
        val tvDistrito: TextView = itemView.findViewById(R.id.distritoTextView)
        val tvDepartamento: TextView = itemView.findViewById(R.id.departamentoTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CensoOfflineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.censo_offline_item, parent, false)
        return CensoOfflineViewHolder(view)
    }

    override fun onBindViewHolder(holder: CensoOfflineViewHolder, position: Int) {
        val censo = censos[position]
        holder.tvNombreLuminaria.text = "Luminaria: ${censo.nombreLuminaria}"
        holder.tvFecha.text = "Fecha: ${censo.fecha}"
        holder.tvPotenciaNominal.text = "Potencia Nominal: ${censo.potenciaNominal}W"
        holder.tvConsumoMensual.text = "Consumo Mensual: ${censo.consumoMensual} kWh"
        holder.tvDireccion.text = "Dirección: ${censo.direccion}"
        holder.tvObservacion.text = "Observación: ${censo.observacion}"
        holder.tvNombreFalla.text = "Falla: ${censo.nombreFalla}"
        holder.tvCondicionLampara.text = "Condición Lámpara: ${censo.condicionLampara}"
        holder.tvNombreCompania.text = "Compañía: ${censo.nombreCompania}"
        holder.tvDistrito.text = "DISTRITO: ${censo.nombreDistrito}"
        holder.tvDepartamento.text = "DEPARTAMENTO: ${censo.nombreDepartamento}"
    }

    override fun getItemCount(): Int = censos.size
}
