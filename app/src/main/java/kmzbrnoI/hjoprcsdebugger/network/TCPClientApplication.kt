package kmzbrnoI.hjoprcsdebugger.network

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import kmzbrnoI.hjoprcsdebugger.constants.*
import kmzbrnoI.hjoprcsdebugger.helpers.ParseHelper
import kmzbrnoI.hjoprcsdebugger.helpers.TCPClientResponse
import kmzbrnoI.hjoprcsdebugger.models.Server
import java.net.ConnectException

@SuppressLint("Registered")
class TCPClientApplication: Application(), TCPClient.OnMessageReceivedListener {
    var delegate: TCPClientResponse? = null

    var server: Server? = null

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
        mTcpClient = TCPClient(server.host, server.port, delegate)

        mTcpClient!!.listen(this)
    }

    fun disconnect() {
        this.server = null

        if (mTcpClient != null)
            this.mTcpClient!!.disconnect()
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
        parsed.set(1, parsed.get(1).toUpperCase())

        if (parsed.get(1) == "HELLO") {
            delegate?.response(HAND_SHAKE, parsed)

        } else if (parsed.get(1) == "OR-LIST") {
            delegate?.response(AREAS, parsed)

        } else if (parsed.get(1) == "PING" && parsed.size > 2 && parsed.get(2).toUpperCase() == "REQ-RESP") {
            if (parsed.size >= 4) {
                this.send("-;PONG;" + parsed.get(3) + '\n'.toString())
            } else {
                this.send("-;PONG\n")
            }

        } else if (parsed.get(1) == "LOK") {
            if (parsed.size < 3) return
            if (parsed.get(2) == "G") {
                if (parsed.get(3).toUpperCase() == "AUTH"){
                    delegate?.response(GLOBAL_AUTH, parsed)
                }
            }
        }
    }
}