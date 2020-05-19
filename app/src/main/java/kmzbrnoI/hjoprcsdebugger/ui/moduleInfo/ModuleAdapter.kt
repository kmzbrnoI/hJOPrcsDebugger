package kmzbrnoI.hjoprcsdebugger.ui.moduleInfo

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.helpers.ParseHelper
import kotlinx.android.synthetic.main.module_row.view.*

class ModuleAdapter(
    inputs: String?,
    outputs: String?,
    private var inputsTypes: ArrayList<String>,
    private var outputsTypes: ArrayList<String>
): RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>() {
    private var handler: Handler = Handler()
    private lateinit var sound: MediaPlayer

    private var inputsList= parse(inputs)
    private var outputsList = parse(outputs)

    private var inputsChanged =  ArrayList<Boolean>()
    private var outputsChanged = ArrayList<Boolean>()

    enum class Type {
        Input,
        Output,
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        sound = MediaPlayer.create(parent.context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

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

    private fun parse(types: String?): ArrayList<String> {
        if (types == null)
            return ArrayList()

        return ParseHelper().parse(types, "|", "")
    }

    private fun findDifferenceAndUpdate(list: ArrayList<String>, parsed: ArrayList<String>, type: Type) {
        if (list.size == 0) {
            list.addAll(parsed)
        } else {
            for (i in 0 until list.size) {
                if (list[i] != parsed[i]) {
                    list[i] = parsed[i]
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
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    fun receiveUpdate(parsed: ArrayList<String>) {
        if (parsed[5].toUpperCase() == "I") {
            findDifferenceAndUpdate(inputsList, parse(parsed[6]), Type.Input)
        } else if (parsed[5].toUpperCase() == "O") {
            findDifferenceAndUpdate(outputsList, parse(parsed[6]), Type.Output)
        }
        handler.post {
            notifyDataSetChanged()
            sound.start()
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
                color = R.color.blue
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
            view.row_index.text = "${position + 1}:"

            view.input.setBackgroundColor(getColor(inputValue, inputType))
            view.output.setBackgroundColor(getColor(outputValue, outputType))

            view.input_wrapper.setBackgroundColor(getWrapperColor(inputHasChanged))
            view.output_wrapper.setBackgroundColor(getWrapperColor(outputHasChanged))
        }
    }
}