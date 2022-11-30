package org.ale.pallota.distributed.systems.algorithms.election

import io.grpc.ConnectivityState
import io.grpc.ManagedChannel
import org.ale.pallotta.election.ElectionGrpcKt

suspend fun election(serverPort: Int, channelsByPort: Map<Int, ManagedChannel>) {
  ElectionService(
    port = serverPort,
    peerStubs = channelsByPort.mapValues { ElectionGrpcKt.ElectionCoroutineStub(it.value) }
  ).routine()
}