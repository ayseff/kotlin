//KT-2164 !! does not propagate nullability information
package kt2164

fun foo(x: Int): Int = x + 1

fun main(args : Array<String>) {
    val x: Int? = null

    foo(<!TYPE_MISMATCH!>x<!>)

    if (x != null) {
        foo(<!DEBUG_INFO_AUTOCAST!>x<!>)
        foo(x<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>)
        foo(<!DEBUG_INFO_AUTOCAST!>x<!>)
    }

    foo(<!TYPE_MISMATCH!>x<!>)

    if (x != null) {
        foo(<!DEBUG_INFO_AUTOCAST!>x<!>)
        foo(x<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>)
        foo(<!DEBUG_INFO_AUTOCAST!>x<!>)
    } else {
        foo(<!TYPE_MISMATCH!>x<!>)
        foo(x!!)
        foo(<!DEBUG_INFO_AUTOCAST!>x<!>)
    }

    foo(<!DEBUG_INFO_AUTOCAST!>x<!>)
    foo(x<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>)
    foo(<!DEBUG_INFO_AUTOCAST!>x<!>)
    
    val y: Int? = null
    y!!
    y<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>
    foo(<!DEBUG_INFO_AUTOCAST!>y<!>)
    foo(y<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>)
}
