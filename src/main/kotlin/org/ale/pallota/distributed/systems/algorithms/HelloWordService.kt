package org.ale.pallota.distributed.systems.algorithms

import org.ale.pallotta.helloworld.GreeterGrpcKt
import org.ale.pallotta.helloworld.HelloReply
import org.ale.pallotta.helloworld.HelloRequest

class HelloWorldService : GreeterGrpcKt.GreeterCoroutineImplBase() {
  override suspend fun sayHello(request: HelloRequest): HelloReply = HelloReply
    .newBuilder()
    .setMessage("Hello ${request.name}")
    .build()
    .also {
      println("Received HelloRequest from client: ${request.name}")
    }

  override suspend fun sayHelloAgain(request: HelloRequest): HelloReply = HelloReply
    .newBuilder()
    .setMessage("Hello again ${request.name}")
    .build()
}
