package org.purpurmc.purpur.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.purpurmc.purpur.YukimalConfig;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class YukimalCommand extends Command {
    public YukimalCommand(String name) {
        super(name);
        this.description = "YukimalMC related commands";
        this.usageMessage = "/yukimal [version | reload | packs]";
        this.setPermission("bukkit.command.yukimal");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) throws IllegalArgumentException {
        if (args.length == 1) {
            return Stream.of("version", "reload", "packs")
                    .filter(arg -> arg.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;

        if (args.length != 1) {
            sender.sendMessage(Component.text("Usage: " + usageMessage, NamedTextColor.RED));
            return false;
        }

        if (args[0].equalsIgnoreCase("version")) {
            sender.sendMessage(Component.text("YukimalMC (" + Bukkit.getName() + " " + Bukkit.getVersion() + ")", NamedTextColor.AQUA));
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (YukimalConfig.init()) {
                sender.sendMessage(Component.text("yukimal.yml reload complete.", NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("yukimal.yml has errors. Previous settings were kept. See the console.", NamedTextColor.RED));
            }
        } else if (args[0].equalsIgnoreCase("packs")) {
            File[] packs = YukimalConfig.listPacks();
            sender.sendMessage(Component.text("Resourcepack folder: " + packs.length + " pack(s)", NamedTextColor.AQUA));
            for (File pack : packs) {
                sender.sendMessage(Component.text(" - " + pack.getName(), NamedTextColor.GRAY));
            }
        } else {
            sender.sendMessage(Component.text("Usage: " + usageMessage, NamedTextColor.RED));
            return false;
        }

        return true;
    }
}