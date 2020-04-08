package com.norbye.dev.customdonation;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

class ApiTask extends TimerTask {

    private Main plugin;

    private CloseableHttpClient httpClient;

    private String urlRoot;
    private String passKey;

    public ApiTask(Main plugin) {
        this.plugin = plugin;
    }

    public void run() {
        // Reset connection for each run
        httpClient = HttpClients.createDefault();

        // Update vars from config
        urlRoot = plugin.getConfig().getString("api.root", "");
        passKey = plugin.getConfig().getString("api.pass-key", "");

        if (urlRoot == "") {
            error("Missing api.root in the config.yml");
            return;
        }

        try {
            fetchActiveDonations();
        } catch (IOException e) {
            debug("fectactivedonations failed");
            e.printStackTrace();
        } finally {
            try {;
                httpClient.close();
            } catch (IOException e) {
                debug("Failed to close httpClient");
                e.printStackTrace();
            }
        }
    }

    private void fetchActiveDonations() throws IOException {
        HttpGet request = new HttpGet(urlRoot);

        // add request headers
        request.addHeader("pass-key", passKey);
        request.addHeader(HttpHeaders.USER_AGENT, "Googlebot");

        try (CloseableHttpResponse response = httpClient.execute(request)) {

            // Get HttpResponse Status
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                debug("fetchActiveDonations unsuccessfull request " + statusCode);
            }

            HttpEntity entity = response.getEntity();
            Header headers = entity.getContentType();

            if (entity == null) {
                return;
            }

            // return it as a String
            String result = EntityUtils.toString(entity);
            debug("fetchActiveDonations res \n" + result);
            // JSON result
            JSONObject res = new JSONObject(result);
            JSONArray donations = res.getJSONArray("donations");
            for (int i = 0; i < donations.length(); i++) {
                JSONObject donation = donations.getJSONObject(i);
                JSONArray jCommands = donation.getJSONArray("commands");
                String[] commands = new String[jCommands.length()];
                for (int k = 0; k < jCommands.length(); k++) {
                    commands[k] = jCommands.getString(k);
                }
                executeDonation(
                        donation.getInt("id"),
                        donation.getString("username"),
                        commands
                );
            }
        }
    }

    private void executeDonation(int donationId, String username, String[] commands) {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        for (int i = 0; i < commands.length; i++) {
            String command = "/" + commands[i];
            try {
                log("Command: " + command);
                boolean success = Bukkit.getScheduler().callSyncMethod(
                        plugin,
                        () -> Bukkit.dispatchCommand(console, command)
                ).get();
            } catch (InterruptedException | ExecutionException e) {
                debug("Failed to send command");
                e.printStackTrace();
            }
        }

        // Update the donation info
        try {
            updateExecutedDonation(donationId);
        } catch (IOException e) {
            debug("updateExecutedDonation threw exception");
            e.printStackTrace();
        }
    }

    private void updateExecutedDonation(int donationId) throws IOException {
        HttpPatch post = new HttpPatch(urlRoot + "/" + donationId);

        post.addHeader("pass-key", passKey);
        post.addHeader(HttpHeaders.USER_AGENT, "Googlebot");

        // add request parameter, form parameters
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("success", "1"));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {

            // JSON result
            String result = EntityUtils.toString(response.getEntity());
            debug("updateExecutedDonation res: \n" + result);
        }
    }

    private void debug(String s) {
        if (!plugin.getConfig().getBoolean("debug", false)) {
            return;
        }
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        console.sendMessage(ChatColor.DARK_GRAY + "[CustomDonation][debug] " + s);
    }

    private void error(String s) {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        console.sendMessage(ChatColor.DARK_RED + "[CustomDonation][error] " + s);
    }

    private void log(String s) {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        console.sendMessage("[CustomDonation] " + s);
    }
}