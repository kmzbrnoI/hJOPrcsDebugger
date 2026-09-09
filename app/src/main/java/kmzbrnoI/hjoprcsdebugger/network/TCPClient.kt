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
        disconnect(waitRead = true, waitWrite = true)
    }

    fun disconnect(waitRead: Boolean, waitWrite: Boolean) {
        mRun = false
        if (socket != null) {
            try {
                socket!!.close()

                if (waitWrite) {
                    wt!!.interrupt()
                    try {
                        wt!!.join()
                    } catch (_: InterruptedException) {
                    }

                }

                if (waitRead) {
                    rt!!.interrupt()
                    try {
                        rt!!.join()
                    } catch (_: InterruptedException) {
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

    fun listen(events: Events) {
        mRun = true
        if (socket != null) return
        val st = SocketThread(events)
        Thread{ st.start() }.start()
    }

    inner class SocketThread internal constructor(
        internal var m_events: Events
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

            wt = WriteThread(socket!!, m_events)
            Thread{ wt!!.start() }.start()
            rt = ReadThread(socket!!, m_events)
            Thread{ rt!!.start() }.start()

            delegate?.response(CONNECTION_ESTABLISHED)
        }
    }

    interface Events {
        fun onMessageReceived(message: String)
        fun onDisconnected()
    }

    inner class WriteThread internal constructor(
        private val m_socket: Socket,
        private val m_events: Events
    ) : Thread() {
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
                m_events.onDisconnected()
                return
            }

            lock.withLock  {
                while (!isInterrupted && !m_socket.isClosed) {
                    try {
                        condition.await()
                    } catch (_: InterruptedException) {
                    }

                    for (data in m_queue) {
                        try {
                            str.write(data)

                        } catch (_: IOException) {
                            disconnect(true, false)
                            m_events.onDisconnected()
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
        private val m_events: Events
    ) : Thread() {

        override fun start() {
            val buffer = ByteArray(8192)
            var totalLen = 0
            var newLen: Int

            val str: InputStream
            try {
                str = m_socket.getInputStream()
            } catch (e: IOException) {
                Log.e("TCP", "Socket IO exception", e)
                disconnect(false, true)
                m_events.onDisconnected()
                return
            }

            while (!isInterrupted && !m_socket.isClosed) {
                try {
                    newLen = str.read(buffer, totalLen, 8192 - totalLen)
                    if (newLen == 0)
                        continue
                    else if (newLen == -1) {
                        disconnect(false, true)
                        m_events.onDisconnected()
                        return
                    }

                    totalLen += newLen

                    var last = 0
                    for (i in 0 until totalLen) {
                        if (buffer[i] == '\n'.code.toByte()) {
                            val range = buffer.copyOfRange(last, i)

                            val end = if (i > 0 && buffer[i - 1] == '\r'.code.toByte())
                                i - 1 - last
                            else
                                i - last

                            m_events.onMessageReceived(String(range, 0, end))
                            last = i + 1
                        }
                    }

                    for (i in 0 until totalLen - last)
                        buffer[i] = buffer[i + last]
                    totalLen -= last
                } catch (_: IOException) {
                    disconnect(waitRead = false, waitWrite = true)
                    m_events.onDisconnected()
                    return
                }

            }
        }
    }
}