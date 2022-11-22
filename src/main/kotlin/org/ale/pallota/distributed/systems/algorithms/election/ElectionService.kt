package org.ale.pallota.distributed.systems.algorithms.election

import com.google.protobuf.Empty
import io.grpc.ManagedChannel
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
  private val userStubs: Map<Int, ElectionGrpcKt.ElectionCoroutineStub>
) : ElectionGrpcKt.ElectionCoroutineImplBase() {

  private val server = buildServer(port, this)
  private val leader: AtomicReference<ElectionGrpcKt.ElectionCoroutineStub> = AtomicReference()
  private val isElectionScheduled = AtomicBoolean(false)

  override suspend fun send(request: ElectionMessage): Empty =
    when (request.type) {
      ElectionMessageType.PING -> Empty.getDefaultInstance()
      ElectionMessageType.ELECTION -> scheduleElection()
      ElectionMessageType.NEW_LEADER -> updateLeader(request.id)
      else -> error("Unrecognized request type")
    }

  private fun scheduleElection(): Empty {
    isElectionScheduled.set(true)
    return Empty.getDefaultInstance()
  }

  private fun updateLeader(newLeader: Int): Empty {
    println("Received leader update, new leader is $newLeader")
    leader.set(userStubs[newLeader]!!)
    isElectionScheduled.set(false)
    return Empty.getDefaultInstance()
  }

  suspend fun routine() {
    runElection()

    while (!server.isTerminated) {
      pingLeader()?.onFailure { scheduleElection() }

      if (isElectionScheduled.get()) {
        runElection()
      }

      delay(1000)
    }
  }

  private suspend fun runElection() {
    val newLeader = userStubs.filterKeys { it > port }
      .mapNotNull { entry ->
        sendElection(entry.value).getOrNull()?.let { entry }
      }
      .maxByOrNull { it.key }

    if (newLeader == null) {
      println("I am the new leader ($port)")
      sendUpdateLeaderToAllUsers()
      leader.set(null)
    }

    isElectionScheduled.set(false)
  }

  private suspend fun pingLeader() = leader.get()?.let { sendPing(it) }

  private suspend fun sendPing(stub: ElectionGrpcKt.ElectionCoroutineStub) =
    sendMessage(stub, createElectionMessage(ElectionMessageType.PING))

  private suspend fun sendElection(stub: ElectionGrpcKt.ElectionCoroutineStub) =
    sendMessage(stub, createElectionMessage(ElectionMessageType.ELECTION))

  private suspend fun sendUpdateLeaderToAllUsers() =
    userStubs.values.forEach {
      sendMessage(it, createElectionMessage(ElectionMessageType.NEW_LEADER, port))
    }

  private suspend fun sendMessage(stub: ElectionGrpcKt.ElectionCoroutineStub, request: ElectionMessage) =
    runCatching {
      (stub.channel as ManagedChannel).resetConnectBackoff()
      stub.withDeadlineAfter(5, TimeUnit.SECONDS)
        .send(request)
    }
}
