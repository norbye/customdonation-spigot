package com.norbye.dev.customdonation;

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
        timer.cancel();
        timer.purge();
        int intervalSeconds = getConfig().getInt("api.poll-interval", 20);
        timer.schedule(new ApiTask(this), 5000, intervalSeconds * 1000);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
