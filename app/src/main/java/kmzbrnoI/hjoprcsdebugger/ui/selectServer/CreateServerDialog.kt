package kmzbrnoI.hjoprcsdebugger.ui.selectServer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.helpers.CreateServerDialogResponse
import kmzbrnoI.hjoprcsdebugger.models.Server
import kmzbrnoI.hjoprcsdebugger.storage.ServerDb
import kotlinx.android.synthetic.main.new_server.view.*

class CreateServerDialog : DialogFragment() {
    var delegate: CreateServerDialogResponse? = null

    companion object {
        const val TAG = "CreateServerDialogTag"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true

        return inflater.inflate(R.layout.new_server, container, false).apply {

            saveButton.setOnClickListener {
                val name = nameText.text.toString()
                val port = portText.text.toString()
                val ipAdr = ipText.text.toString()
                val about = aboutText.text.toString()

                if (name == "" || port == "" || ipAdr == "") {
                    AlertDialog.Builder(context)
                        .setMessage(R.string.ns_warning_compulsory)
                        .setCancelable(false)
                        .setPositiveButton("ok"
                        ) { _, _ -> }.show()
                } else if (name.contains("--")) {
                    AlertDialog.Builder(context)
                        .setMessage(R.string.ns_warning_invalid_characters)
                        .setCancelable(false)
                        .setPositiveButton("ok"
                        ) { _, _ -> }.show()
                } else {
                    ServerDb.getInstance().addStoredServer(
                        Server(
                            name,
                            ipAdr,
                            Integer.parseInt(port),
                            false,
                            about,
                            "",
                            ""
                        )
                    )

                    delegate?.onServerCreated()
                    dismiss()
                }
            }

            backButton.setOnClickListener {
                dismiss()
            }
        }
    }
}