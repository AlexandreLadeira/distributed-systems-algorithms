package org.ale.pallota.distributed.systems.algorithms.mutex

import io.grpc.ManagedChannel
import org.ale.pallotta.mutex.MutexGrpcKt

suspend fun mutex(serverPort: Int, channelsByPort: Map<Int, ManagedChannel>) {
    when (serverPort) {
        COORDINATOR_PORT ->
            MutexCoordinatorService(
                port = serverPort,
                userStubs = channelsByPort.mapValues { MutexGrpcKt.MutexCoroutineStub(it.value) }
            ).coordinatorRoutine()
        else ->
            MutexUserService(
                port = serverPort,
                coordinatorStub = MutexGrpcKt.MutexCoroutineStub(channelsByPort[COORDINATOR_PORT]!!)
            ).userRoutine()
    }
}

const val COORDINATOR_PORT = 9001
