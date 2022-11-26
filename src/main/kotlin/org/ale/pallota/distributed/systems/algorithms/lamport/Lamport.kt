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
  LamportService(
    port = serverPort,
    peerStubs = channels.map { LamportGrpcKt.LamportCoroutineStub(it) }
  ).routine()
}
