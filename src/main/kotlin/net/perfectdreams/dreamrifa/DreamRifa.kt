package net.perfectdreams.dreamrifa

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamrifa.commands.RifaCommand
import net.perfectdreams.dreamrifa.listeners.TagListener
import net.perfectdreams.dreamrifa.utils.RifaData
import org.bukkit.Bukkit
import java.io.File

class DreamRifa : KotlinPlugin() {
	companion object {
		const val PREFIX = "§8[§2§lRifa§8]§e"
	}

	lateinit var data: RifaData

	override fun softEnable() {
		super.softEnable()

		registerCommand(RifaCommand(this))
		registerEvents(TagListener(this))

		dataFolder.mkdirs()
		val configFile = File(dataFolder, "data.json")

		if (!configFile.exists()) {
			configFile.createNewFile()
			configFile.writeText(DreamUtils.gson.toJson(RifaData()))
		}

		data = DreamUtils.gson.fromJson(configFile.readText(), RifaData::class.java)

		scheduler().schedule(this) {
			while (true) {
				waitFor(20)
				val diff = System.currentTimeMillis() - data.started

				if (diff > 1.hours.toMillis()) {
					handleWin()
				}
			}
		}
	}

	fun save() {
		scheduler().schedule(this, SynchronizationContext.ASYNC) {
			val loteriaConfig = File(dataFolder, "data.json")

			loteriaConfig.writeText(DreamUtils.gson.toJson(data))
		}
	}

	fun handleWin() {
		if (data.players.isEmpty()) {
			broadcast("$PREFIX §eA rifa acabou e pelo visto ninguém comprou um ticket... :(")

			data.started = System.currentTimeMillis()
		} else {
			val winner = Bukkit.getOfflinePlayer(data.players.random().uniqueId)
			data.lastWinner = winner.uniqueId

			val displayName = Bukkit.getPlayerExact(winner.name)?.displayName ?: winner.name
			val money = data.players.sumBy { it.tickets * 250 }

			data.lastWinnerPrize = money.toDouble()
			broadcast("$PREFIX §b${displayName}§e ganhou a rifa! Parabéns! Prêmio: §2${money} Sonhos§e")

			winner.balance += money

			// TODO: Correios
//			DreamCorreios.addItems(winner, true, true, ItemStack(Material.MAP, 1, 99)
//					.rename("§a§lGanhei na Loteria!")
//					.lore("§7§oUm mapa comemorativo devido a sua vitória na Loteritta!", "§7§o", "§7Você pode usar ele para enfeitar a sua casa", "§7ou apenas guardar como lembrança."))

			data.players.clear()
			data.started = System.currentTimeMillis()
		}
		broadcast("$PREFIX §eUma nova rodada de rifa começou, que tal comprar um ticket? ;) §6/rifa comprar")
		save()
	}

	override fun softDisable() {
		super.softDisable()
	}
}