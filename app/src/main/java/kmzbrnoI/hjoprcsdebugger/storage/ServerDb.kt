package kmzbrnoI.hjoprcsdebugger.storage

import android.content.SharedPreferences
import android.util.Log
import kmzbrnoI.hjoprcsdebugger.constants.STORED_SERVERS_RELOAD
import kmzbrnoI.hjoprcsdebugger.models.Server
import kmzbrnoI.hjoprcsdebugger.responses.ServerDbResponse
import java.util.ArrayList

class ServerDb(internal var preferences: SharedPreferences?) {
    var delegate: ServerDbResponse? = null

    var found: ArrayList<Server> = ArrayList()
    var stored: ArrayList<Server> = ArrayList()

    companion object {
        private var instance: ServerDb? = null

        fun getInstance(preferences: SharedPreferences? = null): ServerDb {
            if (instance == null) {
                instance = ServerDb(preferences)
            }

            return instance as ServerDb
        }
    }

    init {
        this.loadServers()
    }

    fun loadServers() {
        if (!preferences?.contains("StoredServers")!!) return
        val serverString = preferences?.getString("StoredServers", "")!!.split("\\|".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()

        for (tmpS in serverString) {
            try {
                val attributes =
                    tmpS.split(";".toRegex()).toTypedArray()
                if (attributes.size > 5) {
                    val tmpServer = Server(
                        attributes[0], attributes[1], Integer.parseInt(attributes[2]), false,
                        attributes[3], attributes[4], attributes[5]
                    )
                    tmpServer.active = true
                    if (!stored.contains(tmpServer)) stored.add(tmpServer)
                }
            } catch (e: Exception) {
                Log.e("ServerDb", "loadServers: " + e.message)
            }

        }
    }

    private fun saveServers() {
        var saveString = ""
        for (s in this.stored)
            saveString = saveString + s.getSaveDataString() + "|"

        val editor = preferences?.edit()
        editor?.remove("StoredServers")
        editor?.clear()
        editor?.putString("StoredServers", saveString)
        editor?.apply()
    }

    fun addStoredServer(server: Server) {
        this.stored.add(server)
        this.saveServers()
    }

    fun removeStoredServer(position: Int) {
        if (position <= stored.size) {
            for (s in found) {
                if (s.host == stored[position].host && s.port == stored[position].port) {
                    s.username = ""
                    s.password = ""
                }
            }

            this.stored.removeAt(position)
            this.saveServers()

            delegate?.response(STORED_SERVERS_RELOAD)
        }
    }

    fun addFoundServer(server: Server) {
        this.found.add(server)

        // transfer password from stored servers
        if (server.username.isEmpty() && server.password.isEmpty()) {
            for (s in stored) {
                if (server.host.equals(s.host) && server.port == s.port) {
                    server.username = s.username
                    server.password = s.password
                    break
                }
            }
        }
    }

    fun isFoundServer(host: String, port: Int): Boolean {
        for (s in found)
            if (s.host.equals(host) && s.port == port)
                return true
        return false
    }

    fun isStoredServer(host: String, port: Int): Boolean {
        for (s in stored)
            if (s.host.equals(host) && s.port == port)
                return true
        return false
    }

    fun transferLoginToSaved(found: Server) {
        for (s in stored) {
            if (s !== found && s.host.equals(found.host) && s.port == found.port) {
                s.username = found.username
                s.password = found.password
            }
        }
        this.saveServers()
    }
}