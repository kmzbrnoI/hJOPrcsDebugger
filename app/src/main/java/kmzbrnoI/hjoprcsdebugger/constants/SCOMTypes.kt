package kmzbrnoI.hjoprcsdebugger.constants

val SCOMTypes = mapOf(
    0 to "Stůj/ Posun zakázán",
    1 to "Volno",
    2 to "Výstraha",
    3 to "Očekávejte 40 km/h",
    4 to "40 km/h a volno",
    5 to "Svítí vše (rezerva)",
    6 to "40 km/h a výstraha",
    7 to "40 km/h a očekávejte 40 km/h",
    8 to "Přivolávací návěst",
    9 to "Dovolen zajištěný posun",
    10 to "Dovolen nezajištěný posun",
    11 to "Opakování návěsti volno",
    12 to "Opakování návěsti výstraha",
    13 to "Návěstidlo zhaslé",
    14 to "Opak.návěsti očekávejte 40km/h",
    15 to "Opak.návěsti očekávejte 40km/h"
)

fun getSCOMTypesStrings(): Array<CharSequence> {
    val result = ArrayList<String>()
    for (element in SCOMTypes) {
        result.add(element.key.toString() + ": " + element.value)
    }

    return result.toTypedArray()
}