package kmzbrnoI.hjoprcsdebugger.helpers

import java.util.ArrayList

class ParseHelper {
    fun parse(text: String, separators: String, ignore: String): ArrayList<String> {
        val result = ArrayList<String>()
        var s = ""
        var plain_cnt = 0
        if (text == "") return ArrayList()

        for (i in 0 until text.length) {
            if (text[i] == '{') {
                if (plain_cnt > 0) s = s + text[i]
                plain_cnt++
            } else if (text[i] == '}' && plain_cnt > 0) {
                plain_cnt--
                if (plain_cnt > 0) s = s + text[i]
            } else if (separators.indexOf(text[i]) != -1 && plain_cnt == 0) {
                result.add(s)
                s = ""
            } else if (ignore.indexOf(text[i]) == -1 || plain_cnt > 0) {
                s = s + text[i]
            }
        }

        if (s !== "") result.add(s)
        return result
    }
}