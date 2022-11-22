package org.ale.pallota.distributed.systems.algorithms.election

import org.ale.pallotta.election.ElectionMessage
import org.ale.pallotta.election.ElectionMessageType

fun createElectionMessage(type: ElectionMessageType, id: Int = 0): ElectionMessage = ElectionMessage
  .newBuilder()
  .setType(type)
  .setId(id)
  .build()
