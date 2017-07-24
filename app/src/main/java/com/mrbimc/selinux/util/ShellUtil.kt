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

    shell.addCommand("/system/bin/getenforce", 1) { _, _, output: MutableList<String> ->
        val outputString = output.joinToString()
        val isEnforcing = outputString.toLowerCase().contains("enforcing")
        future.complete(if (isEnforcing) SELinuxState.ENFORCING else SELinuxState.PERMISSIVE)
    }

    return future
}

fun executeSetSELinuxState(state: SELinuxState, command: String): CompletableFuture<SELinuxState> {
    val future = CompletableFuture<SELinuxState>()

    shell.addCommand(command, 1) { _, _, output: MutableList<String> ->
        val outputString = output.joinToString()
        val isError = outputString.trim().isNotEmpty()
        future.complete(if (isError) SELinuxState.UNKNOWN else state)
    }

    return future
}