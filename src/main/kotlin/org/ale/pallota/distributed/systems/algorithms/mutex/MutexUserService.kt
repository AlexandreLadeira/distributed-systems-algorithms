package org.ale.pallota.distributed.systems.algorithms.mutex

import com.google.protobuf.Empty
import io.grpc.ServerBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.yield
import org.ale.pallota.distributed.systems.algorithms.addShutdownHook
import org.ale.pallotta.mutex.MutexGrpc.MutexStub
import org.ale.pallotta.mutex.MutexGrpcKt
import org.ale.pallotta.mutex.MutexMessage
import org.ale.pallotta.mutex.Type
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

class MutexUserService(private val port: Int, private val coordinatorStub: MutexGrpcKt.MutexCoroutineStub) : MutexGrpcKt.MutexCoroutineImplBase() {

  private val server = ServerBuilder
    .forPort(port)
    .addService(this)
    .build()
    .also { it.start() }
    .addShutdownHook()

  private val waitingForRequest = AtomicBoolean(false)

  override suspend fun send(request: MutexMessage): Empty {
    require(request.type == Type.OK)

    // Use resource for some time
    delay(Random.nextLong(2000, 8000))

    val message = MutexMessage.newBuilder().setType(Type.RELEASE).setId(port).build()
    println("Releasing resource")
    coordinatorStub.send(message)

    waitingForRequest.set(false)

    return Empty.getDefaultInstance()
  }

  suspend fun userRoutine() {
    while (!server.isTerminated) {
      delay(Random.nextLong(4000, 6000))
      val message = MutexMessage.newBuilder().setType(Type.REQUEST).setId(port).build()
      println("Requesting resource")
      coordinatorStub.send(message)

      waitingForRequest.set(true)
      while (waitingForRequest.get()) {
        delay(200)
      }
    }
  }
}
