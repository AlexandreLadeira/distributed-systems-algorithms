package org.ale.pallota.distributed.systems.algorithms.mutex

import com.google.protobuf.Empty
import io.grpc.ServerBuilder
import org.ale.pallota.distributed.systems.algorithms.addShutdownHook
import org.ale.pallotta.lamport.LamportGrpcKt
import org.ale.pallotta.lamport.Message
import org.ale.pallotta.mutex.MutexGrpcKt
import org.ale.pallotta.mutex.MutexMessage
import org.ale.pallotta.mutex.Type
import java.lang.Integer.max
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class MutexCoordinatorService(port: Int, private val userStubs: Map<Int, MutexGrpcKt.MutexCoroutineStub>) : MutexGrpcKt.MutexCoroutineImplBase() {

  private val server = ServerBuilder
    .forPort(port)
    .addService(this)
    .build()
    .also { it.start() }
    .addShutdownHook()

  private val queue = mutableListOf<Int>()
  private val reserved = AtomicBoolean(false)

  override suspend fun send(request: MutexMessage): Empty {
    when (request.type) {
      Type.REQUEST -> {
        if (!reserved.getAndSet(true)) {
          val message = MutexMessage.newBuilder().setType(Type.OK).setId(request.id).build()
          println("[coordinator] Reserving resource for ${message.id}")
          userStubs[request.id]!!.send(message)
        } else {
          queue.add(request.id)
          println("[coordinator] Resource is busy, enqueueing ${request.id}. Queue: $queue")
        }
      }
      Type.RELEASE -> {
        if (queue.isNotEmpty()) {
          val userId = queue.removeFirst()
          val message = MutexMessage.newBuilder().setType(Type.OK).setId(userId).build()
          println("[coordinator] Reserving resource for $userId. Queue: $queue")
          userStubs[userId]!!.send(message)
        } else {
          println("[coordinator] Resource is free, queue is empty")
          reserved.set(false)
        }
      }
      else -> error("Invalid request type")
    }
    return Empty.getDefaultInstance()
  }

  suspend fun coordinatorRoutine() {
    server.awaitTermination()
  }
}
