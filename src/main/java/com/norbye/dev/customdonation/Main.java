package com.norbye.dev.customdonation;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Timer;

public class Main extends JavaPlugin {

    public Timer timer = new Timer();

    @Override
    public void onEnable() {
        // Set the default config
        getConfig().options().copyDefaults(true);
        this.saveDefaultConfig();

        this.getCommand("customdonation").setExecutor(new CommandCustomDonation(this));

        // Initialize the looper searching for new donations
        initializeTimer();
    }

    public void initializeTimer() {
        Bukkit.getScheduler().cancelTasks(this);
        int intervalSeconds = getConfig().getInt("api.poll-interval", 20);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
                this,
                new ApiTask(this),
                20L * intervalSeconds,
                20L * intervalSeconds
        );
    }

    @Override
    public void onDisable() {
        // Cancel tasks
        Bukkit.getScheduler().cancelTasks(this);
    }
}
