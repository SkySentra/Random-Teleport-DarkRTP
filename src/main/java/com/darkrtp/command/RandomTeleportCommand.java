package com.darkrtp.command;

import com.darkrtp.message.Message;
import com.darkrtp.message.MessageService;
import com.darkrtp.teleport.RandomTeleportService;
import com.darkrtp.teleport.TeleportRequest;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RandomTeleportCommand implements TabExecutor {

    private static final String PERMISSION_USE = "darkrtp.use";
    private static final String PERMISSION_OTHERS = "darkrtp.others";
    private static final String PERMISSION_RELOAD = "darkrtp.reload";
    private static final String PERMISSION_BYPASS_COOLDOWN = "darkrtp.bypass.cooldown";
    private static final String PERMISSION_BYPASS_WARMUP = "darkrtp.bypass.warmup";

    private final RandomTeleportService teleportService;
    private final MessageService messages;
    private final Runnable reloadAction;

    public RandomTeleportCommand(RandomTeleportService teleportService,
                                 MessageService messages,
                                 Runnable reloadAction) {
        this.teleportService = teleportService;
        this.messages = messages;
        this.reloadAction = reloadAction;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            return handleReload(sender);
        }
        if (args.length >= 1 && args[0].equalsIgnoreCase("help")) {
            messages.send(sender, Message.HELP);
            return true;
        }
        if (!sender.hasPermission(PERMISSION_USE)) {
            messages.send(sender, Message.NO_PERMISSION);
            return true;
        }
        if (args.length >= 1) {
            return handleTeleportOther(sender, args[0]);
        }
        return handleTeleportSelf(sender);
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission(PERMISSION_RELOAD)) {
            messages.send(sender, Message.NO_PERMISSION);
            return true;
        }
        reloadAction.run();
        messages.send(sender, Message.RELOADED);
        return true;
    }

    private boolean handleTeleportOther(CommandSender sender, String targetName) {
        if (!sender.hasPermission(PERMISSION_OTHERS)) {
            messages.send(sender, Message.NO_PERMISSION);
            return true;
        }
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            messages.send(sender, Message.PLAYER_NOT_FOUND, MessageService.placeholder("player", targetName));
            return true;
        }
        teleportService.teleport(new TeleportRequest(target, sender,
                sender.hasPermission(PERMISSION_BYPASS_COOLDOWN),
                sender.hasPermission(PERMISSION_BYPASS_WARMUP)));
        return true;
    }

    private boolean handleTeleportSelf(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, Message.PLAYERS_ONLY);
            return true;
        }
        teleportService.teleport(new TeleportRequest(player, player,
                player.hasPermission(PERMISSION_BYPASS_COOLDOWN),
                player.hasPermission(PERMISSION_BYPASS_WARMUP)));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length != 1) {
            return suggestions;
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        if ("help".startsWith(prefix)) {
            suggestions.add("help");
        }
        if (sender.hasPermission(PERMISSION_RELOAD) && "reload".startsWith(prefix)) {
            suggestions.add("reload");
        }
        if (sender.hasPermission(PERMISSION_OTHERS)) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    suggestions.add(online.getName());
                }
            }
        }
        return suggestions;
    }
}
