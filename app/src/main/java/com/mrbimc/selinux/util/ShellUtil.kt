package com.mrbimc.selinux.util

import eu.chainfire.libsuperuser.Shell
import java8.util.concurrent.CompletableFuture

/**
 * Created by Pavel Sikun on 23.07.17.
 */

enum class SELinuxState(val value: Int) { PERMISSIVE(0), ENFORCING(1), NOROOT(3), UNSWITCHABLE(4) }

private val shell by lazy { Shell.Builder()
        .setShell("su")
        .open()
}

fun executeGetSELinuxState(): CompletableFuture<SELinuxState> {
    val future = CompletableFuture<SELinuxState>()

    if (Shell.SU.available()) {
        shell.addCommand("/system/bin/getenforce", 1) { _, _, output: MutableList<String>? ->
            val outputString = output?.joinToString() ?: "enforcing"
            val isEnforcing = outputString.toLowerCase().contains("enforcing")
            future.complete(if (isEnforcing) SELinuxState.ENFORCING else SELinuxState.PERMISSIVE)
        }
    }
    else {
        future.complete(SELinuxState.NOROOT)
    }

    return future
}

fun executeSetSELinuxState(state: SELinuxState, command: String): CompletableFuture<Pair<SELinuxState, SELinuxState>> {
    val future = CompletableFuture<Pair<SELinuxState, SELinuxState>>() //returning (RealState, DesiredState)

    shell.addCommand(command, 1) { _, _, _: MutableList<String>? ->
        executeGetSELinuxState().whenCompleteAsync { systemState, _ ->
            val resultState = if (systemState == state) systemState else SELinuxState.UNSWITCHABLE
            future.complete(resultState to state)
        }
    }

    return future
}