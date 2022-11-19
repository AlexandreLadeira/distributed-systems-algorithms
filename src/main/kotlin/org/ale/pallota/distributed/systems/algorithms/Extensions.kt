package org.ale.pallota.distributed.systems.algorithms

import io.grpc.BindableService
import io.grpc.Server
import io.grpc.ServerBuilder
import org.ale.pallota.distributed.systems.algorithms.lamport.LamportService

class Extensions {
}

fun Server.addShutdownHook() = this.also {
    Runtime.getRuntime().addShutdownHook(
        Thread {
            println("*** shutting down gRPC server since JVM is shutting down")
            shutdown()
            println("*** server shut down")
        }
    )
}

fun buildServer(serverPort: Int, service: BindableService) = ServerBuilder
    .forPort(serverPort)
    .addService(service)
    .build()
    .also { it.start() }
    .addShutdownHook()
