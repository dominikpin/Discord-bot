package org.example.events;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

public class TriviaQuizEvent extends ListenerAdapter {

    private static final String API_URL = "https://trivia-by-api-ninjas.p.rapidapi.com/v1/trivia?";
    private static final String API_KEY = "386dd7fef6mshc7279ae2ec65aafp1cdec1jsnc2a2d0355e3a";
    private boolean triviaActive = false;
    private boolean found = false;
    private TextChannel currentTriviaChannel = null;
    private String[] possibleTriviaCategorys = {"artliterature", "language", "sciencenature", "general", "fooddrink", "peopleplaces", "geography", "historyholidays", "entertainment", "toysgames", "music", "mathematics", "religionmythology", "sportsleisure"};

    public Timer timer = new Timer();
    public static Random rand = new Random();
    public String[] trivia = new String[] {null, null};
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        currentTriviaChannel = event.getChannel().asTextChannel();
        if (event.getMember().getUser().isBot()) {
            return;
        }
        String[] message = event.getMessage().getContentRaw().split("\\s+");
        if (message[0].equals("!trivia")) {
            checkIfTriviaStart(event, message);
            return;
        }
        //System.out.println("0");
        //System.out.println(currentTriviaChannel.equals(event.getChannel().asTextChannel()));
        if (triviaActive && currentTriviaChannel.equals(event.getChannel().asTextChannel())) {
            //System.out.println("1");
            String userAnswer = event.getMessage().getContentRaw().trim();
            if (userAnswer.equalsIgnoreCase(trivia[1])) {
                currentTriviaChannel.sendMessage("**Correct!**").queue();
                task1.cancel();
                triviaActive = false;
                currentTriviaChannel = null;
                found = false;
                //System.out.println("2");
            }
        }
    }

    public void checkIfTriviaStart(MessageReceivedEvent event, String[] message) {
        if (message.length == 2) {
            if (message[1].equals("stop")) {
                if (triviaActive) {
                    currentTriviaChannel.sendMessage("**Trivia Stoped**").queue();
                    triviaActive = false;
                    currentTriviaChannel = null;
                    found = false;
                    task1.cancel();
                    return;
                } else {
                    currentTriviaChannel.sendMessage("**There is no trivia in progress**").queue();
                    return;
                }
            }
            if (message[1].equals("help")) {
                currentTriviaChannel.sendMessage("Here are the available commands for !trivia:\n\n!trivia - Starts a new trivia game and chooses random category.\n!trivia [category] - Starts a new trivia game. These are the categories: *artliterature, language, sciencenature, general, fooddrink, peopleplaces, geography, historyholidays, entertainment, toysgames, music, mathematics, religionmythology, sportsleisure.*\n!trivia stop - Stops the current trivia game.\n!trivia help - Shows this help message.\n\nDuring a trivia game, you can answer questions by typing your answer in the chat. The bot will let you know if your answer is correct or incorrect.\n\nGood luck and have fun!").queue();
                return;
            }
            if (!triviaActive) {
                for (String string : possibleTriviaCategorys) {
                    if (string.equals(message[1])) {
                        found = true;
                        break;
                    }
                }
                try {
                    if (found) {
                        trivia = getTrivia(message[1]);
                    } else {
                        currentTriviaChannel.sendMessage("Sorry, I couldn't find the cathegory " + message[1]).queue();
                        return;
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                startGame(event);
            } else {
                currentTriviaChannel.sendMessage("**Trivia is already in progress**").queue();
            }
        } else if (message.length == 1) {
            if (!triviaActive) {
                try {
                    trivia = getTrivia("");
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                startGame(event);
            } else {
                currentTriviaChannel.sendMessage("**Trivia is already in progress**").queue();
            }
        } else {
            currentTriviaChannel.sendMessage("**Wrong command**").queue();
        }
    }

    private static String[] getTrivia(String category) throws IOException, InterruptedException {
        String currentURL = API_URL;
        if (category.equals("")) {
            currentURL += "limit=30";
        } else {
            currentURL += "category=" + category + "&limit=30";
        }
        //System.out.println(currentURL);
        HttpRequest request = HttpRequest.newBuilder()
		.uri(URI.create(currentURL))
		.header("X-RapidAPI-Key", API_KEY)
		.header("X-RapidAPI-Host", "trivia-by-api-ninjas.p.rapidapi.com")
		.method("GET", HttpRequest.BodyPublishers.noBody())
		.build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        //System.out.println(response.body());
        int randomNumber = rand.nextInt(30) + 1;
        String question = new Gson().fromJson(response.body(), JsonArray.class).get(randomNumber).getAsJsonObject().get("question").getAsString();
        String answer = new Gson().fromJson(response.body(), JsonArray.class).get(randomNumber).getAsJsonObject().get("answer").getAsString();
        String[] Question = {question, answer};
        //System.out.println(answer);
        return Question;
    }

    public void startGame(MessageReceivedEvent event) {
        currentTriviaChannel = event.getChannel().asTextChannel();
        if (trivia == null) {
            currentTriviaChannel.sendMessage("Sorry, I couldn't find any trivia").queue();
        } else {
            triviaActive = true;
            currentTriviaChannel.sendMessage("**" + trivia[0] + "**").queue();
            //System.out.println(trivia[1]);
            task1 = new TimerTask() {
                int TimeLeft = 30;
                @Override
                public void run() {
                    if (TimeLeft % 5 == 0) {
                        currentTriviaChannel.sendMessage(TimeLeft + " second/s left").queue();
                    }
                    TimeLeft--;
                    if (TimeLeft == 0) {
                        currentTriviaChannel.sendMessage("**No time left, the answer is " + trivia[1] + "**").queue();
                        triviaActive = false;
                        currentTriviaChannel = null;
                        found = false;
                        cancel();
                    }
                }
            };
            timer.schedule(task1, 0, 1000);
        }
    }

    TimerTask task1 = new TimerTask() {
        int TimeLeft = 30;
        @Override
        public void run() {
            if (TimeLeft % 5 == 0) {
                currentTriviaChannel.sendMessage(TimeLeft + " second/s left").queue();
            }
            TimeLeft--;
            if (TimeLeft == 0) {
                currentTriviaChannel.sendMessage("**No time left, the answer is " + trivia[1] + "**").queue();
            }
        }
    };
}