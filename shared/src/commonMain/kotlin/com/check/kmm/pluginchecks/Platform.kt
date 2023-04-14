package com.check.kmm.pluginchecks

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform