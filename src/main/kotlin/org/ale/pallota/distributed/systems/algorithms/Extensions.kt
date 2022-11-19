package org.ale.pallota.distributed.systems.algorithms

import io.grpc.Server

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
