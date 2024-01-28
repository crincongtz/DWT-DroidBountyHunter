package mx.dwtraining.droidbountyhunter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import mx.dwtraining.droidbountyhunter.databinding.ActivityAgregarBinding

class AgregarActivity : AppCompatActivity(){

    private lateinit var binding: ActivityAgregarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}