package org.ale.pallota.distributed.systems.algorithms.mutex

import org.ale.pallotta.mutex.MutexMessage
import org.ale.pallotta.mutex.Type

fun createMutexMessage(type: Type, id: Int): MutexMessage = MutexMessage
    .newBuilder()
    .setType(type)
    .setId(id)
    .build()
