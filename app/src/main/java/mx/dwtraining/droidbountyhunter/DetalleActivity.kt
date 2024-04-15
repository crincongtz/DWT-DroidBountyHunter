package mx.dwtraining.droidbountyhunter

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.IntentCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import mx.dwtraining.droidbountyhunter.data.DatabaseBountyHunter
import mx.dwtraining.droidbountyhunter.databinding.ActivityDetalleBinding
import mx.dwtraining.droidbountyhunter.models.Fugitivo
import mx.dwtraining.droidbountyhunter.models.FugitivoRequest
import mx.dwtraining.droidbountyhunter.models.FugitivoResponse
import mx.dwtraining.droidbountyhunter.network.ApiClient
import mx.dwtraining.droidbountyhunter.network.NetworkHelper
import mx.dwtraining.droidbountyhunter.utils.PermissionUtils
import mx.dwtraining.droidbountyhunter.utils.PermissionUtils.permissionUseGPS
import mx.dwtraining.droidbountyhunter.utils.PictureTools
import mx.dwtraining.droidbountyhunter.utils.PictureTools.Companion.MEDIA_TYPE_IMAGE
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetalleActivity : AppCompatActivity(){

    private lateinit var binding: ActivityDetalleBinding

    private var UDID: String? = ""
    private var fugitivo: Fugitivo? = null
    private var database: DatabaseBountyHunter? = null

    private val REQUEST_CODE_GPS = 1234

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLocationObjects()

        UDID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fugitivo = IntentCompat.getParcelableExtra(intent, "fugitivo" ,Fugitivo::class.java)

        // Se obtiene el nombre del fugitivo del intent y se usa como título
        title = fugitivo!!.name + " - " + fugitivo!!.id
        // Se identifica si es Fugitivo o capturado para el mensaje...
        if (fugitivo!!.status == 0){
            binding.etiquetaMensaje.text = "El fugitivo sigue suelto..."
            activarGPS()
        }else{
            binding.etiquetaMensaje.text = "Atrapado!!!"
            binding.botonCapturar.visibility = View.GONE

            fugitivo!!.photo?.let {
                Glide.with(this)
                    .load(it)
                    .into(binding.pictureFugitive)
            }
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
        binding.botonTomarFoto.setOnClickListener {
            tomarFotoFugitivo()
        }
        binding.botonVerMapa.setOnClickListener {
            abrirMapa()
        }
    }

    private fun setupLocationObjects() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).apply {
            setMinUpdateDistanceMeters(100f)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()

        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    fugitivo!!.latitude = location.latitude
                    fugitivo!!.longitude = location.longitude
                } else {
                    Log.d("LocationCallback", "Location missing in callback.")
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            android.R.id.home -> {
                setResult(fugitivo!!.status)
                onBackPressedDispatcher.onBackPressed()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun capturarFugitivoPresionado(){
        database = DatabaseBountyHunter(this)
        fugitivo!!.status = 1
        if (fugitivo!!.photo.isNullOrEmpty()){
            Toast.makeText(this,
                "Es necesario tomar la foto antes de capturar al fugitivo",
                Toast.LENGTH_LONG).show()
            return
        }
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

    private fun tomarFotoFugitivo() {
        if (PermissionUtils.permissionReadMemory(this)){
            obtenFotoDeCamara()
        }
    }

    private fun abrirMapa() {
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("fugitivo", fugitivo)
        startActivity(intent)
    }

    private fun obtenFotoDeCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val direccionImagen = PictureTools.getOutputMediaFileUri(this, MEDIA_TYPE_IMAGE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, direccionImagen)
        resultLauncher.launch(intent)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            fugitivo!!.photo = PictureTools.currentPhotoPath

            Glide.with(this)
                .load(fugitivo!!.photo)
                .into(binding.pictureFugitive)
        }
    }

    @SuppressLint("MissingPermission")
    private fun activarGPS() {
        if (permissionUseGPS(this, REQUEST_CODE_GPS)) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            Toast.makeText(this, "Activando GPS...", Toast.LENGTH_LONG).show()

            fusedLocationClient.lastLocation.addOnSuccessListener {location ->
                fugitivo!!.latitude = location.latitude
                fugitivo!!.longitude = location.longitude
            }
        }
    }

    private fun apagarGPS() {
        Toast.makeText(this, "Desactivando GPS...", Toast.LENGTH_LONG).show()

        val removeTask = fusedLocationClient?.removeLocationUpdates(locationCallback)
        removeTask?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("LocationRequest", "Location Callback removed")
            } else {
                Log.w("LocationRequest", "Failed to remove Location Callback")
            }
        }
    }

    override fun onStop() {
        super.onStop()
        apagarGPS()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PictureTools.REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    obtenFotoDeCamara()
                } else {
                    Log.w("RequestPermissions", "Camera - Not Granted")
                }
            } else {
                if  (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                {
                    obtenFotoDeCamara()
                } else {
                    Log.w("RequestPermissions", "Camera - Not Granted")
                }
            }
        } else if (requestCode == REQUEST_CODE_GPS) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                activarGPS()
            } else {
                Log.w("RequestPermissions", "GPS - Not Granted")
            }
        }
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