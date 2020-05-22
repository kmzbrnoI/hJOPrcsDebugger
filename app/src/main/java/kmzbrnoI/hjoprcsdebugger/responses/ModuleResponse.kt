package kmzbrnoI.hjoprcsdebugger.responses

interface ModuleResponse {
    fun response(parsed: ArrayList<String>)

    fun response(output: Int, parsed: ArrayList<String>)
}