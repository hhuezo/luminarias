import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

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
}
