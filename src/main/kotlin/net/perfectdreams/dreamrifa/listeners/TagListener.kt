package net.perfectdreams.dreamrifa.listeners

import net.perfectdreams.dreamchat.events.ApplyPlayerTagsEvent
import net.perfectdreams.dreamchat.utils.PlayerTag
import net.perfectdreams.dreamrifa.DreamRifa
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class TagListener(val m: DreamRifa) : Listener {
	@EventHandler
	fun onTag(e: ApplyPlayerTagsEvent) {
		if (e.player.name == m.lastWinner) {
			e.tags.add(
				PlayerTag("§a§lS", "§a§lSortudo", listOf("§a${e.player.name}§7 é sortudo e ganhou na última §aRifa!§7"))
			)
		}
	}
}