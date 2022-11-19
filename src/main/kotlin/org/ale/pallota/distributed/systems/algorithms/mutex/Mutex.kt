package org.ale.pallota.distributed.systems.algorithms.mutex

import io.grpc.ManagedChannelBuilder
import io.grpc.ServerBuilder
import kotlinx.coroutines.delay
import org.ale.pallota.distributed.systems.algorithms.addShutdownHook
import org.ale.pallota.distributed.systems.algorithms.lamport.LamportService
import org.ale.pallotta.lamport.LamportGrpcKt
import org.ale.pallotta.lamport.Message
import org.ale.pallotta.mutex.MutexGrpcKt
import org.ale.pallotta.mutex.MutexMessage
import org.ale.pallotta.mutex.Type
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

suspend fun mutex() {
    val userPorts = listOf(9001, 9002, 9003, 9004)
    val serverPort = System.getenv("PORT")?.toInt() ?: error("Port must not be null")
    val isCoordinator = serverPort == 9000

    val coordinatorStub = MutexGrpcKt.MutexCoroutineStub(
        ManagedChannelBuilder.forAddress("app-9000", 9000).usePlaintext().build()
    )
    val userStubs = userPorts.associateWith { port ->
        MutexGrpcKt.MutexCoroutineStub(
            ManagedChannelBuilder.forAddress(
                "app-$port",
                port
            ).usePlaintext().build()
        )
    }

    if (isCoordinator) {
        MutexCoordinatorService(9000, userStubs).coordinatorRoutine()
    } else {
        MutexUserService(serverPort, coordinatorStub).userRoutine()
    }
}