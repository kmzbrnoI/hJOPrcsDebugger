package kmzbrnoI.hjoprcsdebugger.models

import kmzbrnoI.hjoprcsdebugger.helpers.ParseHelper

class Module {
    var address: String = ""
    var name: String = ""
    var type: String = ""
    var status: String = ""
    var firmware: String = ""
    var inputTypes: String = ""
    var outputTypes: String = ""

    constructor(
        address: String,
        name: String,
        type: String,
        status: String,
        firmware: String,
        inputTypes: String,
        outputTypes: String
    ) {
        this.address = address
        this.name = name
        this.type = type
        this.status = status
        this.firmware = firmware
        this.inputTypes = inputTypes
        this.outputTypes = outputTypes
    }

    // Create server from discovery packet.
    // @param disovery in format:
    //   adresa|nazev|typ|status|firmware|inputTypes|outputTypes
    constructor(discovery: String) {
        val parsed = ParseHelper().parse(discovery, "|", "")

        this.address = parsed[0]
        this.name = parsed[1]
        this.type = parsed[2]
        this.status = parsed[3]
        this.firmware = parsed[4]
        this.inputTypes = parsed[5]
        this.outputTypes = parsed[6]
    }

    override fun toString(): String {
        return this.address.toString() + ": " + this.name
    }
}