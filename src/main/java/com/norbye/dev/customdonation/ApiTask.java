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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

class ApiTask extends TimerTask {

    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    private String urlRoot = "http://localhost/test/customdonation/api.php";

    public void run() {
        // Try to fetch new status
        // Stay silent unless debug mode is enabled

        // TODO Listen to config file and update code to support api structure
        try {
            fetchActiveDonations();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean fetchActiveDonations() throws IOException {
        HttpGet request = new HttpGet(urlRoot);

        // add request headers
        request.addHeader("custom-key", "eybro");
        request.addHeader(HttpHeaders.USER_AGENT, "Googlebot");

        try (CloseableHttpResponse response = httpClient.execute(request)) {

            // Get HttpResponse Status
            System.out.println(response.getStatusLine().toString());

            HttpEntity entity = response.getEntity();
            Header headers = entity.getContentType();
            System.out.println(headers);

            if (entity != null) {
                // return it as a String
                String result = EntityUtils.toString(entity);
                // JSON result
                System.out.println(result);
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
        return false;
    }

    private boolean executeDonation(int donationId, String username, String[] commands) {
        // TODO Execute commands

        // Update the donation info
        try {
            updateExecutedDonation(donationId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean updateExecutedDonation(int donationId) throws IOException {
        HttpPatch post = new HttpPatch(urlRoot + "?id=" + donationId);

        // add request parameter, form parameters
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("success", "1"));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {

            // JSON result
            System.out.println(EntityUtils.toString(response.getEntity()));
        }
        return false;
    }
}