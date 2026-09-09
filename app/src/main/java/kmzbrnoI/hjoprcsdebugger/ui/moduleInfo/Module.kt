package kmzbrnoI.hjoprcsdebugger.ui.moduleInfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kmzbrnoI.hjoprcsdebugger.databinding.ModuleBinding
import kmzbrnoI.hjoprcsdebugger.network.TCPClientApplication
import kmzbrnoI.hjoprcsdebugger.responses.ModuleResponse

class Module: Fragment(), ModuleResponse {
    public lateinit var adapter: ModuleAdapter
    private var _binding: ModuleBinding? = null
    public val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true

        _binding = ModuleBinding.inflate(inflater, container, false)
        _binding.apply {
            binding.modulesListView.layoutManager = LinearLayoutManager(context)
            binding.modulesListView.adapter = adapter
        }

        TCPClientApplication.getInstance().activityContext = context

        TCPClientApplication.getInstance().delegateModuleResponse = this

        val extras = activity?.intent?.extras

        var inputs: String? = null
        var outputs: String?  = null
        var inputsTypes = ArrayList<String>()
        var outputsTypes = ArrayList<String>()

        extras?.let { e ->
            inputs = e.getString("inputs")
            outputs = e.getString("outputs")
            inputsTypes = parseTypes(e.getString("inputsTypes"))
            outputsTypes = parseTypes(e.getString("outputsTypes"))
        }

        adapter = ModuleAdapter(context, inputs, outputs, inputsTypes, outputsTypes)

        return binding.root;
    }

    private fun parseTypes(types: String?): ArrayList<String> {
        val result = ArrayList<String>()
        if (types == null)
            return result

        for (char in types) {
            result.add(char.toString())
        }

        return result
    }

    override fun response(parsed: ArrayList<String>) {
        adapter.receiveUpdate(parsed)
    }

    override fun response(output: Int, parsed: ArrayList<String>) {}
}