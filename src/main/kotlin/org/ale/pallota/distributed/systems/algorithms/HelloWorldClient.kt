package org.ale.pallota.distributed.systems.algorithms

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.io.Closeable
import java.util.concurrent.TimeUnit
import org.ale.pallotta.helloworld.GreeterGrpcKt
import org.ale.pallotta.helloworld.HelloRequest

class HelloWorldClient(private val channel: ManagedChannel) : Closeable {
  private val stub: GreeterGrpcKt.GreeterCoroutineStub = GreeterGrpcKt.GreeterCoroutineStub(channel)

  suspend fun greet(name: String) {
    val request = HelloRequest.newBuilder().setName(name).build()
    val response = stub.sayHello(request)
    println("Received: ${response.message}")
    val againResponse = stub.sayHelloAgain(request)
    println("Received: ${againResponse.message}")
  }

  override fun close() {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
  }
}

suspend fun main() {
  val port = 9393

  val channel = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build()

  val client = HelloWorldClient(channel)

  client.greet("Client teste")
}
