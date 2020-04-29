package kmzbrnoI.hjoprcsdebugger.helpers

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class HashHelper {
    fun hashPassword(password: String): String {
        var md: MessageDigest? = null
        try {
            md = MessageDigest.getInstance("SHA-256")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        md!!.update(password.toByteArray())
        val byteData = md.digest()

        val sb = StringBuilder()
        for (aByteData in byteData) {
            sb.append(Integer.toString((aByteData.toInt() and 0xff) + 0x100, 16).substring(1))
        }

        return sb.toString()
    }

}
