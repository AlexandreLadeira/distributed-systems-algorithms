package org.ale.pallota.distributed.systems.algorithms.lamport

import io.grpc.ManagedChannelBuilder
import io.grpc.ServerBuilder
import kotlinx.coroutines.delay
import org.ale.pallota.distributed.systems.algorithms.addShutdownHook
import org.ale.pallotta.lamport.LamportGrpcKt
import org.ale.pallotta.lamport.Message
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

suspend fun lamport() {
    val ports = listOf(9000, 9001, 9002, 9003, 9004)
    val serverPort = System.getenv("PORT")?.toInt() ?: error("Port must not be null")
    val localTime = AtomicInteger()
    val stubs = ports.filterNot { it == serverPort }
        .map { port ->
            LamportGrpcKt.LamportCoroutineStub(
                ManagedChannelBuilder.forAddress(
                    "app-$port",
                    port
                ).usePlaintext().build()
            )
        }

    val server = ServerBuilder
        .forPort(serverPort)
        .addService(LamportService(localTime))
        .build()
        .also { it.start() }

    server.addShutdownHook()

    delay(3000)

    while (!server.isTerminated) {
        val time = localTime.incrementAndGet()
        val message = Message.newBuilder().setTime(time).setBody(UUID.randomUUID().toString()).build()
        println("[${time}] Sending message: ${message.body}")
        stubs.random().send(message)
        delay(Random.nextLong(100, 2000))
    }
}