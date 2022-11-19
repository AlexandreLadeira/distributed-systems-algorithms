package org.ale.pallota.distributed.systems.algorithms.lamport

import io.grpc.ManagedChannel
import io.grpc.ServerBuilder
import kotlinx.coroutines.delay
import org.ale.pallota.distributed.systems.algorithms.addShutdownHook
import org.ale.pallotta.lamport.Message
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import org.ale.pallota.distributed.systems.algorithms.buildServer
import org.ale.pallotta.lamport.LamportGrpcKt

suspend fun lamport(serverPort: Int, channels: List<ManagedChannel>) {
    val localTime = AtomicInteger()
    val stubs = channels.map { LamportGrpcKt.LamportCoroutineStub(it) }
    val server = buildServer(serverPort, LamportService(localTime))

    delay(3000)

    while (!server.isTerminated) {
        val time = localTime.incrementAndGet()
        val message = Message.newBuilder()
            .setTime(time)
            .setBody(UUID.randomUUID().toString().substringBefore("-"))
            .build()
        println("[${time}] Sending message: ${message.body}")
        stubs.random().send(message)
        delay(Random.nextLong(100, 2000))
    }
}
