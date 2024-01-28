package mx.dwtraining.droidbountyhunter

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import mx.dwtraining.droidbountyhunter.ui.main.SectionsPagerAdapter
import mx.dwtraining.droidbountyhunter.databinding.ActivityHomeBinding
import mx.dwtraining.droidbountyhunter.fragments.FugitivoListener
import mx.dwtraining.droidbountyhunter.models.Fugitivo

class HomeActivity : AppCompatActivity(), FugitivoListener {

    private lateinit var binding: ActivityHomeBinding

    private var sectionsPagerAdapter: SectionsPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sectionsPagerAdapter = SectionsPagerAdapter(this, this, supportFragmentManager)

        setSupportActionBar(binding.toolbar)

        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter

        binding.tabs.setupWithViewPager(viewPager)

        binding.fab.setOnClickListener {
            resultLauncher.launch(Intent(this, AgregarActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        actualizarListas(it.resultCode)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_agregar -> {
                resultLauncher.launch(Intent(this, AgregarActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun actualizarListas(index: Int){
        binding.viewPager.adapter = sectionsPagerAdapter
        binding.viewPager.currentItem = index
    }

    override fun fugitivoSeleccionado(fugitivo: Fugitivo) {
        val intent = Intent(this, DetalleActivity::class.java)
        intent.putExtra("fugitivo", fugitivo)
        resultLauncher.launch(intent)
    }
}