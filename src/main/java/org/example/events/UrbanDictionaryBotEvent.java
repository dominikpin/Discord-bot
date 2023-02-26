package org.example.events;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class UrbanDictionaryBot extends ListenerAdapter {

    private static final String API_URL = "https://mashape-community-urban-dictionary.p.rapidapi.com/define?term=";
    private static final String API_KEY = "acbab4be1amshdb0245055ce3333p194689jsn0c98dd77baa6";

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split("\\s+");
        if (message[0].equalsIgnoreCase("!define")) {
            if (message.length < 2) {
                event.getChannel().sendMessage("Please specify a word to define.").queue();
                return;
            }
            String word = message[1];
            String definition = getDefinition(word);
            if (definition == null) {
                event.getChannel().sendMessage("Sorry, I couldn't find a definition for that word.").queue();
            } else {
                event.getChannel().sendMessage("**" + word + "**: " + definition).queue();
            }
        }
    }

    private String getDefinition(String word) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + word))
                .header("X-RapidAPI-Key", API_KEY)
                .header("X-RapidAPI-Host", "mashape-community-urban-dictionary.p.rapidapi.com")
                .build();
        try {
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonResponse = new JSONObject(httpResponse.body());
            JSONArray definitionsArray = jsonResponse.getJSONArray("list");
            if (definitionsArray.length() == 0) {
                return null;
            } else {
                JSONObject firstDefinition = definitionsArray.getJSONObject(0);
                String definition = firstDefinition.getString("definition");
                JSONArray exampleArray = firstDefinition.getJSONArray("example");
                List<String> exampleList = new ArrayList<>();
                for (int i = 0; i < exampleArray.length(); i++) {
                    exampleList.add(exampleArray.getString(i));
                }

                String examples = String.join("\n", exampleList);
                return definition + "\n\nExamples:\n" + examples;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
