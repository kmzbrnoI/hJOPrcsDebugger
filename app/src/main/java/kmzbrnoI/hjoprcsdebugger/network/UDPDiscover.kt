package kmzbrnoI.hjoprcsdebugger.network

import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.util.Log
import kmzbrnoI.hjoprcsdebugger.constants.FOUND_SERVERS_RELOAD
import kmzbrnoI.hjoprcsdebugger.helpers.UDPDiscoverResponse
import kmzbrnoI.hjoprcsdebugger.helpers.ParseHelper
import kmzbrnoI.hjoprcsdebugger.models.Server
import kmzbrnoI.hjoprcsdebugger.storage.ServerDb
import java.io.IOException
import java.net.*
import java.util.*

class UDPDiscover(internal var mWifi: WifiManager) : AsyncTask<String, Server, String>() {
    var delegate: UDPDiscoverResponse? = null
    val DEFAULT_PORT = 5880
    private val TIMEOUT_MS = 800

    @Throws(IOException::class)
    internal fun getBroadcastAddress(): InetAddress? {
        val dhcp = mWifi.getDhcpInfo() ?: return null

        val broadcast = dhcp.ipAddress and dhcp.netmask or dhcp.netmask.inv()
        val quads = ByteArray(4)
        for (k in 0..3)
            quads[k] = (broadcast shr k * 8 and 0xFF).toByte()

        return InetAddress.getByAddress(quads)
    }

    @Throws(IOException::class)
    private fun listenForResponses(socket: DatagramSocket) {
        val buf = ByteArray(1024)

        // Loop and try to receive responses until the timeout elapses.
        try {
            while (true) {
                val packet = DatagramPacket(buf, buf.size)
                socket.receive(packet)
                val s = String(packet.data, 0, packet.length)

                val server = parseServerMessage(s)
                if (server != null) {
                    this.publishProgress(server)
                }
            }
        } catch (e: SocketTimeoutException) {
            Log.i("listening exception", "S: time out '$e'") // timeout OK
        }

    }

    private fun parseServerMessage(message: String): Server? {
        //"hJOP";verze_protokolu;typ_zarizeni;server_nazev;server_ip;server_port;
        //server_status;server_popis
        val parsed = ParseHelper().parse(message, ";", "")

        return if (parsed.size >= 8 && parsed.get(0) == "hJOP" && parsed.get(2) == "server")
            Server(message)
        else
            null
    }

    private fun getIPAddress(useIPv4: Boolean): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress
                        val isIPv4 = sAddr.indexOf(':') < 0

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr
                        } else {
                            if (!isIPv4) {
                                val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                return if (delim < 0) sAddr.toUpperCase() else sAddr.substring(
                                    0,
                                    delim
                                ).toUpperCase()
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
        }
        // for now eat exceptions
        return ""
    }

    override fun doInBackground(vararg urls: String): String? {
        try {
            val sock = DatagramSocket(null)
            sock.reuseAddress = true
            sock.bind(InetSocketAddress(DEFAULT_PORT))

            sock.broadcast = true
            sock.soTimeout = TIMEOUT_MS

            val message = "hJOP;1.0;regulator;mobileManager;" + this.getIPAddress(true) + ";\n"
            val packet = DatagramPacket(
                message.toByteArray(), message.length,
                getBroadcastAddress(), DEFAULT_PORT
            )
            sock.send(packet)

            listenForResponses(sock)
            sock.close()
        } catch (e: Exception) {
            Log.e("exception", "S: Received Message: '$e'")
        }

        return null
    }

    override fun onProgressUpdate(vararg progress: Server) {
        ServerDb.instance?.let { instance ->
            progress[0].host?.let { host ->
                if (!instance.isFoundServer(host, progress[0].port)) {
                    ServerDb.instance?.addFoundServer(progress[0])
                    delegate?.discoveringFinished(FOUND_SERVERS_RELOAD)
                }
            }

        }
    }
}