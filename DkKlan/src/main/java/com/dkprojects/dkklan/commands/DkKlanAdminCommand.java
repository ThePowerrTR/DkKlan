package com.dkprojects.dkklan.commands;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.managers.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DkKlanAdminCommand implements CommandExecutor, TabCompleter {
    private final DkKlan plugin;
    public DkKlanAdminCommand(DkKlan plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§eKullanım: /dkklan reload");
            return true;
        }
        String sub = args[0].toLowerCase();
        if (sub.equals("reload")) {
            if (sender instanceof Player && !sender.hasPermission("dkklan.admin")) {
                sender.sendMessage("§cYetkin yok.");
                return true;
            }
            new ConfigManager(plugin).updateConfig("config.yml");
            plugin.reloadConfig();
            plugin.getWarManager().reloadConfig();
            sender.sendMessage("§aDkKlan yapılandırması yeniden yüklendi.");
            return true;
        }
        sender.sendMessage("§eKullanım: /dkklan reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }
}
