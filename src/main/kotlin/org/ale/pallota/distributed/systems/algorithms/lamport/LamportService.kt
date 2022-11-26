package org.ale.pallota.distributed.systems.algorithms.lamport

import com.google.protobuf.Empty
import kotlinx.coroutines.delay
import org.ale.pallota.distributed.systems.algorithms.buildServer
import org.ale.pallotta.election.ElectionGrpcKt
import org.ale.pallotta.lamport.LamportGrpcKt
import org.ale.pallotta.lamport.Message
import java.lang.Integer.max
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

class LamportService(
  port: Int,
  private val peerStubs: List<LamportGrpcKt.LamportCoroutineStub>
) : LamportGrpcKt.LamportCoroutineImplBase() {

  private val server = buildServer(port, this)

  private val localTime = AtomicInteger()

  override suspend fun send(request: Message): Empty {
    val updatedTime = max(request.time, localTime.get()) + 1
    localTime.set(updatedTime)

    println("[$updatedTime] Received message: ${request.body}")

    return Empty.getDefaultInstance()
  }

  suspend fun routine() {
    delay(3000)

    while (!server.isTerminated) {
      val message = buildRandomMessage()
      println("[${message.time}] Sending message: ${message.body}")
      peerStubs.random().send(message)

      delay(Random.nextLong(100, 2000))
    }
  }

  private fun buildRandomMessage(): Message {
    val time = localTime.incrementAndGet()
    return Message.newBuilder()
      .setTime(time)
      .setBody(UUID.randomUUID().toString().substringBefore("-"))
      .build()
  }
}
