package kmzbrnoI.hjoprcsdebugger.ui.moduleInfo

import android.annotation.SuppressLint
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
    private var layoutManager: LinearLayoutManager,
    inputs: String?,
    outputs: String?,
    private var inputsTypes: ArrayList<String>,
    private var outputsTypes: ArrayList<String>
): RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>() {
    private var handler: Handler = Handler()
    private lateinit var sound: MediaPlayer
    private lateinit var parent: ViewGroup

    private var inputsList= parse(inputs)
    private var outputsList = parse(outputs)


    enum class Type {
        Input,
        Output,
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        sound = MediaPlayer.create(parent.context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        this.parent = parent

        val view = LayoutInflater.from(parent.context).inflate(R.layout.module_row, parent, false)

        return  ModuleViewHolder(view)
    }

    override fun getItemCount(): Int = inputsList.size

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        holder.bind(position, inputsList[position], outputsList[position], inputsTypes[position], outputsTypes[position])
    }

    private fun parse(types: String?): ArrayList<String> {
        if (types == null)
            return ArrayList()

        return ParseHelper().parse(types, "|", "")
    }

    private fun setTemporaryColorToView(view: View) {
        handler.post {
            view.setBackgroundColor(ContextCompat.getColor(parent.context, R.color.colorPrimaryLighten))
            sound.start()
        }
        handler.postDelayed( {
            view.setBackgroundColor(ContextCompat.getColor(parent.context, R.color.transparent))
        }, 5000)
    }

    private fun findDifferenceAndUpdate(list: ArrayList<String>, parsed: ArrayList<String>, type: Type) {
        if (list.size == 0) {
            list.addAll(parsed)
        } else {
            for (i in 0 until list.size) {
                if (list[i] != parsed[i]) {
                    list[i] = parsed[i]
                    val row = layoutManager.findViewByPosition(i)
                    if (type == Type.Input) {
                        row?.input?.let { setTemporaryColorToView(it) }

                    } else if (type == Type.Output) {
                        row?.output?.let { setTemporaryColorToView(it) }
                    }
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    fun receiveUpdate(parsed: ArrayList<String>) {
        if (parsed[5].toUpperCase() == "I") {
            findDifferenceAndUpdate(inputsList, parse(parsed[6]), Type.Input)
            handler.post {
                notifyDataSetChanged()
            }
        } else if (parsed[5].toUpperCase() == "O") {
            findDifferenceAndUpdate(outputsList, parse(parsed[6]), Type.Output)
            handler.post {
                notifyDataSetChanged()
            }
        }
    }

    inner class ModuleViewHolder(private val view: View): RecyclerView.ViewHolder(view) {

        @SuppressLint("SetTextI18n")
        fun bind(position: Int, inputValue: String, outputValue: String, inputType: String, outputType: String) {
            view.row_index.text = "${position + 1}:"
            view.input.text = inputValue
            view.output.text = outputValue
        }
    }
}