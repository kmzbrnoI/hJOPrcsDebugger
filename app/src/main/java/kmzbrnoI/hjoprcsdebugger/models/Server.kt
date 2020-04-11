package kmzbrnoI.hjoprcsdebugger.models

import kmzbrnoI.hjoprcsdebugger.helpers.ParseHelper

class Server {
    var name: String = ""
    var host: String? = null
    var port: Int = 0
    var type: String = ""
    var active: Boolean = false

    var username: String = ""
    var password: String = ""

    constructor(
        name: String,
        host: String,
        port: Int,
        active: Boolean,
        type: String,
        username: String,
        password: String
    ) {
        this.name = name
        this.host = host
        this.port = port
        this.type = type
        this.active = active
        this.username = username
        this.password = password
    }

    // Create server from discovery packet.
    // @param disovery in format:
    //   "hJOP";verze_protokolu;typ_zarizeni;server_nazev;server_ip;server_port;server_status;server_popis
    constructor(discovery: String) {
        val parsed = ParseHelper().parse(discovery, ";", "")

        this.name = parsed.get(7)
        this.host = parsed.get(4)
        this.port = Integer.valueOf(parsed.get(5))
        this.type = parsed.get(3)
        this.active = parsed.get(6) == "on"
        this.username = ""
        this.password = ""
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val server = o as Server?

        if (port != server!!.port) return false
        if (name != server.name) return false
        return if (host != null) host == server.host else server.host == null
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + if (host != null) host!!.hashCode() else 0
        result = 31 * result + port
        return result
    }
}