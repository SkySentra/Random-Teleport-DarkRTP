package com.darkrtp.message;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.EnumMap;
import java.util.Map;

public final class MiniMessageService implements MessageService {

    private final Plugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<Message, String> templates = new EnumMap<>(Message.class);
    private String prefix = "";

    public MiniMessageService(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    @Override
    public void reload() {
        templates.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("messages");
        if (section == null) {
            return;
        }
        for (Message message : Message.values()) {
            String value = section.getString(message.key());
            if (value != null) {
                templates.put(message, value);
            }
        }
        prefix = templates.getOrDefault(Message.PREFIX, "");
    }

    @Override
    public void send(CommandSender recipient, Message message, TagResolver... placeholders) {
        String template = templates.get(message);
        if (template == null || template.isEmpty()) {
            return;
        }
        recipient.sendMessage(miniMessage.deserialize(prefix + template, placeholders));
    }
}
