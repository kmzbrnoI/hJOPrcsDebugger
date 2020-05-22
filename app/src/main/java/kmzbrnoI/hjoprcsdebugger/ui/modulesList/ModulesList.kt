package kmzbrnoI.hjoprcsdebugger.ui.modulesList

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.helpers.ParseHelper
import kmzbrnoI.hjoprcsdebugger.models.Module
import kmzbrnoI.hjoprcsdebugger.network.TCPClientApplication
import kmzbrnoI.hjoprcsdebugger.responses.ModuleResponse
import kmzbrnoI.hjoprcsdebugger.ui.moduleInfo.ModuleInfoActivity
import kotlinx.android.synthetic.main.modules_list.*
import kotlinx.android.synthetic.main.modules_list.view.*
import kotlin.collections.ArrayList

class ModulesList: Fragment(), ModuleResponse {
    lateinit var modulesList: ArrayList<Module>
    lateinit var mAdapter: ArrayAdapter<String>

    var selectedIndex: Int? = null

    private var selectedModuleInputs: String? = null
    private var selectedModuleOutputs: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true

        TCPClientApplication.getInstance().activityContext = context

        val extras = activity?.intent?.extras
        var modules = extras?.getString("modules")

        // to be able to use parse method as it is
        modules = modules?.replace("{", "[")
        modules = modules?.replace("}", "]")

        val modulesStringArray = modules?.let { ParseHelper().parse(it, "]", "[") }!!
        modulesList = ArrayList(modulesStringArray.map { module -> Module(module) })

        return inflater.inflate(R.layout.modules_list, container, false).apply {
            modules_list_overlay.visibility = View.GONE
            modules_list_loadBar.visibility = View.GONE

            mAdapter = ArrayAdapter(
                context,
                android.R.layout.simple_list_item_1, android.R.id.text1, modulesList.map { module -> "${module.address}: $module" }
            )
            modules_list_view.adapter = mAdapter

            modules_list_view.setOnItemClickListener { _, _, position, _ ->
                connectToModule(position)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        modules_list_overlay.visibility = View.GONE
        modules_list_loadBar.visibility = View.GONE
    }

    private fun connectToModule(index: Int) {
        modules_list_overlay.visibility = View.VISIBLE
        modules_list_loadBar.visibility = View.VISIBLE

        selectedIndex = index

        val tcp = TCPClientApplication.getInstance()
        tcp.delegateModuleResponse = this

        tcp.connectToModule(modulesList[index])
    }

    override fun response(parsed: ArrayList<String>) {
        if (parsed[5].toUpperCase() == "I") {
            selectedModuleInputs = parsed[6]
        } else if (parsed[5].toUpperCase() == "O") {
            selectedModuleOutputs = parsed[6]
        }

        if (selectedModuleInputs != null && selectedModuleOutputs != null) {
            val intent = Intent(context, ModuleInfoActivity::class.java)
            intent.putExtra("inputs", selectedModuleInputs)
            intent.putExtra("outputs", selectedModuleOutputs)
            selectedIndex?.let { index ->
                intent.putExtra("inputsTypes", modulesList[index].inputTypes)
                intent.putExtra("outputsTypes", modulesList[index].outputTypes)
                intent.putExtra("nameOfModule", modulesList[index].name)
            }
            startActivity(intent)
        }

    }
}

