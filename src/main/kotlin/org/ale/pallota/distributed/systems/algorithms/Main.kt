package org.ale.pallota.distributed.systems.algorithms

import io.grpc.ManagedChannelBuilder
import org.ale.pallota.distributed.systems.algorithms.election.election
import org.ale.pallota.distributed.systems.algorithms.lamport.lamport
import org.ale.pallota.distributed.systems.algorithms.mutex.mutex
import java.util.concurrent.TimeUnit

enum class Algorithm {
  LAMPORT, MUTEX, ELECTION
}

suspend fun main() {
  val port = System.getenv("PORT")?.toInt() ?: error("Port must not be null")

  val ports = listOf(9001, 9002, 9003, 9004, 9005)
  val channelsByPort = ports.filterNot { it == port }
    .associateWith {
      ManagedChannelBuilder.forAddress(
        "app-$it",
        it
      ).keepAliveTimeout(2, TimeUnit.SECONDS).usePlaintext().build()
    }

  when (Algorithm.valueOf(System.getenv("ALGORITHM"))) {
    Algorithm.LAMPORT -> lamport(port, channelsByPort.values.toList())
    Algorithm.MUTEX -> mutex(port, channelsByPort)
    Algorithm.ELECTION -> election(port, channelsByPort)
  }
}
