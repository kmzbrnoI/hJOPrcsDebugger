package kmzbrnoI.hjoprcsdebugger.ui.serverConnector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.helpers.HashHelper
import kmzbrnoI.hjoprcsdebugger.responses.LoginDialogResponse
import kmzbrnoI.hjoprcsdebugger.network.TCPClientApplication
import kmzbrnoI.hjoprcsdebugger.storage.ServerDb
import kmzbrnoI.hjoprcsdebugger.databinding.UserLoginBinding

class LoginDialog(private var message: String) : DialogFragment() {
    var delegate: LoginDialogResponse? = null
    private var _binding: UserLoginBinding? = null
    val binding get() = _binding!!

    companion object {
        const val TAG = "LoginDialogTag"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true

        _binding = UserLoginBinding.inflate(inflater, container, false)
        binding.apply {
            //set dialog component
            tvNote.text = message

            dialogName.setText(TCPClientApplication.getInstance().server?.username)
            dialogPasswd.setText("")

            // if button is clicked, close the custom dialog
            dialogButtonOK.setOnClickListener {
                TCPClientApplication.getInstance().server?.username = dialogName.text.toString()
                TCPClientApplication.getInstance().server?.password =
                    HashHelper().hashPassword(dialogPasswd.text.toString())

                if (dialogSaveData.isChecked) {
                    if (ServerDb.getInstance().isStoredServer(
                            TCPClientApplication.getInstance().server!!.host,
                            TCPClientApplication.getInstance().server!!.port
                        )
                    ) {
                        TCPClientApplication.getInstance().server?.let { it1 ->
                            ServerDb.getInstance().transferLoginToSaved(
                                it1
                            )
                        }
                    } else {
                        TCPClientApplication.getInstance().server?.let { it1 ->
                            ServerDb.getInstance().addStoredServer(
                                it1
                            )
                        }
                    }

                }

                TCPClientApplication.getInstance().send(
                    "-;RCSd;AUTH;{" +
                            TCPClientApplication.getInstance().server?.username + "};" +
                            TCPClientApplication.getInstance().server?.password
                )

                delegate?.onLoginClicked()

                dismiss()
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}