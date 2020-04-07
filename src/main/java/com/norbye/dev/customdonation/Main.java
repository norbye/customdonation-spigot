package com.norbye.dev.customdonation;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Timer;

public class Main extends JavaPlugin {

    FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        // Set the default config
        config.addDefault("api.root", "https://example.com/api.php");
        config.addDefault("api.poll-seconds", 5);
        config.options().copyDefaults(true);
        this.saveDefaultConfig();

        this.getCommand("customdonation").setExecutor(new CommandCustomDonation(this));

        // Initialize the looper searching for new donations
        Timer timer = new Timer();
        int intervalSeconds = config.getInt("api.poll-seconds", 5);
        timer.schedule(new ApiTask(), 5000, intervalSeconds * 1000);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
