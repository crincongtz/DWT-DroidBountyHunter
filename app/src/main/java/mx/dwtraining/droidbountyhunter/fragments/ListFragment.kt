package mx.dwtraining.droidbountyhunter.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import mx.dwtraining.droidbountyhunter.R
import mx.dwtraining.droidbountyhunter.data.DatabaseBountyHunter
import mx.dwtraining.droidbountyhunter.databinding.FragmentListBinding
import mx.dwtraining.droidbountyhunter.models.Fugitivo

const val SECTION_NUMBER : String = "section_number"

class ListFragment(private val listener: FugitivoListener) : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Se hace referencia al Fragment generado por XML en los Layouts y
        // se instancia en una View...
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val modo = requireArguments().getInt(SECTION_NUMBER)
        // Datos dummy para la lista
        actualizarDatos(modo)
        binding.listaFugitivosCapturados.setOnItemClickListener { adapterView, view, position, id ->
            val fugitivoList = binding.listaFugitivosCapturados.tag as Array<Fugitivo>
            listener.fugitivoSeleccionado(fugitivoList[position])
        }
    }

    private fun actualizarDatos(modo: Int) {
        val database = DatabaseBountyHunter(requireContext())
        val fugitivos = database.obtenerFugitivos(modo)
        if (fugitivos.isNotEmpty()){
            val values = ArrayList<String?>()
            fugitivos.mapTo(values){ it.name }
            val adaptador = ArrayAdapter<String>(requireContext(), R.layout.item_fugitivo_list, values)
            binding.listaFugitivosCapturados.adapter = adaptador
            binding.listaFugitivosCapturados.tag = fugitivos
        }
    }
}

interface FugitivoListener {
    fun fugitivoSeleccionado(fugitivo: Fugitivo)
}