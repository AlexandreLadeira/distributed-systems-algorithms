package org.ale.pallota.distributed.systems.algorithms.election

import com.google.protobuf.Empty
import io.grpc.ManagedChannel
import kotlinx.coroutines.delay
import org.ale.pallota.distributed.systems.algorithms.buildServer
import org.ale.pallotta.election.ElectionGrpcKt

suspend fun runElection(
  serverPort: Int,
  stubsByPort: Map<Int, ElectionGrpcKt.ElectionCoroutineStub>
): Map.Entry<Int, ElectionGrpcKt.ElectionCoroutineStub>? {
  println("Running election")

  val leader = stubsByPort.filterKeys { it > serverPort }
    .mapNotNull { entry ->
      runCatching { entry.value.ping(Empty.getDefaultInstance()).let { entry } }.getOrNull()
    }
    .maxByOrNull { it.key }

  leader?.let {
    println("Leader is ${it.key}")
  } ?: println("I am the leader")

  return leader
}

suspend fun election(serverPort: Int, channelsByPort: Map<Int, ManagedChannel>) {
  val stubsByPort = channelsByPort.mapValues { ElectionGrpcKt.ElectionCoroutineStub(it.value) }
  val server = buildServer(serverPort, ElectionService())

  delay(3000)

  var leader = runElection(serverPort, stubsByPort)

  while (!server.isTerminated) {
    runCatching { leader?.value?.ping(Empty.getDefaultInstance()) }.onFailure {
      println("Leader is not alive")
      leader = runElection(serverPort, stubsByPort)
    }
    delay(1000)
  }
}