package kmzbrnoI.hjoprcsdebugger.ui.modulesList

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.constants.ON_RECEIVE_MODULES
import kmzbrnoI.hjoprcsdebugger.helpers.ParseHelper
import kmzbrnoI.hjoprcsdebugger.models.Module
import kmzbrnoI.hjoprcsdebugger.network.TCPClientApplication
import kmzbrnoI.hjoprcsdebugger.responses.ModuleResponse
import kmzbrnoI.hjoprcsdebugger.ui.moduleInfo.ModuleInfoActivity
import kotlinx.android.synthetic.main.module.*
import kotlinx.android.synthetic.main.modules_list.*
import kotlinx.android.synthetic.main.modules_list.view.*
import kotlin.collections.ArrayList

class ModulesList: Fragment(), ModuleResponse {
    lateinit var modulesList: ArrayList<Module>
    lateinit var mAdapter: ArrayAdapter<String>

    var selectedIndex: Int? = null

    private var selectedModuleInputs: String? = null
    private var selectedModuleOutputs: String? = null

    var handler: Handler = Handler()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true

        TCPClientApplication.getInstance().activityContext = context
        TCPClientApplication.getInstance().delegateModuleResponse = this

        val extras = activity?.intent?.extras

        modulesList = parseModules(extras?.getString("modules"))

        return inflater.inflate(R.layout.modules_list, container, false).apply {
            modules_list_overlay.visibility = View.GONE
            modules_list_loadBar.visibility = View.GONE

            mAdapter = ArrayAdapter(
                context,
                android.R.layout.simple_list_item_1, android.R.id.text1, getModulesStrings(modulesList)
            )
            modules_list_view.adapter = mAdapter

            modules_list_view.setOnItemClickListener { _, _, position, _ ->
                connectToModule(position)
            }

            modules_list_view.setOnItemLongClickListener { _, _, position, _ ->
                modules_list_view.getChildAt( position - modules_list_view.firstVisiblePosition)?.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryLighten))
                AlertDialog.Builder(context)
                    .setMessage(getString(R.string.module_address) + ": " + modulesList[position].address + "\n"
                            + getString(R.string.module_name) + ": " + modulesList[position].name + "\n"
                            + getString(R.string.module_type) + ": " + modulesList[position].type + "\n"
                            + getString(R.string.module_present) + ": " + modulesList[position].status + "\n"
                            + getString(R.string.module_firmware) + ": " + modulesList[position].firmware + "\n"
                            + getString(R.string.module_inputsTypes) + ": " + modulesList[position].inputTypes + "\n"
                            + getString(R.string.module_outputsTypes) + ": " + modulesList[position].outputTypes + "\n"
                    )
                    .setTitle(modulesList[position].toString())
                    .setPositiveButton(
                        getString(R.string.ok)
                    ) { _, _ ->
                        modules_list_view.getChildAt(position - modules_list_view.firstVisiblePosition)?.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                    }
                    .setCancelable(true)
                    .show()
                true
            }

            modules_swipe_refresh_layout.setOnRefreshListener {
                reloadModules()
            }
        }
    }

    private fun getModulesStrings(modules: ArrayList<Module>): List<String> {
        return modules.map { module -> module.toString() }
    }

    private fun parseModules(modulesString: String?): ArrayList<Module> {
        if (modulesString == null) {
            return ArrayList()
        }

        // to be able to use parse method as it is
        var modules = modulesString.replace("{", "[")
        modules = modules.replace("}", "]")

        val modulesStringArray = modules.let { ParseHelper().parse(it, "]", "[") }
        return ArrayList(modulesStringArray.map { module -> Module(module) })
    }

    private fun reloadModules() {
        modulesList.clear()

        handler.post {
            mAdapter.clear()
            mAdapter.notifyDataSetChanged()
        }
        TCPClientApplication.getInstance().loadModules()
    }

    private fun onReceiveModules(modules: String) {
        modulesList = parseModules(modules)

        handler.post {
            mAdapter.addAll(getModulesStrings(modulesList))
            mAdapter.notifyDataSetChanged()
            modules_swipe_refresh_layout?.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        modules_list_overlay.visibility = View.GONE
        modules_list_loadBar.visibility = View.GONE

        val tcp = TCPClientApplication.getInstance()
        tcp.delegateModuleResponse = this

        selectedModuleInputs = null
        selectedModuleOutputs = null
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
                intent.putExtra("addressOfModule", modulesList[index].address)
            }

            val tcp = TCPClientApplication.getInstance()
            tcp.delegateModuleResponse = null

            startActivity(intent)
        }

    }

    override fun response(output: Int, parsed: ArrayList<String>) {
        when (output) {
            ON_RECEIVE_MODULES -> {
                onReceiveModules(parsed[3])
            }
        }
    }
}

