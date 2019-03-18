package net.perfectdreams.dreamrifa.utils

import java.util.*

class RifaData(var lastWinner: UUID? = null,
			   var lastWinnerPrize: Double = 0.toDouble(),
			   var players: MutableList<RifaPlayer> = mutableListOf(),
			   var started: Long = 0)

class RifaPlayer(val uniqueId: UUID, var tickets: Int)