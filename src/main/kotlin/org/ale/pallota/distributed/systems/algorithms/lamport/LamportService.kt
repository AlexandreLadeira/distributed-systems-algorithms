package org.ale.pallota.distributed.systems.algorithms.lamport

import com.google.protobuf.Empty
import org.ale.pallotta.lamport.LamportGrpcKt
import org.ale.pallotta.lamport.Message
import java.lang.Integer.max
import java.util.concurrent.atomic.AtomicInteger

class LamportService(private val localTime: AtomicInteger) : LamportGrpcKt.LamportCoroutineImplBase() {
  override suspend fun send(request: Message): Empty {
    val updatedTime = max(request.time, localTime.get()) + 1
    localTime.set(updatedTime)

    println("[$updatedTime] Received message: ${request.body}")

    return Empty.getDefaultInstance()
  }
}
