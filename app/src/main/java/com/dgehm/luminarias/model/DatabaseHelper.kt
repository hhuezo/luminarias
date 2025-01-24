import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.dgehm.luminarias.model.ReporteFallaOffline
import com.dgehm.luminarias.model.ReporteFallaOfflineList
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(private val context: Context) {

    private val databaseName = "luminarias.db"
    private val databasePath = "${context.filesDir}/$databaseName"

    // Verifica si la base de datos existe
    fun databaseExists(): Boolean {
        val dbFile = File(databasePath)
        return dbFile.exists()
    }

    // Borra la base de datos si existe
    fun deleteDatabase(): Boolean {
        val dbFile = File(databasePath)
        if (dbFile.exists()) {
            val deleted = dbFile.delete()
            if (deleted) {
                println("Base de datos eliminada exitosamente.")
            } else {
                println("No se pudo eliminar la base de datos.")
            }
            return deleted
        }
        println("La base de datos no existe para ser eliminada.")
        return false
    }

    // Copia la base de datos desde la carpeta `assets`
    fun copyDatabase() {
        try {
            val inputStream: InputStream = context.assets.open(databaseName)
            val outputStream = FileOutputStream(databasePath)

            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            println("Base de datos copiada exitosamente a: $databasePath")
        } catch (e: IOException) {
            e.printStackTrace()
            println("Error al copiar la base de datos: ${e.message}")
        }
    }


    // Método para obtener los departamentos
    fun getDepartamentos(): List<String> {
        val departamentos = mutableListOf<String>()

        // Abre la base de datos en modo lectura
        val db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READONLY)

        val query = "SELECT nombre FROM departamento"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                departamentos.add(nombre)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return departamentos
    }

    fun getDepartamentoId(nombre: String): Int {
        var departamentoId = 0

        // Abre la base de datos en modo lectura
        val db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READONLY)

        // Consulta para obtener el ID del departamento por nombre
        val query = "SELECT id FROM departamento WHERE nombre = ?"
        val cursor = db.rawQuery(query, arrayOf(nombre))

        // Si el cursor tiene resultados, obtenemos el ID
        if (cursor.moveToFirst()) {
            departamentoId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        }

        cursor.close()
        db.close()

        return departamentoId
    }


    fun getMunicipios(departamentoId: Int): List<String> {
        val municipios = mutableListOf<String>()

        // Abre la base de datos en modo lectura
        val db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READONLY)

        // Consulta para obtener los municipios según el departamento_id
        val query = "SELECT nombre FROM municipio WHERE departamento_id = ?"
        val cursor = db.rawQuery(query, arrayOf(departamentoId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                municipios.add(nombre)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return municipios
    }

    fun getMunicipioId(departamentoId: Int, nombre: String): Int {
        var municipioId = 0

        // Abre la base de datos en modo lectura
        val db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READONLY)

        // Consulta para obtener el ID del municipio según el departamento_id y nombre
        val query = "SELECT id FROM municipio WHERE departamento_id = ? AND nombre = ?"
        val cursor = db.rawQuery(query, arrayOf(departamentoId.toString(), nombre))

        // Si el cursor tiene resultados, obtenemos el ID
        if (cursor.moveToFirst()) {
            municipioId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        }

        cursor.close()
        db.close()

        return municipioId
    }


    fun getDistritos(municipioId: Int): List<String> {
        val distritos = mutableListOf<String>()

        // Abre la base de datos en modo lectura
        val db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READONLY)

        // Consulta para obtener los distritos según el municipio_id
        val query = "SELECT nombre FROM distrito WHERE municipio_id = ?"
        val cursor = db.rawQuery(query, arrayOf(municipioId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                distritos.add(nombre)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return distritos
    }


    fun getDistritoId(municipioId: Int, nombre: String): Int {
        var distritoId = 0

        // Abre la base de datos en modo lectura
        val db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READONLY)

        // Consulta para obtener el ID del distrito según el municipio_id y nombre
        val query = "SELECT id FROM distrito WHERE municipio_id = ? AND nombre = ?"
        val cursor = db.rawQuery(query, arrayOf(municipioId.toString(), nombre))

        // Si el cursor tiene resultados, obtenemos el ID
        if (cursor.moveToFirst()) {
            distritoId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        }

        cursor.close()
        db.close()

        return distritoId
    }


    fun getTipoFallaId(nombre: String): Int {
        var tipoFallaId = 0

        // Abre la base de datos en modo lectura
        val db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READONLY)

        // Consulta para obtener el ID según el nombre
        val query = "SELECT id FROM tipo_falla WHERE nombre = ?"
        val cursor = db.rawQuery(query, arrayOf(nombre))

        // Si hay resultados, obtenemos el ID
        if (cursor.moveToFirst()) {
            tipoFallaId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        }

        cursor.close()
        db.close()

        return tipoFallaId
    }




    // Método para obtener los tipos de falla
    fun getTiposFalla(): List<String> {
        val tiposFalla = mutableListOf<String>()

        // Abre la base de datos en modo lectura
        val db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READONLY)

        val query = "SELECT nombre FROM tipo_falla"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                tiposFalla.add(nombre)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return tiposFalla
    }



    fun insertReporteFalla(
        distritoId: Int,
        tipoFallaId: Int,
        descripcion: String,
        latitud: String,
        longitud: String,
        telefono: String,
        nombreContacto: String,
        correoContacto: String,
        usuarioId: Int,
        photoUri: String?,
        tipoImagen: String?
    ): Long {
        val db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READWRITE)

        // Crear un objeto ContentValues con los datos que se van a insertar
        val values = ContentValues().apply {
            put("fecha_creacion", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                Date()
            ))
            put("distrito_id", distritoId)
            put("tipo_falla_id", tipoFallaId)
            put("descripcion", descripcion)
            put("latitud", latitud)
            put("longitud", longitud)
            put("telefono_contacto", telefono)
            put("nombre_contacto", nombreContacto)
            put("correo_contacto", correoContacto)
            put("usuario_creacion", usuarioId)
            put("url_foto", photoUri)
            put("tipo_imagen", tipoImagen)
        }

        // Insertar en la tabla 'reporte_falla' y obtener el ID de la fila insertada
        val newRowId = db.insert("reporte_falla", null, values)

        db.close()
        return newRowId
    }



    //metodo para listar reportes de falla
    fun getLIstarReportesFalla(): List<ReporteFallaOfflineList> {
        val reportesFalla = mutableListOf<ReporteFallaOfflineList>()

        // Abre la base de datos en modo lectura
        val db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READONLY)

        val query = "select reporte.id,strftime('%d/%m/%Y %H:%M', reporte.fecha_creacion) AS fecha,tipo.nombre as tipoFalla,  reporte.nombre_contacto as nombreContacto,reporte.telefono_contacto as telefonoContacto,reporte.url_foto  as descripcion,\n" +
                "distrito.nombre as distrito,departamento.nombre as departamento from reporte_falla reporte \n" +
                "inner join distrito on distrito.id = reporte.distrito_id\n" +
                "inner join municipio  on municipio.id = distrito.municipio_id\n" +
                "inner join departamento  on departamento.id = municipio.departamento_id\n" +
                "inner join tipo_falla tipo on tipo.id = reporte.tipo_falla_id"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val fechaCreacion = cursor.getString(cursor.getColumnIndexOrThrow("fecha")) ?: ""
                val tipoFalla = cursor.getString(cursor.getColumnIndexOrThrow("tipoFalla"))
                val nombreContacto = cursor.getString(cursor.getColumnIndexOrThrow("nombreContacto"))
                val telefonoContacto = cursor.getString(cursor.getColumnIndexOrThrow("telefonoContacto"))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"))
                val distrito = cursor.getString(cursor.getColumnIndexOrThrow("distrito"))
                val departamento = cursor.getString(cursor.getColumnIndexOrThrow("departamento"))

                val reporteFalla = ReporteFallaOfflineList(
                    id = id,
                    fechaCreacion = fechaCreacion,
                    descripcion = descripcion,
                    tipoFalla = tipoFalla,
                    distrito = distrito,
                    departamento = departamento,
                    nombreContacto = nombreContacto,
                    telefonoContacto = telefonoContacto
                )

                reportesFalla.add(reporteFalla)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return reportesFalla
    }




    fun getReportesFalla(): List<ReporteFallaOffline> {
        val reportesFalla = mutableListOf<ReporteFallaOffline>()
        // Abre la base de datos en modo lectura
        val db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READONLY)
        val query = "SELECT * FROM reporte_falla"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val fechaCreacion = cursor.getString(cursor.getColumnIndexOrThrow("fecha_creacion"))
                val distritoId = cursor.getInt(cursor.getColumnIndexOrThrow("distrito_id"))
                val tipoFallaId = cursor.getInt(cursor.getColumnIndexOrThrow("tipo_falla_id"))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"))
                val latitud = cursor.getString(cursor.getColumnIndexOrThrow("latitud"))
                val longitud = cursor.getString(cursor.getColumnIndexOrThrow("longitud"))
                val telefonoContacto = cursor.getString(cursor.getColumnIndexOrThrow("telefono_contacto"))
                val nombreContacto = cursor.getString(cursor.getColumnIndexOrThrow("nombre_contacto"))
                val correoContacto = cursor.getString(cursor.getColumnIndexOrThrow("correo_contacto"))
                val usuarioCreacion = cursor.getInt(cursor.getColumnIndexOrThrow("usuario_creacion"))
                val urlFoto = cursor.getString(cursor.getColumnIndexOrThrow("url_foto"))
                val tipoImagen = cursor.getString(cursor.getColumnIndexOrThrow("tipo_imagen"))
                val reporteFalla = ReporteFallaOffline(
                    id, fechaCreacion, distritoId, tipoFallaId, descripcion,
                    latitud, longitud, telefonoContacto, nombreContacto,
                    correoContacto, usuarioCreacion, urlFoto, tipoImagen
                )
                reportesFalla.add(reporteFalla)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return reportesFalla
    }


    fun deleteReporteFallaById(id: Long) {
        val db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READWRITE)
        val query = "DELETE FROM reporte_falla WHERE id = ?"
        val statement = db.compileStatement(query)
        statement.bindLong(1, id)
        statement.executeUpdateDelete()
        db.close()
    }






    //

}
