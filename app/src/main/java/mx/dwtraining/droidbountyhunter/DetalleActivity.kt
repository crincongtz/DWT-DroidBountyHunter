package mx.dwtraining.droidbountyhunter

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import mx.dwtraining.droidbountyhunter.data.DatabaseBountyHunter
import mx.dwtraining.droidbountyhunter.databinding.ActivityDetalleBinding
import mx.dwtraining.droidbountyhunter.models.Fugitivo

class DetalleActivity : AppCompatActivity(){

    private lateinit var binding: ActivityDetalleBinding

    private var fugitivo: Fugitivo? = null
    private var database: DatabaseBountyHunter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fugitivo = IntentCompat.getParcelableExtra(intent, "fugitivo" ,Fugitivo::class.java)

        // Se obtiene el nombre del fugitivo del intent y se usa como título
        title = fugitivo!!.name + " - " + fugitivo!!.id
        // Se identifica si es Fugitivo o capturado para el mensaje...
        if (fugitivo!!.status == 0){
            binding.etiquetaMensaje.text = "El fugitivo sigue suelto..."
        }else{
            binding.etiquetaMensaje.text = "Atrapado!!!"
            binding.botonCapturar.visibility = View.GONE
        }

        setListeners()
    }

    private fun setListeners() {
        binding.botonCapturar.setOnClickListener {
            capturarFugitivoPresionado()
        }
        binding.botonEliminar.setOnClickListener {
            eliminarFugitivoPresionado()
        }
    }

    private fun capturarFugitivoPresionado(){
        database = DatabaseBountyHunter(this)
        fugitivo!!.status = 1
        database!!.actualizarFugitivo(fugitivo!!)
        setResult(1)
        finish()
    }

    private fun eliminarFugitivoPresionado(){
        database = DatabaseBountyHunter(this)
        database!!.borrarFugitivo(fugitivo!!)
        setResult(0)
        finish()
    }
}