fun String.plus() : String {
    if (this == "") {
        return "done"
    }
    return +""
}

fun box() : String = if (+"11" == "done") "OK" else "FAIL"