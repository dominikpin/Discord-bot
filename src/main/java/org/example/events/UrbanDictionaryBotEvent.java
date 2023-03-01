package org.example.events;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class UrbanDictionaryBotEvent extends ListenerAdapter {

    // Define constant variables for the API_URL, API_KEY, PREFIX
    private static final String API_URL = "https://urban-dictionary7.p.rapidapi.com/v0/define?term=";
    private static String API_KEY;
    private static String PREFIX;

    // Constructor that initializes the value of API_KEY
    public UrbanDictionaryBotEvent(String API_KEY) {
        UrbanDictionaryBotEvent.API_KEY = API_KEY;
    }

    // Define a static method for updating the command prefix
    public static void updatePrefix(String newPrefix) {
        PREFIX = newPrefix;
    }

    // Overriding the onMessageReceived method to handle incoming messages
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Splitting the message into an array of strings
        String[] message = event.getMessage().getContentRaw().split("\\s+");
        // Checking if the first string in the message array is the define command
        if (!(message[0].equals(PREFIX + "define"))){
            return;
        }
        // Checking if the message includes the word to define
        if (message.length < 2) {
            event.getChannel().sendMessage("Please specify a word to define.").queue();
            return;
        }
        String word = message[1];
        String definition = null;
        // Call the getDefinition method to retrieve the definition for the word
        try {
            definition = getDefinition(word);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        // If a definition is found, send it in a message to the channel
        if (definition != null) {
            event.getChannel().sendMessage("**" + word + "**: " + definition).queue();
        }
        event.getChannel().sendMessage("Sorry, I couldn't find a definition for that word.").queue();
    }

    // Method for retrieving the definition for a given word from API
    private String getDefinition(String word) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + word))
                .header("X-RapidAPI-Key", API_KEY)
                .header("X-RapidAPI-Host", "urban-dictionary7.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        String firstDefinition = new Gson().fromJson(response.body(), JsonObject.class)
        .getAsJsonArray("list").get(0).getAsJsonObject().get("definition").getAsString();

        return firstDefinition;
    }
}
