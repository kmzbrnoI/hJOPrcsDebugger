package kmzbrnoI.hjoprcsdebugger.network

import android.util.Log
import kmzbrnoI.hjoprcsdebugger.constants.CONNECTION_ESTABLISHED
import kmzbrnoI.hjoprcsdebugger.constants.TCP_DISCONNECT
import kmzbrnoI.hjoprcsdebugger.responses.TCPClientResponse
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ConnectException
import java.net.InetAddress
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class TCPClient(ip: String, port: Int, var delegate: TCPClientResponse?) {
    var serverIp: String = ip
    var serverPort: Int = port

    private var socket: Socket? = null
    private var mRun = false

    internal var wt: WriteThread? = null
    internal var rt: ReadThread? = null

    @Throws(ConnectException::class)
    fun send(message: String) {
        if (!socket!!.isConnected)
            throw ConnectException("Not connected!")

        wt!!.send((message + '\n').toByteArray(StandardCharsets.UTF_8))
    }

    fun disconnect() {
        disconnect(true, true)
    }

    fun disconnect(wait_read: Boolean, wait_write: Boolean) {
        mRun = false
        if (socket != null) {
            try {
                socket!!.close()

                if (wait_write) {
                    wt!!.interrupt()
                    try {
                        wt!!.join()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                }

                if (wait_read) {
                    rt!!.interrupt()
                    try {
                        rt!!.join()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                }

            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                socket = null
            }
        }
        delegate?.response(TCP_DISCONNECT, "Disconnect")
    }

    fun connected(): Boolean {
        return socket != null
    }

    fun listen(listener: OnMessageReceivedListener) {
        mRun = true
        if (socket != null) return
        val st = SocketThread(listener)
        Thread{ st.start() }.start()
    }

    inner class SocketThread internal constructor(
        internal var m_listener: OnMessageReceivedListener
    ) : Thread() {
        override fun start() {
            try {
                val serverAddr = InetAddress.getByName(serverIp)
                socket = Socket(serverAddr, serverPort)

                // disable Nagle's algorithm to make connection low-latency
                socket!!.tcpNoDelay = true
            } catch (e: Exception) {
                Log.e("TCP", "Cannot connect to socket", e)
                delegate?.response(TCP_DISCONNECT, "Cannot connect to socket")
                return
            }

            if (socket == null) {
                Log.e("TCP", "Socket not initialized")
                delegate?.response(TCP_DISCONNECT, "Socket not initialized!")
                return
            }

            wt = WriteThread(socket!!)
            Thread{ wt!!.start() }.start()
            rt = ReadThread(socket!!, m_listener)
            Thread{ rt!!.start() }.start()

            delegate?.response(CONNECTION_ESTABLISHED)
        }
    }

    interface OnMessageReceivedListener {
        fun onMessageReceived(message: String)
    }

    inner class WriteThread internal constructor(private val m_socket: Socket) : Thread() {
        private val lock = ReentrantLock()
        private val condition = lock.newCondition()
        private val m_queue = ArrayList<ByteArray>()

        override fun start() {
            val str: OutputStream
            try {
                str = m_socket.getOutputStream()
            } catch (e: IOException) {
                Log.e("TCP", "Socket IO exception", e)
                disconnect(true, false)
                return
            }

            lock.withLock  {
                while (!isInterrupted && !m_socket.isClosed) {
                    try {
                        condition.await()
                    } catch (ex: InterruptedException) {
                    }

                    for (data in m_queue) {
                        try {
                            str.write(data)

                        } catch (e: IOException) {
                            disconnect(true, false)
                            return
                        }

                    }
                    m_queue.clear()
                }
            }
        }

        fun send(data: ByteArray) {
            lock.withLock {
                m_queue.add(data)
                condition.signal()
            }
        }
    }

    inner class ReadThread internal constructor(
        private val m_socket: Socket,
        private val m_listener: OnMessageReceivedListener
    ) : Thread() {

        override fun start() {
            val buffer = ByteArray(8192)
            var total_len = 0
            var new_len: Int

            val str: InputStream
            try {
                str = m_socket.getInputStream()
            } catch (e: IOException) {
                Log.e("TCP", "Socket IO exception", e)
                disconnect(false, true)
                return
            }

            while (!isInterrupted && !m_socket.isClosed) {
                try {
                    new_len = str.read(buffer, total_len, 8192 - total_len)
                    if (new_len == 0)
                        continue
                    else if (new_len == -1) {
                        disconnect(false, true)
                        return
                    }

                    total_len += new_len

                    var last = 0
                    for (i in 0 until total_len) {
                        if (buffer[i] == '\n'.toByte()) {
                            val range = Arrays.copyOfRange(buffer, last, i)

                            val end: Int
                            if (i > 0 && buffer[i - 1] == '\r'.toByte())
                                end = i - 1 - last
                            else
                                end = i - last

                            m_listener.onMessageReceived(String(range, 0, end))
                            last = i + 1
                        }
                    }

                    for (i in 0 until total_len - last)
                        buffer[i] = buffer[i + last]
                    total_len = total_len - last
                } catch (e: IOException) {
                    disconnect(false, true)
                    return
                }

            }
        }
    }
}