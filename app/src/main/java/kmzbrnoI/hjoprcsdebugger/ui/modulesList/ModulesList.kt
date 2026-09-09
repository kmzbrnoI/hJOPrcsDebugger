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
import kmzbrnoI.hjoprcsdebugger.databinding.ModulesListBinding
import kmzbrnoI.hjoprcsdebugger.helpers.ParseHelper
import kmzbrnoI.hjoprcsdebugger.models.Module
import kmzbrnoI.hjoprcsdebugger.network.TCPClientApplication
import kmzbrnoI.hjoprcsdebugger.responses.ModuleResponse
import kmzbrnoI.hjoprcsdebugger.ui.moduleInfo.ModuleInfoActivity
import kotlin.collections.ArrayList

class ModulesList: Fragment(), ModuleResponse {
    private var _binding: ModulesListBinding? = null
    public val binding get() = _binding!!

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

        _binding = ModulesListBinding.inflate(inflater, container, false)
        _binding.apply {
            binding.modulesListOverlay.visibility = View.GONE
            binding.modulesListLoadBar.visibility = View.GONE

            mAdapter = ArrayAdapter(
                context!!,
                android.R.layout.simple_list_item_1, android.R.id.text1, getModulesStrings(modulesList)
            )
            binding.modulesListView.adapter = mAdapter

            binding.modulesListView.setOnItemClickListener { _, _, position, _ ->
                connectToModule(position)
            }

            binding.modulesListView.setOnItemLongClickListener { _, _, position, _ ->
                binding.modulesListView.getChildAt( position - binding.modulesListView.firstVisiblePosition)?.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorPrimaryLighten))
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
                        binding.modulesListView.getChildAt(position - binding.modulesListView.firstVisiblePosition)?.setBackgroundColor(ContextCompat.getColor(context!!, R.color.transparent))
                    }
                    .setCancelable(true)
                    .show()
                true
            }

            binding.modulesSwipeRefreshLayout.setOnRefreshListener {
                reloadModules()
            }
        }

        return binding.root
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
            if (_binding == null) return@post
            mAdapter.clear()
            mAdapter.notifyDataSetChanged()
        }
        TCPClientApplication.getInstance().loadModules()
    }

    private fun onReceiveModules(modules: String) {
        modulesList = parseModules(modules)

        handler.post {
            val binding = _binding ?: return@post
            mAdapter.clear()
            mAdapter.addAll(getModulesStrings(modulesList))
            mAdapter.notifyDataSetChanged()
            binding.modulesSwipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        binding.modulesListOverlay.visibility = View.GONE
        binding.modulesListLoadBar.visibility = View.GONE

        val tcp = TCPClientApplication.getInstance()
        tcp.delegateModuleResponse = this

        selectedModuleInputs = null
        selectedModuleOutputs = null
    }

    private fun connectToModule(index: Int) {
        binding.modulesListOverlay.visibility = View.VISIBLE
        binding.modulesListLoadBar.visibility = View.VISIBLE

        selectedIndex = index

        val tcp = TCPClientApplication.getInstance()
        tcp.delegateModuleResponse = this

        tcp.connectToModule(modulesList[index])
    }

    override fun response(parsed: ArrayList<String>) {
        if (!isAdded) return
        if (parsed.size < 7) return
        if (parsed[5].uppercase() == "I") {
            selectedModuleInputs = parsed[6]
        } else if (parsed[5].uppercase() == "O") {
            selectedModuleOutputs = parsed[6]
        }

        if (selectedModuleInputs != null && selectedModuleOutputs != null) {
            val intent = Intent(context, ModuleInfoActivity::class.java)
            intent.putExtra("inputs", selectedModuleInputs)
            intent.putExtra("outputs", selectedModuleOutputs)
            selectedIndex?.let { index ->
                if (index < modulesList.size) {
                    intent.putExtra("inputsTypes", modulesList[index].inputTypes)
                    intent.putExtra("outputsTypes", modulesList[index].outputTypes)
                    intent.putExtra("nameOfModule", modulesList[index].name)
                    intent.putExtra("addressOfModule", modulesList[index].address)
                }
            }

            val tcp = TCPClientApplication.getInstance()
            tcp.delegateModuleResponse = null

            handler.post {
                if (isAdded) {
                    startActivity(intent)
                }
            }
        }

    }

    override fun response(output: Int, parsed: ArrayList<String>) {
        when (output) {
            ON_RECEIVE_MODULES -> {
                onReceiveModules(parsed[3])
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (TCPClientApplication.getInstance().activityContext == context) {
            TCPClientApplication.getInstance().activityContext = null
        }
        if (TCPClientApplication.getInstance().delegateModuleResponse == this) {
            TCPClientApplication.getInstance().delegateModuleResponse = null
        }
        _binding = null
    }
}

