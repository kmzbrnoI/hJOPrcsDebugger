package kmzbrnoI.hjoprcsdebugger.ui.moduleInfo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.constants.SCOMTypes
import kmzbrnoI.hjoprcsdebugger.constants.getSCOMTypesStrings
import kmzbrnoI.hjoprcsdebugger.helpers.ParseHelper
import kmzbrnoI.hjoprcsdebugger.network.TCPClientApplication
import kotlinx.android.synthetic.main.module_row.view.*

class ModuleAdapter(
    private var context: Context?,
    inputs: String?,
    outputs: String?,
    private var inputsTypes: ArrayList<String>,
    private var outputsTypes: ArrayList<String>
) : RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>() {
    private var handler: Handler = Handler()
    var sound: MediaPlayer? = null

    private var inputsList: ArrayList<String> = parse(inputs)
    private var outputsList: ArrayList<String> = parse(outputs)

    private var inputsChanged =  ArrayList<Boolean>()
    private var outputsChanged = ArrayList<Boolean>()

    private var requestWasSend = false

    enum class Type {
        Input,
        Output,
    }

    init {
        context?.let { c ->
            sound = MediaPlayer.create(c, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        for (i in 0 until inputsList.size) {
            inputsChanged.add(false)
        }

        for (i in 0 until outputsList.size) {
            outputsChanged.add(false)
        }

        val view = LayoutInflater.from(parent.context).inflate(R.layout.module_row, parent, false)
        return ModuleViewHolder(view)
    }

    override fun getItemCount(): Int = inputsList.size

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        holder.bind(position, inputsList[position], outputsList[position], inputsTypes[position], outputsTypes[position], inputsChanged[position], outputsChanged[position])
    }

    private fun onItemClicked(view: View, position: Int) {
        if (outputsTypes[position] == "I") {
            var newValue = ""

            if (outputsList[position] == "0") {
                newValue = "1"
            } else if (outputsList[position] == "1") {
                newValue = "0"
            }

            view.output.setBackgroundColor(ContextCompat.getColor(view.context, R.color.yellow))
            TCPClientApplication.getInstance().changeOutput(position, newValue)
            requestWasSend = true
        } else if (outputsTypes[position] == "B") {
            var newValue = "0"

            if (outputsList[position] == "0") {
                newValue = "1"
            }

            view.output.setBackgroundColor(ContextCompat.getColor(view.context, R.color.yellow))
            TCPClientApplication.getInstance().changeOutput(position, newValue)
            requestWasSend = true
        } else if (outputsTypes[position] == "S") {
            var SCOMTypesStrings = getSCOMTypesStrings()
            var selectedId = outputsList[position].toInt()
            var wasInList = true

            if (!SCOMTypes.containsKey(selectedId)) {
                SCOMTypesStrings = SCOMTypesStrings.plus("$selectedId: ")
                selectedId = SCOMTypesStrings.lastIndex
                wasInList = false
            }

            AlertDialog.Builder(context)
                .setTitle(context?.getString(R.string.change_module_scom_to))
                .setSingleChoiceItems(SCOMTypesStrings, selectedId) { dialog, which ->
                    var selectedOption = which

                    if (!wasInList && selectedOption == SCOMTypes.size) {
                        selectedOption = outputsList[position].toInt()
                    }
                    TCPClientApplication.getInstance().changeOutput(position, selectedOption.toString())
                    requestWasSend = true

                    view.output.setBackgroundColor(ContextCompat.getColor(view.context, R.color.yellow))

                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun parse(types: String?): ArrayList<String> {
        if (types == null)
            return ArrayList()

        return ParseHelper().parse(types, "|", "")
    }

    private fun findDifferenceAndUpdate(list: ArrayList<String>, parsed: ArrayList<String>, type: Type): Boolean {
        var changesDetected = false
        if (list.size == 0) {
            list.addAll(parsed)
            return true
        } else {
            for (i in 0 until list.size) {
                if (list[i] != parsed[i]) {
                    changesDetected = true
                    list[i] = parsed[i]
                    if (!requestWasSend) {
                        if (type == Type.Input) {
                            inputsChanged[i] = true

                            handler.postDelayed({
                                inputsChanged[i] = false
                                notifyDataSetChanged()
                            }, 5000)

                        } else if (type == Type.Output) {
                            outputsChanged[i] = true

                            handler.postDelayed({
                                outputsChanged[i] = false
                                notifyDataSetChanged()
                            }, 5000)
                        }
                        handler.post {
                            sound?.start()
                        }
                    }
                }
            }
        }
        return changesDetected
    }

    @SuppressLint("DefaultLocale")
    fun receiveUpdate(parsed: ArrayList<String>) {
        var changesDetected = false

        if (parsed[5].toUpperCase() == "I") {
            changesDetected = findDifferenceAndUpdate(inputsList, parse(parsed[6]), Type.Input)
        } else if (parsed[5].toUpperCase() == "O") {
            changesDetected = findDifferenceAndUpdate(outputsList, parse(parsed[6]), Type.Output)
            requestWasSend = false
        }

        if (changesDetected) {
            handler.post {
                notifyDataSetChanged()
            }
        }
    }

    inner class ModuleViewHolder(private val view: View): RecyclerView.ViewHolder(view) {

        private fun getColor(value: String, type: String): Int {
            var color = R.color.black

            if (type == "B" || type == "I") {
                if(value == "0") {
                    color = R.color.red
                } else if (value == "1") {
                    color = R.color.green
                }
            } else if (type == "S") {
                if (value == "0") {
                    color = R.color.blue
                } else {
                    color = R.color.purple
                }
            }

            return ContextCompat.getColor(itemView.context, color)
        }

        private fun getWrapperColor(hasChanged: Boolean): Int {
            var color = R.color.transparent

            if (hasChanged) {
                color = R.color.colorPrimaryLighten
            }

            return ContextCompat.getColor(itemView.context, color)
        }

        @SuppressLint("SetTextI18n")
        fun bind(position: Int, inputValue: String, outputValue: String, inputType: String, outputType: String, inputHasChanged: Boolean, outputHasChanged: Boolean) {
            view.row_index.text = "${position}:"

            view.input.setBackgroundColor(getColor(inputValue, inputType))
            view.output.setBackgroundColor(getColor(outputValue, outputType))

            view.input_wrapper.setBackgroundColor(getWrapperColor(inputHasChanged))
            view.output_wrapper.setBackgroundColor(getWrapperColor(outputHasChanged))

            view.output.setOnClickListener { view -> onItemClicked(view, position) }
        }
    }
}