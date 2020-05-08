package kmzbrnoI.hjoprcsdebugger.ui.modulesList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.helpers.ParseHelper
import kmzbrnoI.hjoprcsdebugger.models.Module
import kotlinx.android.synthetic.main.modules_list.view.*
import java.util.ArrayList

class ModulesList: Fragment() {
    lateinit var modulesList: ArrayList<Module>
    lateinit var mAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true

        val extras = activity?.intent?.extras
        var modules = extras?.getString("modules")

        // to be able to use parse method as it is
        modules = modules?.replace("{", "[")
        modules = modules?.replace("}", "]")

        val modulesStringArray = modules?.let { ParseHelper().parse(it, "]", "[") }!!
        modulesList = ArrayList(modulesStringArray.map { module -> Module(module) })

        return inflater.inflate(R.layout.modules_list, container, false).apply {
            mAdapter = ArrayAdapter(
                context,
                android.R.layout.simple_list_item_1, android.R.id.text1, modulesList.map { module -> module.toString() }
            )
            modules_list_view.adapter = mAdapter
        }
    }
}