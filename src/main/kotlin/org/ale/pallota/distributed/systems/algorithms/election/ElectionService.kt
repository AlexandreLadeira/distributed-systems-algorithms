package org.ale.pallota.distributed.systems.algorithms.election

import com.google.protobuf.Empty
import io.grpc.ConnectivityState
import io.grpc.ManagedChannel
import io.grpc.Server
import kotlinx.coroutines.delay
import org.ale.pallota.distributed.systems.algorithms.buildServer
import org.ale.pallotta.election.ElectionGrpcKt
import org.ale.pallotta.election.ElectionMessage
import org.ale.pallotta.election.ElectionMessageType
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class ElectionService(
  private val port: Int,
  private val peerStubs: Map<Int, ElectionGrpcKt.ElectionCoroutineStub>
) : ElectionGrpcKt.ElectionCoroutineImplBase() {

  private val leader: AtomicReference<ElectionGrpcKt.ElectionCoroutineStub> = AtomicReference()
  private val isElectionScheduled = AtomicBoolean(true)

  override suspend fun send(request: ElectionMessage): Empty =
    when (request.type) {
      ElectionMessageType.PING -> {}
      ElectionMessageType.ELECTION -> scheduleElection()
      ElectionMessageType.NEW_LEADER -> updateLeader(request.id)
      else -> error("Unrecognized request type")
    }.let { Empty.getDefaultInstance() }

  private fun scheduleElection() {
    isElectionScheduled.set(true)
  }

  private fun updateLeader(newLeader: Int) {
    println("Received leader update, new leader is $newLeader")
    leader.set(peerStubs[newLeader]!!)
  }

  suspend fun routine() {
    delay(10000)

    println("Starting server...")
    val server = buildServer(port, this)

    while (!server.isTerminated) {
      pingLeader()?.onFailure { scheduleElection() }

      if (isElectionScheduled.get()) {
        runElection()
        isElectionScheduled.set(false)
      }

      delay(1000)
    }
  }

  private suspend fun runElection() {
    val responses = peerStubs.filterKeys { it > port }
      .mapNotNull { sendElection(it.value).getOrNull() }

    if (responses.isEmpty()) {
      println("I am the new leader ($port)")
      sendUpdateLeaderToAllUsers()
      leader.set(null)
    }
  }

  private suspend fun pingLeader() = leader.get()?.let { sendPing(it) }

  private suspend fun sendPing(stub: ElectionGrpcKt.ElectionCoroutineStub) =
    sendMessage(stub, createElectionMessage(ElectionMessageType.PING))

  private suspend fun sendElection(stub: ElectionGrpcKt.ElectionCoroutineStub) =
    sendMessage(stub, createElectionMessage(ElectionMessageType.ELECTION))

  private suspend fun sendUpdateLeaderToAllUsers() =
    peerStubs.values.forEach {
      sendMessage(it, createElectionMessage(ElectionMessageType.NEW_LEADER, port))
    }

  private suspend fun sendMessage(
    stub: ElectionGrpcKt.ElectionCoroutineStub,
    request: ElectionMessage
  ) =
    runCatching {
      (stub.channel as ManagedChannel).resetConnectBackoff()
      stub.withDeadlineAfter(2, TimeUnit.SECONDS).send(request)
    }
}
