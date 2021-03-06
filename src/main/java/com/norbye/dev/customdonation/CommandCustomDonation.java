package com.norbye.dev.customdonation;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

public class CommandCustomDonation implements CommandExecutor {

    private Main plugin;

    public CommandCustomDonation(Main plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender commandSender, Command cmd, String label, String[] args) {
        PluginDescriptionFile pdf = plugin.getDescription();
        commandSender.sendMessage(ChatColor.GOLD + "[" + pdf.getName() + "] v" + pdf.getVersion());

        if ("reload".equalsIgnoreCase(args[0])) {
            reloadPlugin();
            commandSender.sendMessage(ChatColor.GOLD + pdf.getName() + " reloaded");
            if (plugin.getConfig().getBoolean("debug", false)) {
                commandSender.sendMessage("Debug mode enabled");
            }
        }
        return true;
    }

    private void reloadPlugin() {
        // Initialize config if it was deleted
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveDefaultConfig();
        // Reload the config
        plugin.reloadConfig();
        // Restart the task
        plugin.initializeTimer();
    }
}
