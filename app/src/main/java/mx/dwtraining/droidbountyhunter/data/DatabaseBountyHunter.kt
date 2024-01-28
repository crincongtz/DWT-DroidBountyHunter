package mx.dwtraining.droidbountyhunter.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import mx.dwtraining.droidbountyhunter.models.Fugitivo

/** ------------------- Nombre de Base de Datos --------------------------**/
const val DATABASE_NAME = "DroidBountyHunterDatabase"
/** ------------------ Versión de Base de Datos --------------------------**/
const val VERSION = 1
/** ---------------------- Tablas y Campos -------------------------------**/
const val TABLE_NAME_FUGITIVOS = "fugitivos"
const val COLUMN_NAME_ID = "id"
const val COLUMN_NAME_NAME = "name"
const val COLUMN_NAME_STATUS = "status"

class DatabaseBountyHunter(val context: Context) {
    private val TAG: String = DatabaseBountyHunter::class.java.simpleName

    /** ------------------- Declaración de Tablas ----------------------------**/
    private val TFugitivos = "CREATE TABLE " + TABLE_NAME_FUGITIVOS + " (" +
            COLUMN_NAME_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            COLUMN_NAME_NAME + " TEXT NOT NULL, " +
            COLUMN_NAME_STATUS + " INTEGER, " +
            "UNIQUE (" + COLUMN_NAME_NAME + ") ON CONFLICT REPLACE);"

    /** ---------------------- Variables y Helpers ---------------------------**/
    private var helper: DBHelper? = null
    private var database: SQLiteDatabase? = null

    inner class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, VERSION) {
        override fun onCreate(db: SQLiteDatabase?) {
            Log.d(TAG, "Creación de la base de datos")
            db!!.execSQL(TFugitivos)
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            Log.w(TAG, "Actualización de la BDD de la versión " + oldVersion + "a la " +
                    newVersion + ", de la que se destruirá la información anterior")
            // Destruir BDD anterior y crearla nuevamente las tablas actualizadas
            db!!.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_FUGITIVOS")
            // Re-creando nuevamente la BDD actualizada
            onCreate(db)
        }
    }

    private fun open(): DatabaseBountyHunter{
        helper = DBHelper(context)
        database = helper!!.writableDatabase
        return this
    }

    private fun close(){
        helper!!.close()
        database!!.close()
    }

    private fun querySQL(sql: String, selectionArgs: Array<String>): Cursor {
        open()
        return database!!.rawQuery(sql, selectionArgs)
    }

    fun borrarFugitivo(fugitivo: Fugitivo){
        open()
        database!!.delete(TABLE_NAME_FUGITIVOS, COLUMN_NAME_ID + "=?", arrayOf(fugitivo.id.toString()))
        close()
    }

    fun actualizarFugitivo(fugitivo: Fugitivo){
        open()
        val values = ContentValues()
        values.put(COLUMN_NAME_NAME, fugitivo.name)
        values.put(COLUMN_NAME_STATUS, fugitivo.status)
        database!!.update(TABLE_NAME_FUGITIVOS, values, COLUMN_NAME_ID + "=?", arrayOf(fugitivo.id.toString()))
        close()
    }

    fun insertarFugitivo(fugitivo: Fugitivo){
        val values = ContentValues()
        values.put(COLUMN_NAME_NAME, fugitivo.name)
        values.put(COLUMN_NAME_STATUS, fugitivo.status)
        open()
        database!!.insert(TABLE_NAME_FUGITIVOS, null, values)
        close()
    }

    fun obtenerFugitivos(status: Int) : Array<Fugitivo> {
        var fugitivos: Array<Fugitivo> = arrayOf()
        val dataCursor = querySQL("SELECT * FROM " + TABLE_NAME_FUGITIVOS + " WHERE " +
                COLUMN_NAME_STATUS + "= ? ORDER BY " + COLUMN_NAME_NAME, arrayOf(status.toString()))
        if (dataCursor.count > 0) {
            fugitivos = generateSequence {
                if (dataCursor.moveToNext()) dataCursor else null
            }.map {
                val name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME_NAME))
                val statusFugitivo = it.getInt(it.getColumnIndexOrThrow(COLUMN_NAME_STATUS))
                val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_NAME_ID))
                return@map Fugitivo(id, name, statusFugitivo)
            }.toList().toTypedArray()
        }
        return fugitivos
    }
}