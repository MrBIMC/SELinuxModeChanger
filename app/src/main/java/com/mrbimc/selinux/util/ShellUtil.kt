package com.mrbimc.selinux.util

import eu.chainfire.libsuperuser.Shell
import java8.util.concurrent.CompletableFuture

/**
 * Created by Pavel Sikun on 23.07.17.
 */

enum class SELinuxState(val value: Int) { PERMISSIVE(0), ENFORCING(1), UNKNOWN(3) }

private val shell by lazy { Shell.Builder()
        .setShell("su")
        .open()
}

fun executeGetSELinuxState(): CompletableFuture<SELinuxState> {
    val future = CompletableFuture<SELinuxState>()

    if (Shell.SU.available()) {
        future.complete(if (Shell.SU.isSELinuxEnforcing()) SELinuxState.ENFORCING else SELinuxState.PERMISSIVE)
    }
    else {
        future.complete(SELinuxState.UNKNOWN)
    }

    return future
}

fun executeSetSELinuxState(state: SELinuxState, command: String): CompletableFuture<SELinuxState> {
    val future = CompletableFuture<SELinuxState>()

    if (Shell.SU.available()) {
        shell.addCommand(command, 1) { _, _, output: MutableList<String> ->
            val outputString = output.joinToString()
            val isError = outputString.trim().isNotEmpty()
            future.complete(if (isError) SELinuxState.UNKNOWN else state)
        }
    }
    else {
        future.complete(SELinuxState.UNKNOWN)
    }

    return future
}