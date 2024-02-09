package mx.dwtraining.droidbountyhunter

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import mx.dwtraining.droidbountyhunter.data.DatabaseBountyHunter
import mx.dwtraining.droidbountyhunter.databinding.ActivityDetalleBinding
import mx.dwtraining.droidbountyhunter.models.Fugitivo
import mx.dwtraining.droidbountyhunter.models.FugitivoRequest
import mx.dwtraining.droidbountyhunter.models.FugitivoResponse
import mx.dwtraining.droidbountyhunter.network.ApiClient
import mx.dwtraining.droidbountyhunter.network.NetworkHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetalleActivity : AppCompatActivity(){

    private lateinit var binding: ActivityDetalleBinding

    private var UDID: String? = ""
    private var fugitivo: Fugitivo? = null
    private var database: DatabaseBountyHunter? = null

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        UDID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

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

        showProgressBar()

        val call = ApiClient.apiService.postFugitivo(FugitivoRequest(UDID ?: ""))
        call.enqueue(object : Callback<FugitivoResponse> {
            override fun onResponse(call: Call<FugitivoResponse>, response: Response<FugitivoResponse>) {
                hideProgressBar()
                if (response.isSuccessful) {
                    val fugitivoResponse = response.body()
                    mensajeDeCerrado(fugitivoResponse?.mensaje ?: "Fugitivo atrapado!!")
                } else {
                    // Handle error
                    NetworkHelper.manageError(this@DetalleActivity, response.code(), response.message())
                }
            }

            override fun onFailure(call: Call<FugitivoResponse>, t: Throwable) {
                // Handle failure
                t.printStackTrace()
                hideProgressBar()
                NetworkHelper.manageError(this@DetalleActivity, NetworkHelper.ERR_NAME_NOT_RESOLVED, t.cause.toString())
            }
        })
    }

    private fun eliminarFugitivoPresionado(){
        database = DatabaseBountyHunter(this)
        database!!.borrarFugitivo(fugitivo!!)
        setResult(0)
        finish()
    }

    fun mensajeDeCerrado(mensaje: String) {
        val builder = AlertDialog.Builder(this)
        builder.create()
        builder.setTitle("Alerta!!")
            .setMessage(mensaje)
            .setOnDismissListener {
                setResult(fugitivo!!.status)
                finish()
            }.show()
    }

    private fun showProgressBar() {
        binding.progressIndicator.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressIndicator.visibility = View.GONE
    }
}