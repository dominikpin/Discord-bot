package org.example.events;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class UrbanDictionaryBotEvent extends ListenerAdapter {

    private static final String API_URL = "https://urban-dictionary7.p.rapidapi.com/v0/define?term=";
    private static final String API_KEY = "386dd7fef6mshc7279ae2ec65aafp1cdec1jsnc2a2d0355e3a";

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split("\\s+");
        if (message[0].equalsIgnoreCase("!define")) {
            if (message.length < 2) {
                event.getChannel().sendMessage("Please specify a word to define.").queue();
                return;
            }
            String word = message[1];
            //System.out.println(word);
            String definition = null;
            try {
                definition = getDefinition(word);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            //System.out.println(definition);
            if (definition == null) {
                event.getChannel().sendMessage("Sorry, I couldn't find a definition for that word.").queue();
            } else {
                event.getChannel().sendMessage("**" + word + "**: " + definition).queue();
            }
        }
    }

    private String getDefinition(String word) throws IOException, InterruptedException {


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + word))
                .header("X-RapidAPI-Key", API_KEY)
                .header("X-RapidAPI-Host", "urban-dictionary7.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        //System.out.println(response.body());
        JsonObject jsonObject = new Gson().fromJson(response.body(), JsonObject.class);
        JsonArray list = jsonObject.getAsJsonArray("list");
        JsonObject firstItem = list.get(0).getAsJsonObject();
        String firstDefinition = firstItem.get("definition").getAsString();

        // Get the "definition" value from the first object in the "list" array

        return firstDefinition;
    }
}
