package kmzbrnoI.hjoprcsdebugger.responses

interface TCPClientResponse {
    fun response(output: Int)

    fun response(output: Int, message: String)

    fun response(output: Int, parsed: ArrayList<String>)
}