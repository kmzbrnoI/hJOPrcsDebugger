package kmzbrnoI.hjoprcsdebugger.ui.moduleInfo

import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.helpers.ParseHelper
import kmzbrnoI.hjoprcsdebugger.network.TCPClientApplication
import kmzbrnoI.hjoprcsdebugger.responses.ModuleResponse
import kotlinx.android.synthetic.main.module.*
import kotlinx.android.synthetic.main.module.view.*

class Module: Fragment(), ModuleResponse {
    private var inputsList: ArrayList<String> = ArrayList()
    lateinit var inputsAdapter: ArrayAdapter<String>

    private var outputsList: ArrayList<String> = ArrayList()
    lateinit var outputsAdapter: ArrayAdapter<String>

    private var handler: Handler = Handler()

    private lateinit var sound: MediaPlayer

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

        sound = MediaPlayer.create(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

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

    private fun findDifferenceAndUpdate(view: ListView, list: ArrayList<String>, parsed: ArrayList<String>) {
        if (list.size == 0) {
            list.addAll(parsed)
        } else {
            for (i in 0 until list.size) {
                if (list[i] != parsed[i]) {
                    list[i] = parsed[i]
                    handler.post {
                        context?.let{ context ->
                            view.getChildAt(i).setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryLighten))
                            sound.start()
                        }
                    }
                    handler.postDelayed( {
                        context?.let{ context ->
                            view.getChildAt(i).setBackgroundColor(ContextCompat.getColor(context,  R.color.transparent))
                        }
                    }, 5000)
                }
            }
        }
    }

    override fun response(parsed: ArrayList<String>) {
        if (parsed[5].toUpperCase() == "I") {
            findDifferenceAndUpdate(inputs_list_view, inputsList, parse(parsed[6]))
            handler.post {
                inputsAdapter.notifyDataSetChanged()
            }
        } else if (parsed[5].toUpperCase() == "O") {
            findDifferenceAndUpdate(outputs_list_view, outputsList, parse(parsed[6]))
            handler.post {
                outputsAdapter.notifyDataSetChanged()
            }
        }
    }

}