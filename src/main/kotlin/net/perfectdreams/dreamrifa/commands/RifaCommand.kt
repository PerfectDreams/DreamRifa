package net.perfectdreams.dreamrifa.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.commands.bukkit.SubcommandPermission
import net.perfectdreams.dreamcore.utils.DateUtils
import net.perfectdreams.dreamcore.utils.balance
import net.perfectdreams.dreamcore.utils.broadcast
import net.perfectdreams.dreamcore.utils.hours
import net.perfectdreams.dreamrifa.DreamRifa
import org.bukkit.entity.Player
import java.util.*

class RifaCommand(val m: DreamRifa) : SparklyCommand(arrayOf("rifa")) {

	@Subcommand
	fun root(sender: Player) {
		sender.sendMessage(DreamRifa.PREFIX + " §bPrêmio atual: §2${m.players.size * 250} Sonhos")
		sender.sendMessage(DreamRifa.PREFIX + " §bTickets comprados: §2${m.players.size} Tickets")
		sender.sendMessage(DreamRifa.PREFIX + " §bPessoas participando: §2${m.players.toMutableSet().size} Players")
		sender.sendMessage(DreamRifa.PREFIX + " §bÚltimo ganhador: §2${m.lastWinner} (${m.lastWinnerPrize} Sonhos)")
		val cal = Calendar.getInstance()
		cal.timeInMillis = m.started + 1.hours.toMillis()
		sender.sendMessage(DreamRifa.PREFIX + " §bResultado irá sair daqui a §3${DateUtils.formatDateDiff(Calendar.getInstance(), cal)}§b!")
		sender.sendMessage(DreamRifa.PREFIX + " §3Compre um ticket por §2250 Sonhos§3 usando §6/rifa comprar§3!")
	}

	@Subcommand(["comprar", "buy"])
	fun buy(sender: Player, quantity: String) {
		val quantity = quantity.toIntOrNull() ?: 1
		val price = quantity * 250

		if (sender.balance >= price) {
			for (i in 0 until quantity) {
				sender.balance -= price
				m.players.add(sender.name)
			}
			sender.sendMessage(DreamRifa.PREFIX + " §aVocê comprou ${quantity} ticket${if (quantity == 1) "" else "s"} por §2$price Sonhos§a! Agora é só sentar e relaxar até o resultado da rifa sair!")
			sender.sendMessage(DreamRifa.PREFIX + " §7Querendo mais chances de ganhar? Que tal comprar outro ticket? ;) §6/rifa comprar [quantidade]")

			broadcast(DreamRifa.PREFIX + " §b${sender.displayName}§e comprou ${quantity} ticket${if (quantity == 1) "" else "s"} por §2$price Sonhos§e! Está com sorte? Compre você também! §6/rifa comprar")
			m.save()
		} else {
			sender.sendMessage("${DreamRifa.PREFIX} §cVocê precisa ter §2+${price - sender.balance} Sonhos§c para poder comprar um ticket!")
		}
	}

	@Subcommand(["force"])
	@SubcommandPermission("dreamrifa.admin")
	fun force(sender: Player) {
		m.handleWin()
	}
}