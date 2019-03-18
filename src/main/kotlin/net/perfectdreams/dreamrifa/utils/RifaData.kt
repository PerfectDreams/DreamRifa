package net.perfectdreams.dreamrifa.utils

import java.util.*

class RifaData(var lastWinner: UUID? = null,
			   var lastWinnerPrize: Double = 0.toDouble(),
			   var players: MutableMap<UUID, Int> = mutableMapOf(),
			   var started: Long = 0)