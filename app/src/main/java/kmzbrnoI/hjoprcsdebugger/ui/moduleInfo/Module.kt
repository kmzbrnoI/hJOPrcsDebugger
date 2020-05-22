package kmzbrnoI.hjoprcsdebugger.ui.moduleInfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.helpers.ParseHelper
import kmzbrnoI.hjoprcsdebugger.network.TCPClientApplication
import kmzbrnoI.hjoprcsdebugger.responses.ModuleResponse
import kotlinx.android.synthetic.main.module.view.*

class Module: Fragment(), ModuleResponse {
    private lateinit var adapter: ModuleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true

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

        adapter = ModuleAdapter(inputs, outputs, inputsTypes, outputsTypes)

        return inflater.inflate(R.layout.module, container, false).apply {
            modules_list_view.layoutManager = LinearLayoutManager(context)
            modules_list_view.adapter = adapter
        }
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