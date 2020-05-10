package kmzbrnoI.hjoprcsdebugger.ui.serverConnector

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.constants.*
import kmzbrnoI.hjoprcsdebugger.responses.LoginDialogResponse
import kmzbrnoI.hjoprcsdebugger.responses.TCPClientResponse
import kmzbrnoI.hjoprcsdebugger.models.Server
import kmzbrnoI.hjoprcsdebugger.network.TCPClientApplication
import kmzbrnoI.hjoprcsdebugger.storage.ServerDb
import kmzbrnoI.hjoprcsdebugger.ui.modulesList.ModulesListActivity
import kotlinx.android.synthetic.main.server_connector.*
import kotlinx.android.synthetic.main.server_connector.view.*
import java.util.*

class ServerConnector : Fragment(), TCPClientResponse,
    LoginDialogResponse {
    private var arrayList: ArrayList<String> = ArrayList()
    lateinit var mAdapter: ArrayAdapter<String>
    private var handler: Handler = Handler()

    val serverSupportedVersions = arrayOf("1.0", "1.1")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true

        val view = inflater.inflate(R.layout.server_connector, container, false).apply {
            server_loadBar.visibility = View.VISIBLE
            mAdapter = ArrayAdapter(
                context,
                android.R.layout.simple_list_item_1, android.R.id.text1, arrayList
            )
            connector_list.adapter = mAdapter
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        start()
    }

    private fun start() {
        val extras = activity?.intent?.extras
        val tcp = TCPClientApplication.getInstance()
        tcp.delegateTCPResponse = this
        val server: Server

        if (tcp.connected())
            tcp.disconnect()

        if (extras != null) {
            val type = extras.getString("serverType")
            val id = extras.getInt("serverId")
            if (type == "stored")
                server = ServerDb.getInstance().stored.get(id)
            else
                server = ServerDb.getInstance().found.get(id)

            try {
                Thread { tcp.connect(server) }.start()
            } catch (e: Exception) {
                Log.e("TCP", "Connecting", e)
                arrayList.add(e.toString())
                mAdapter.notifyDataSetChanged()
            }

        } else {
            activity?.finish()
        }

        arrayList.clear()
        arrayList.add(getString(R.string.sc_connecting))
        mAdapter.notifyDataSetChanged()
    }

    fun editLogin(message: String) {
        handler.post {
            val dialog = LoginDialog(message)
            dialog.delegate = this
            val transition = fragmentManager?.beginTransaction()

            if (transition != null) {
                dialog.show(transition, LoginDialog.TAG)
            }
        }
    }

    private fun onReceiveModules(modules: ArrayList<String>) {
        arrayList.add(getString(R.string.sc_done))
        handler.post {
            mAdapter.notifyDataSetChanged()
            server_loadBar.visibility = View.GONE
        }

        val intent = Intent(context, ModulesListActivity::class.java)
        intent.putExtra("modules", modules[3])
        startActivity(intent)
    }

    private fun onHandShake(parsed: ArrayList<String>) {
       if (parsed.size < 3 || !listOf(*serverSupportedVersions).contains(parsed[2])) {
            arrayList.add(getString(R.string.sc_version_warning))
        } else {
            arrayList.add(getString(R.string.sc_connection_ok))
       }

        handler.post {
            mAdapter.notifyDataSetChanged()
        }

        if (TCPClientApplication.getInstance().server!!.username.isEmpty() || TCPClientApplication.getInstance().server!!.password.isEmpty()) {
            arrayList.add(getString(R.string.sc_auth_wait))
            handler.post {
                server_loadBar.visibility = View.GONE
            }
            editLogin(getString(R.string.login_enter))
        } else {
            arrayList.add(getString(R.string.sc_authorizing))
            TCPClientApplication
                .getInstance()
                .send("-;RCSd;AUTH;{" + TCPClientApplication.getInstance().server!!.username + "};" + TCPClientApplication.getInstance().server!!.password)
            handler.post {
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun onGlobalAuth(parsed: ArrayList<String>) {
        if (parsed[3].toUpperCase() == "OK") {
            arrayList.add(getString(R.string.sc_auth_ok))
            arrayList.add(getString(R.string.sc_getting_ors))
            TCPClientApplication.getInstance().send("-;RCSd;LIST;")
        } else {
            arrayList.add(getString(R.string.sc_auth_err))
            if (parsed.size >= 5)
                arrayList.add(parsed.get(4))
            handler.post {
                server_loadBar.visibility = View.GONE
            }
            if (parsed.size >= 5)
                editLogin(parsed.get(4))
            else
                editLogin(getString(R.string.sc_auth_err))
        }
        handler.post {
            mAdapter.notifyDataSetChanged()
        }
    }

    private fun onConnectionEstablished() {
        TCPClientApplication.getInstance().send("-;HELLO;1.1")
    }

    private fun onDisconnect() {
        arrayList.add(getString(R.string.disconnected))
        handler.post {
            server_loadBar.visibility = View.GONE
            mAdapter.notifyDataSetChanged()
        }
    }

    override fun response(output: Int) {
        when (output) {
            CONNECTION_ESTABLISHED -> {
                onConnectionEstablished()
            }
        }
    }

    override fun response(output: Int, message: String) {
        when (output) {
            TCP_DISCONNECT -> {
                onDisconnect()
            }
        }
    }

    override fun response(output: Int, parsed: ArrayList<String>) {
        when (output) {
            HAND_SHAKE -> {
                onHandShake(parsed)
            }
            ON_RECEIVE_MODULES -> {
                onReceiveModules(parsed)
            }
            GLOBAL_AUTH -> {
                onGlobalAuth(parsed)
            }
        }
    }


    override fun onLoginClicked() {
        handler.post {
            arrayList.add(getString(R.string.sc_authorizing))
            mAdapter.notifyDataSetChanged()
            server_loadBar.visibility = View.VISIBLE
        }
    }
}