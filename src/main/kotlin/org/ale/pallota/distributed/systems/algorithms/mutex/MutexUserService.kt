package org.ale.pallota.distributed.systems.algorithms.mutex

import com.google.protobuf.Empty
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random
import kotlinx.coroutines.delay
import org.ale.pallota.distributed.systems.algorithms.buildServer
import org.ale.pallotta.mutex.MutexGrpcKt
import org.ale.pallotta.mutex.MutexMessage
import org.ale.pallotta.mutex.Type

class MutexUserService(private val port: Int, private val coordinatorStub: MutexGrpcKt.MutexCoroutineStub) :
  MutexGrpcKt.MutexCoroutineImplBase() {

  private val server = buildServer(port, this)

  private val waitingForResource = AtomicBoolean(false)

  override suspend fun send(request: MutexMessage): Empty {
    require(request.type == Type.OK)

    // Use resource for some time
    delay(Random.nextLong(2000, 8000))

    println("Releasing resource")
    coordinatorStub.send(createMutexMessage(Type.RELEASE, port))

    waitingForResource.set(false)

    return Empty.getDefaultInstance()
  }

  suspend fun userRoutine() {
    while (!server.isTerminated) {
      delay(Random.nextLong(4000, 6000))

      println("Requesting resource")
      coordinatorStub.send(createMutexMessage(Type.REQUEST, port))

      waitForResource()
    }
  }

  private suspend fun waitForResource() {
    waitingForResource.set(true)
    while (waitingForResource.get()) {
      delay(200)
    }
  }
}
