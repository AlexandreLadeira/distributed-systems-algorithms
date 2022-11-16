package org.ale.pallota.distributed.systems.algorithms.lamport

import io.grpc.ManagedChannelBuilder
import io.grpc.Server
import io.grpc.ServerBuilder
import kotlinx.coroutines.delay
import org.ale.pallota.distributed.systems.algorithms.HelloWorldClient
import org.ale.pallota.distributed.systems.algorithms.HelloWorldServer
import org.ale.pallotta.helloworld.GreeterGrpcKt
import org.ale.pallotta.lamport.LamportGrpcKt
import org.ale.pallotta.lamport.Message
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.coroutineContext
import kotlin.random.Random
import kotlin.random.nextLong

suspend fun main() {
    val ports = listOf(9000, 9001, 9002, 9003, 9004)
    //val serverPort = System.getenv("PORT")?.toInt() ?: error("Port must not be null")
    val serverPort = 9004
    val localTime = AtomicInteger()
    val stubs = ports.filterNot { it == serverPort }
        .map { port ->
            LamportGrpcKt.LamportCoroutineStub(
                ManagedChannelBuilder.forAddress(
                    "localhost",
                    port
                ).usePlaintext().build()
            )
        }

    val server = ServerBuilder
        .forPort(serverPort)
        .addService(LamportService(localTime))
        .build()
        .also { it.start() }

    Runtime.getRuntime().addShutdownHook(
        Thread {
            println("*** shutting down gRPC server since JVM is shutting down")
            server.shutdown()
            println("*** server shut down")
        }
    )

    delay(20000)

    while (!server.isTerminated) {
        val time = localTime.incrementAndGet()
        val message = Message.newBuilder().setTime(time).setBody(UUID.randomUUID().toString()).build()
        println("[${time}] Sending message: ${message.body}")
        stubs.random().send(message)
        delay(Random.nextLong(100, 2000))
    }
}