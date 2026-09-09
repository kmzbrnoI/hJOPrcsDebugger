package kmzbrnoI.hjoprcsdebugger.network

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
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
class TCPClientApplication: Application(), TCPClient.Events {
    var delegateTCPResponse: TCPClientResponse? = null
    var delegateModuleResponse: ModuleResponse? = null

    var server: Server? = null
    var module: Module? = null

    var activityHandler: Handler = Handler(Looper.getMainLooper())
    @SuppressLint("StaticFieldLeak")
    var activityContext: Context? = null

    internal var mTcpClient: TCPClient? = null

    companion object {
        @Volatile
        private var instance: TCPClientApplication? = null

        fun getInstance(): TCPClientApplication {
            return instance ?: synchronized(this) {
                instance ?: TCPClientApplication().also { instance = it }
            }
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
        if (!mTcpClient!!.connected()) return

        try {
            mTcpClient!!.send(message)
        } catch (e: ConnectException) {
            Log.e("TCP", "Cannot send data, disconnecting", e)
            this.disconnect()
        }
    }

    fun connected(): Boolean {
        return (mTcpClient?.connected()) ?: false
    }

    private fun showSafeDialog(message: String) {
        activityHandler.post {
            val context = activityContext
            if (context is Activity && (context.isFinishing || context.isDestroyed)) {
                return@post
            }
            context?.let { ctx ->
                AlertDialog.Builder(ctx)
                    .setMessage(message)
                    .setPositiveButton(ctx.getString(R.string.ok), null)
                    .setCancelable(false)
                    .show()
            }
        }
    }

    override fun onDisconnected() {
        showSafeDialog(activityContext?.getString(R.string.disconnected) ?: "Disconnected")
    }

    override fun onMessageReceived(message: String) {
        val parsed = ParseHelper().parse(message, ";", "")

        if (parsed.size < 2 || parsed[0] != "-") return
        parsed[1] = parsed[1].uppercase()

        if (parsed[1] == "HELLO") {
            delegateTCPResponse?.response(HAND_SHAKE, parsed)

        } else if (parsed[2] == "INFO") {
            delegateTCPResponse?.response(ON_RECEIVE_MODULES, parsed)
            delegateModuleResponse?.response(ON_RECEIVE_MODULES, parsed)

        } else if (parsed[1] == "PING" && parsed[2].uppercase() == "REQ-RESP") {
            if (parsed.size >= 4) {
                this.send("-;PONG;" + parsed[3] + '\n'.toString())
            } else {
                this.send("-;PONG\n")
            }

        } else if (parsed[2] == "AUTH") {
            delegateTCPResponse?.response(GLOBAL_AUTH, parsed)

        } else if (parsed[2] == "MODULE") {
            delegateModuleResponse?.response(parsed)
        } else if (parsed[2] == "ERR") {
            if (parsed.size > 3) {
                showSafeDialog(parsed[3])
            }
        }
    }
}