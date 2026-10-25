package com.github.blade.ezdebug;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

/** {@code /ezdb debug [player]}, {@code /ezdb off [player]}, {@code /ezdb reload}. */
final class Commands implements CommandExecutor, TabCompleter {
    private final EzDebugPlugin plugin;

    Commands(EzDebugPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String action = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        switch (action) {
            case "debug" -> {
                Player target = target(sender, args);
                if (target == null) return true;
                var leftOver = target.getInventory().addItem(plugin.potion().create());
                leftOver.values().forEach(item -> target.getWorld().dropItem(target.getLocation(), item));
                target.sendMessage(Component.text("Here's a Debug Potion. Drink it to see EZvac at work.",
                        NamedTextColor.AQUA));
                if (target != sender)
                    sender.sendMessage(Component.text("Gave " + target.getName() + " a Debug Potion.", NamedTextColor.GREEN));
            }
            case "off" -> {
                Player target = target(sender, args);
                if (target == null) return true;
                if (!plugin.vision().active(target)) {
                    sender.sendMessage(Component.text(target.getName() + " has no debug vision.", NamedTextColor.GRAY));
                    return true;
                }
                plugin.potion().clearCarrier(target);
                plugin.vision().end(target, true);
                if (target != sender)
                    sender.sendMessage(Component.text("Ended " + target.getName() + "'s debug vision.", NamedTextColor.GREEN));
            }
            case "reload" -> {
                plugin.loadSettings();
                plugin.vision().start();
                sender.sendMessage(Component.text("EZdebug+ settings reloaded.", NamedTextColor.GREEN));
            }
            default -> sender.sendMessage(Component.text("Usage: /ezdb <debug|off> [player] | /ezdb reload",
                    NamedTextColor.RED));
        }
        return true;
    }

    /** The named player, or the sender; null (after saying why) when there is none. */
    private static Player target(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            Player named = sender.getServer().getPlayerExact(args[1]);
            if (named == null) sender.sendMessage(Component.text("No player named " + args[1] + " is online.", NamedTextColor.RED));
            return named;
        }
        if (sender instanceof Player player) return player;
        sender.sendMessage(Component.text("From the console, name a player.", NamedTextColor.RED));
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return filter(args[0], List.of("debug", "off", "reload"));
        if (args.length == 2 && !args[0].equalsIgnoreCase("reload"))
            return filter(args[1], sender.getServer().getOnlinePlayers().stream().map(Player::getName).toList());
        return List.of();
    }

    private static List<String> filter(String typed, List<String> options) {
        String prefix = typed.toLowerCase(Locale.ROOT);
        return options.stream().filter(option -> option.toLowerCase(Locale.ROOT).startsWith(prefix)).toList();
    }
}
