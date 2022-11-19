package org.ale.pallota.distributed.systems.algorithms

import org.ale.pallota.distributed.systems.algorithms.lamport.lamport
import org.ale.pallota.distributed.systems.algorithms.mutex.mutex

enum class Algorithm {
    LAMPORT, MUTEX
}

suspend fun main() {
    when (Algorithm.valueOf(System.getenv("ALGORITHM"))) {
        Algorithm.LAMPORT -> lamport()
        Algorithm.MUTEX -> mutex()
    }
}