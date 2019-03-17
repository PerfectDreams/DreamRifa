package net.perfectdreams.dreamrifa

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamrifa.commands.RifaCommand
import net.perfectdreams.dreamrifa.listeners.TagListener
import org.bukkit.Bukkit
import java.io.File

class DreamRifa : KotlinPlugin() {
	companion object {
		const val PREFIX = "§8[§2§lRifa§8]§e"
	}

	var lastWinner: String? = null
	var lastWinnerPrize = 0
	var players = mutableListOf<String>()
	var started = System.currentTimeMillis()

	override fun softEnable() {
		super.softEnable()

		registerCommand(RifaCommand(this))
		registerEvents(TagListener(this))

		dataFolder.mkdirs()
		val loteriaConfig = File(dataFolder, "config.json")

		if (loteriaConfig.exists()) {
			val json = DreamUtils.jsonParser.parse(loteriaConfig.readText()).obj
			lastWinner = json["lastWinner"].nullString
			lastWinnerPrize = json["lastWinnerPrize"].nullInt ?: 0
			players = DreamUtils.gson.fromJson(json["players"])
			started = json["started"].long
		}

		scheduler().schedule(this) {
			while (true) {
				waitFor(20)
				val diff = System.currentTimeMillis() - started

				if (diff > 1.hours.toMillis()) {
					handleWin()
				}
			}
		}
	}

	fun save() {
		scheduler().schedule(this, SynchronizationContext.ASYNC) {
			val loteriaConfig = File(dataFolder, "config.json")
			val json = JsonObject()

			json["lastWinner"] = lastWinner
			json["lastWinnerPrize"] = lastWinnerPrize
			json["players"] = DreamUtils.gson.toJsonTree(players)
			json["started"] = started

			loteriaConfig.writeText(json.toString())
		}
	}

	fun handleWin() {
		if (players.isEmpty()) {
			broadcast("$PREFIX §eA rifa acabou e pelo visto ninguém comprou um ticket... :(")

			started = System.currentTimeMillis()
		} else {
			val winner = Bukkit.getOfflinePlayer(players.random())
			lastWinner = winner.name

			val displayName = Bukkit.getPlayerExact(winner.name)?.displayName ?: winner.name
			val money = players.size * 250

			lastWinnerPrize = money
			broadcast("$PREFIX §b${displayName}§e ganhou a rifa! Parabéns! Prêmio: §2${money} Sonhos§e")

			// TODO: Correios
//			DreamCorreios.addItems(winner, true, true, ItemStack(Material.MAP, 1, 99)
//					.rename("§a§lGanhei na Loteria!")
//					.lore("§7§oUm mapa comemorativo devido a sua vitória na Loteritta!", "§7§o", "§7Você pode usar ele para enfeitar a sua casa", "§7ou apenas guardar como lembrança."))

			players.clear()
			started = System.currentTimeMillis()
		}
		broadcast("$PREFIX §eUma nova rodada de rifa começou, que tal comprar um ticket? ;) §6/rifa comprar")
		save()
	}

	override fun softDisable() {
		super.softDisable()
	}
}