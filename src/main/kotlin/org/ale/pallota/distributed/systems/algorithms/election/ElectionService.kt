package org.ale.pallota.distributed.systems.algorithms.election

import com.google.protobuf.Empty
import org.ale.pallotta.election.ElectionGrpcKt

class ElectionService : ElectionGrpcKt.ElectionCoroutineImplBase() {
  override suspend fun ping(request: Empty): Empty {
    return Empty.getDefaultInstance()
  }
}
