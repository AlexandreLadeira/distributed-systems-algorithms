package org.ale.pallota.distributed.systems.algorithms.mutex

import com.google.protobuf.Empty
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import org.ale.pallota.distributed.systems.algorithms.buildServer
import org.ale.pallotta.mutex.MutexGrpcKt
import org.ale.pallotta.mutex.MutexMessage
import org.ale.pallotta.mutex.Type

class MutexCoordinatorService(port: Int, private val userStubs: Map<Int, MutexGrpcKt.MutexCoroutineStub>) :
  MutexGrpcKt.MutexCoroutineImplBase() {

  private val server = buildServer(port, this)

  private val queue = ConcurrentLinkedQueue<Int>()
  private val reserved = AtomicBoolean(false)

  override suspend fun send(request: MutexMessage): Empty {
    when (request.type) {
      Type.REQUEST -> handleRequest(request.id)
      Type.RELEASE -> handleRelease()
      else -> error("Invalid request type")
    }
    return Empty.getDefaultInstance()
  }

  private suspend fun handleRequest(id: Int) {
    if (!reserved.getAndSet(true)) {
      println("[coordinator] Reserving resource for $id")
      userStubs[id]!!.send(createMutexMessage(Type.OK, id))
    } else {
      queue.add(id)
      println("[coordinator] Resource is busy, enqueueing $id. Queue: $queue")
    }
  }

  private suspend fun handleRelease() {
    if (queue.isNotEmpty()) {
      val userId = queue.remove()
      println("[coordinator] Reserving resource for $userId. Queue: $queue")
      userStubs[userId]!!.send(createMutexMessage(Type.OK, userId))
    } else {
      println("[coordinator] Resource is free, queue is empty")
      reserved.set(false)
    }
  }

  fun coordinatorRoutine() {
    server.awaitTermination()
  }
}
