package kmzbrnoI.hjoprcsdebugger.ui.moduleInfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.helpers.ParseHelper
import kmzbrnoI.hjoprcsdebugger.network.TCPClientApplication
import kmzbrnoI.hjoprcsdebugger.responses.ModuleResponse
import kotlinx.android.synthetic.main.module.view.*

class Module: Fragment(), ModuleResponse {
    private var inputsList: ArrayList<String> = ArrayList()
    lateinit var inputsAdapter: ArrayAdapter<String>

    private var outputsList: ArrayList<String> = ArrayList()
    lateinit var outputsAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true

        TCPClientApplication.getInstance().delegateModuleResponse = this

        val extras = activity?.intent?.extras

        extras?.let { e ->
            inputsList = parse(e.getString("inputs"))
            outputsList = parse(e.getString("outputs"))
        }

        return inflater.inflate(R.layout.module, container, false).apply {
            inputsAdapter = ArrayAdapter(
                context,
                android.R.layout.simple_list_item_1, android.R.id.text1, inputsList
            )

            inputs_list_view.adapter = inputsAdapter

            outputsAdapter = ArrayAdapter(
                context,
                android.R.layout.simple_list_item_1, android.R.id.text1, outputsList
            )

            outputs_list_view.adapter = outputsAdapter
        }
    }

    private fun parse(types: String?): ArrayList<String> {
        if (types == null)
            return ArrayList()

        return ParseHelper().parse(types, "|", "")
    }

    override fun response(parsed: ArrayList<String>) {
        if (parsed[5].toUpperCase() == "I") {
            inputsList = parse(parsed[6])
            inputsAdapter.notifyDataSetChanged()
        } else if (parsed[5].toUpperCase() == "O") {
            outputsList = parse(parsed[6])
            outputsAdapter.notifyDataSetChanged()
        }
    }

}