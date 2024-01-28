package mx.dwtraining.droidbountyhunter.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import mx.dwtraining.droidbountyhunter.DetalleActivity
import mx.dwtraining.droidbountyhunter.R
import mx.dwtraining.droidbountyhunter.databinding.FragmentListBinding

const val SECTION_NUMBER : String = "section_number"

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Se hace referencia al Fragment generado por XML en los Layouts y
        // se instancia en una View...
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val modo = arguments?.getInt(SECTION_NUMBER)
        // Datos dummy para la lista
        val dummyData = listOf(
                "Sergio Anguiano",
                "Arturo Ceballos",
                "Jonatan Juarez",
                "Ivan Aguirre",
                "Osvaldo Gomez",
                "Karen Muñoz",
        )
        val adaptador = ArrayAdapter(requireContext(), R.layout.item_fugitivo_list, dummyData)
        binding.listaFugitivosCapturados.adapter = adaptador
        binding.listaFugitivosCapturados.setOnItemClickListener { adapterView, view, position, id ->
            val intent = Intent(context, DetalleActivity::class.java)
            intent.putExtra("titulo", (view as TextView).text)
            intent.putExtra("modo", modo)
            startActivity(intent)
        }
    }
}