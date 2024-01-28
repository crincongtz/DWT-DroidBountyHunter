package mx.dwtraining.droidbountyhunter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import mx.dwtraining.droidbountyhunter.databinding.ActivityDetalleBinding

class DetalleActivity : AppCompatActivity(){

    private lateinit var binding: ActivityDetalleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Se obtiene el nombre del fugitivo del intent y se usa como título
        title =  intent.extras?.getString("titulo")
        // Se identifica si es Fugitivo o capturado para el mensaje...
        if (intent.extras?.getInt("modo") == 0){
            binding.etiquetaMensaje.text = "El fugitivo sigue suelto..."
        }else{
            binding.etiquetaMensaje.text = "Atrapado!!!"
        }
    }
}