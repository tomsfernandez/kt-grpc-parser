package testing

external fun require(name: String): dynamic
external val __dirname: dynamic

val fs = require("fs")
val jsPath = require("path");

fun loadResourceText(path: String): dynamic {
    val finalPath = jsPath.join(
        __dirname,
        "../../../..",
        "processedResources",
        "js",
        "test",
        path
    )
    return fs.readFileSync(finalPath, "utf8")
}

actual fun readResource(path: String): String {
    return (loadResourceText(path) as String).trim()
}
