package org.purpurmc.purpur.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.purpurmc.purpur.YukimalConfig;
import org.purpurmc.purpur.YukimalResourcePacks;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class YukimalCommand extends Command {
    public YukimalCommand(String name) {
        super(name);
        this.description = "YukimalMC related commands";
        this.usageMessage = "/yukimal [version | reload | packs | info]";
        this.setPermission("bukkit.command.yukimal");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) throws IllegalArgumentException {
        if (args.length == 1) {
            return Stream.of("version", "reload", "packs", "info")
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
                YukimalResourcePacks.refreshAll();
                sender.sendMessage(Component.text("yukimal.yml reload complete. Resource packs were updated for online players.", NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("yukimal.yml has errors. Previous settings were kept. See the console.", NamedTextColor.RED));
            }
        } else if (args[0].equalsIgnoreCase("packs")) {
            File[] packs = YukimalConfig.listPacks();
            sender.sendMessage(Component.text("Resourcepack folder: " + packs.length + " pack(s)", NamedTextColor.AQUA));
            for (File pack : packs) {
                sender.sendMessage(Component.text(" - " + pack.getName(), NamedTextColor.GRAY));
            }
        } else if (args[0].equalsIgnoreCase("info")) {
            double[] tps = Bukkit.getTPS();
            Runtime rt = Runtime.getRuntime();
            long usedMb = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
            long maxMb = rt.maxMemory() / 1048576L;
            long upSec = ManagementFactory.getRuntimeMXBean().getUptime() / 1000L;
            sender.sendMessage(Component.text("--- YukimalMC info ---", NamedTextColor.AQUA));
            sender.sendMessage(Component.text("Version: " + Bukkit.getVersion(), NamedTextColor.GRAY));
            sender.sendMessage(Component.text(String.format("TPS (1m/5m/15m): %.1f / %.1f / %.1f", tps[0], tps[1], tps[2]), NamedTextColor.GRAY));
            sender.sendMessage(Component.text("Memory: " + usedMb + " / " + maxMb + " MB", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("Uptime: " + (upSec / 3600) + "h " + ((upSec % 3600) / 60) + "m", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("Players: " + Bukkit.getOnlinePlayers().size() + " / " + Bukkit.getMaxPlayers(), NamedTextColor.GRAY));
            sender.sendMessage(Component.text("Plugins: " + Bukkit.getPluginManager().getPlugins().length, NamedTextColor.GRAY));
            sender.sendMessage(Component.text("Resource packs (yukimal.yml): " + YukimalResourcePacks.configuredPackCount(), NamedTextColor.GRAY));
        } else {
            sender.sendMessage(Component.text("Usage: " + usageMessage, NamedTextColor.RED));
            return false;
        }

        return true;
    }
}