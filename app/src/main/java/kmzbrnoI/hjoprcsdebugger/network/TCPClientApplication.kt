package kmzbrnoI.hjoprcsdebugger.network

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.os.Handler
import android.util.Log
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.constants.*
import kmzbrnoI.hjoprcsdebugger.helpers.ParseHelper
import kmzbrnoI.hjoprcsdebugger.models.Module
import kmzbrnoI.hjoprcsdebugger.responses.TCPClientResponse
import kmzbrnoI.hjoprcsdebugger.models.Server
import kmzbrnoI.hjoprcsdebugger.responses.ModuleResponse
import java.net.ConnectException

@SuppressLint("Registered")
class TCPClientApplication: Application(), TCPClient.OnMessageReceivedListener {
    var delegateTCPResponse: TCPClientResponse? = null
    var delegateModuleResponse: ModuleResponse? = null

    var server: Server? = null
    var module: Module? = null

    var activityHandler: Handler = Handler()
    var activityContext: Context? = null

    internal var mTcpClient: TCPClient? = null

    companion object {
        private var instance: TCPClientApplication? = null

        fun getInstance(): TCPClientApplication {
            if (instance == null) {
                instance = TCPClientApplication()
            }

            return instance as TCPClientApplication
        }
    }

    fun connect(server: Server) {
        if (mTcpClient != null && mTcpClient!!.connected())
            mTcpClient!!.disconnect()

        this.server = server
        mTcpClient = TCPClient(server.host, server.port, delegateTCPResponse)

        mTcpClient!!.listen(this)
    }

    fun disconnect() {
        this.server = null

        if (mTcpClient != null)
            this.mTcpClient!!.disconnect()
    }

    fun loadModules() {
        if (server != null) {
            send("-;RCSd;LIST;")
        }
    }

    fun changeOutput(port: Int, state: String) {
        if (server != null && this.module != null) {
            send("-;RCSd;SETOUT;" + this.module?.address + ";" + port + ";" + state)
            send("-;RCSd;UPDATE;" + this.module?.address)
        }
    }

    fun connectToModule(module: Module) {
        if (server != null && this.module != null)
            disconnectModule()

        this.module = module
        send("-;RCSd;PLEASE;" + module.address)
    }

    fun disconnectModule() {
        if (this.module != null) {
            send("-;RCSd;RELEASE;" + this.module?.address)
            this.module = null
        }
    }

    fun send(message: String) {
        if (mTcpClient == null) return

        try {
            mTcpClient!!.send(message)
        } catch (e: ConnectException) {
            Log.e("TCP", "Cannot send data, disconnecting", e)
            this.disconnect()
        }
    }

    fun connected(): Boolean {
        return mTcpClient != null && mTcpClient!!.connected()
    }

    override fun onMessageReceived(message: String) {
        val parsed = ParseHelper().parse(message, ";", "")

        if (parsed.size < 2 || parsed.get(0) != "-") return
        parsed[1] = parsed[1].toUpperCase()

        if (parsed[1] == "HELLO") {
            delegateTCPResponse?.response(HAND_SHAKE, parsed)

        } else if (parsed[2] == "INFO") {
            delegateTCPResponse?.response(ON_RECEIVE_MODULES, parsed)
            delegateModuleResponse?.response(ON_RECEIVE_MODULES, parsed)

        } else if (parsed[1] == "PING" && parsed.size > 2 && parsed[2].toUpperCase() == "REQ-RESP") {
            if (parsed.size >= 4) {
                this.send("-;PONG;" + parsed[3] + '\n'.toString())
            } else {
                this.send("-;PONG\n")
            }

        } else if (parsed[2] == "AUTH") {
            if (parsed.size < 3) return
            delegateTCPResponse?.response(GLOBAL_AUTH, parsed)

        } else if (parsed[2] == "MODULE") {
            delegateModuleResponse?.response(parsed)
        } else if (parsed[2] == "ERR") {
            activityHandler.post {
                activityContext?.let { context ->
                    AlertDialog.Builder(context)
                        .setMessage(parsed[3])
                        .setPositiveButton(context.getString(R.string.ok), null)
                        .setCancelable(false)
                        .show()
                }
            }
        }
    }
}