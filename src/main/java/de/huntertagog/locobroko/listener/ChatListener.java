package de.huntertagog.locobroko.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mineacademy.fo.annotation.AutoRegister;

/**
 * Listener class for handling player join and quit events.
 */

@AutoRegister
public final class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        // Konvertiere die Component-Nachricht in einen String
        String rawMessage = LegacyComponentSerializer.legacyAmpersand().serialize(event.message());

        // Ersetze Platzhalter im String
        String messageWithPlaceholders = PlaceholderAPI.setPlaceholders(event.getPlayer(), rawMessage);

        // Konvertiere den String zurück zu einer Component
        Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(messageWithPlaceholders);

        // Setze die neue Nachricht im Event
        event.message(message);

        // Kein Need to cancel the event unless you handle sending the message differently
        // event.setCancelled(true); // Entferne dies, wenn du nicht manuell senden möchtest
    }

}
