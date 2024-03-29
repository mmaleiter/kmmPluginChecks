package com.check.kmm.pluginchecks.android.kable.check.util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren

fun CoroutineScope.childScope() =
    CoroutineScope(coroutineContext + Job(coroutineContext[Job]))

fun CoroutineScope.cancelChildren(
    cause: CancellationException? = CancellationException(),
) = coroutineContext[Job]?.cancelChildren(cause)
