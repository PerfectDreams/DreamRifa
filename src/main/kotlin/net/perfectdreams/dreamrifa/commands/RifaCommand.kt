package net.perfectdreams.dreamrifa.commands

import com.github.benmanes.caffeine.cache.Caffeine
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.commands.bukkit.SubcommandPermission
import net.perfectdreams.dreamcore.utils.DateUtils
import net.perfectdreams.dreamcore.utils.balance
import net.perfectdreams.dreamcore.utils.broadcast
import net.perfectdreams.dreamcore.utils.hours
import net.perfectdreams.dreamrifa.DreamRifa
import net.perfectdreams.dreamrifa.utils.RifaPlayer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.TimeUnit

class RifaCommand(val m: DreamRifa) : SparklyCommand(arrayOf("rifa")) {

	val cooldownCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build<Player, Long>().asMap()

	@Subcommand
	fun root(sender: Player) {
		sender.sendMessage(DreamRifa.PREFIX + " §bPrêmio atual: §2${m.data.players.sumBy { it.tickets * 250 }} Sonhos")
		sender.sendMessage(DreamRifa.PREFIX + " §bTickets comprados: §2${m.data.players.sumBy { it.tickets }} Tickets")
		sender.sendMessage(DreamRifa.PREFIX + " §bPessoas participando: §2${m.data.players.size} Players")

		val lastWinner = Bukkit.getOfflinePlayer(m.data.lastWinner)
		sender.sendMessage(DreamRifa.PREFIX + " §bÚltimo ganhador: §2${lastWinner.name} (${m.data.lastWinnerPrize} Sonhos)")
		sender.sendMessage(DreamRifa.PREFIX + " §bResultado irá sair daqui a §3${DateUtils.formatDateDiff(m.data.started + ((60 * 1000) * 60))}§b!")
		sender.sendMessage(DreamRifa.PREFIX + " §3Compre um ticket por §2250 Sonhos§3 usando §6/rifa comprar§3!")
	}

	@Subcommand(["comprar", "buy"])
	fun buy(sender: Player, quantity: String = "1") {
		val quantity = quantity.toIntOrNull() ?: 1

		if (quantity <= 0) {
			sender.sendMessage(DreamRifa.PREFIX + "Você tem que comprar pelo menos 1 ticket!")
			return
		}

		val price = quantity * 250

		var rifaPlayer = m.data.players.firstOrNull { it.uniqueId == sender.uniqueId }

		if (rifaPlayer == null) {
			rifaPlayer = RifaPlayer(sender.uniqueId, 0).apply { m.data.players.add(this) }
		}

		if (rifaPlayer.tickets + quantity > 5000) {
			sender.sendMessage(DreamRifa.PREFIX + " §eVocê só pode comprar 5000 tickets!")
			return
		}

		if (cooldownCache.getOrDefault(sender, 0) > System.currentTimeMillis()) {
			sender.sendMessage(DreamRifa.PREFIX + " §eAcalme-se! Espere um pouco antes de executar este comando novamente!")
			return
		}

		if (sender.balance >= price) {
			sender.balance -= price

			rifaPlayer.tickets += quantity

			sender.sendMessage(DreamRifa.PREFIX + " §aVocê comprou ${quantity} ticket${if (quantity == 1) "" else "s"} por §2$price Sonhos§a! Agora é só sentar e relaxar até o resultado da rifa sair!")
			sender.sendMessage(DreamRifa.PREFIX + " §7Querendo mais chances de ganhar? Que tal comprar outro ticket? ;) §6/rifa comprar [quantidade]")

			broadcast(DreamRifa.PREFIX + " §b${sender.displayName}§e comprou ${quantity} ticket${if (quantity == 1) "" else "s"} por §2$price Sonhos§e! Está com sorte? Compre você também! §6/rifa comprar")
			m.save()

			cooldownCache[sender] = System.currentTimeMillis() + 30 * 1000
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
