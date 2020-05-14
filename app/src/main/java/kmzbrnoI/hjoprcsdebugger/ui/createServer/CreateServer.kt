package kmzbrnoI.hjoprcsdebugger.ui.createServer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kmzbrnoI.hjoprcsdebugger.R

class CreateServer : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true

        return inflater.inflate(R.layout.new_server, container, false).apply {

            /*saveButton.setOnClickListener {
                val name = nameText.text.toString()
                val port = portText.text.toString()
                val ipAdr = ipText.text.toString()
                val about = aboutText.text.toString()

                if (name == "" || port == "" || ipAdr == "") {
                    AlertDialog.Builder(context)
                        .setMessage(R.string.ns_warning_compulsory)
                        .setCancelable(false)
                        .setPositiveButton("ok"
                        ) { _, _ -> }
                        .setCancelable(false)
                        .show()
                } else if (name.contains("--")) {
                    AlertDialog.Builder(context)
                        .setMessage(R.string.ns_warning_invalid_characters)
                        .setCancelable(false)
                        .setPositiveButton("ok"
                        ) { _, _ -> }
                        .setCancelable(false)
                        .show()
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

                    activity?.setResult(Activity.RESULT_OK, Intent())
                    activity?.finish()
                }
            }

            backButton.setOnClickListener {
                activity?.setResult(Activity.RESULT_CANCELED, Intent())
                activity?.finish()
            }*/
        }
    }
}