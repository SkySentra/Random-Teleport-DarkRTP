package com.darkrtp.message;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

public interface MessageService {

    void send(CommandSender recipient, Message message, TagResolver... placeholders);

    void reload();

    static TagResolver placeholder(String name, String value) {
        return Placeholder.unparsed(name, value);
    }
}
