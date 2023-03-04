package org.example.events;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TriviaQuizEvent extends ListenerAdapter {

    // Define constant variables for the API_URL, API_KEY, PREFIX, POSSIBLE_CATEGORIES
    private static final String API_URL = "https://trivia-by-api-ninjas.p.rapidapi.com/v1/trivia?";
    private static String API_KEY;
    private static String GUILD_ID;
    private static String TRIVIA_CHANNERL_ID;
    private static String PREFIX;
    private static Guild guild;
    private TextChannel triviaChannel;
    private static final String[] POSSIBLE_CATEGORIES = {
            "artliterature", "language", "sciencenature", "general", "fooddrink", "peopleplaces",
            "geography", "historyholidays", "entertainment", "toysgames", "music", "mathematics",
            "religionmythology", "sportsleisure", "random"
    };

    // Create random object for later use
    private static Random rand = new Random();

    // Define variables
    private boolean isTriviaActive = false;
    private boolean found = false;
    private String currentCategory = null;
    private int numberOfStartingQuestions = 0;
    private int numberOfCorrectQuestions = 0;
    private int numberOfQuestions = 0;
    private Timer timer = new Timer();
    private String[] trivia = new String[]{null, null};

    // Constructor that initializes the value of API_KEY
    public TriviaQuizEvent(String API_KEY, String GUILD_ID, String TRIVIA_CHANNERL_ID) {
        TriviaQuizEvent.API_KEY = API_KEY;
        TriviaQuizEvent.GUILD_ID = GUILD_ID;
        TriviaQuizEvent.TRIVIA_CHANNERL_ID = TRIVIA_CHANNERL_ID;
    }

    // Define a static method for updating the command prefix
    public static void updatePrefix(String newPrefix) {
        PREFIX = newPrefix;
    }

    // Method is called when the bot is ready
    @Override
    public void onReady(ReadyEvent event) {
        guild = event.getJDA().getGuildById(GUILD_ID);
        triviaChannel = guild.getTextChannelById(TRIVIA_CHANNERL_ID);
    }

    // Overriding the onMessageReceived method to handle incoming messages
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(!event.getChannel().asTextChannel().equals(triviaChannel)){
            return;
        }
        if (event.getMember().getUser().isBot()) {
            return;
        }
        // Splitting the message into an array of strings
        String[] message = event.getMessage().getContentRaw().split("\\s+");
        // Checking if the first string in the message array is the trivia command
        if (message[0].equals(PREFIX + "trivia")) {
            handleTriviaCommand(message);
            return;
        // Checking if user inputed the right answer in the right text channel
        } else if (isTriviaActive) {
            String userAnswer = event.getMessage().getContentRaw().trim();
            if (userAnswer.equalsIgnoreCase(trivia[1])) {
                triviaChannel.sendMessage("**The answer " + trivia[1] + " is correct**").queue();
                numberOfCorrectQuestions++;
                isTriviaActive = false;
                endQuestion();
            }
        }
    }

    // Method handles trivia comands
    public void handleTriviaCommand(String[] message) {
        switch (message.length) {
            // Handles case of !trivia
            case 1:
                // Checks if there is any active trivia and doesn't start a new game
                if (isTriviaActive) {
                    triviaChannel.sendMessage("**Trivia is already in progress**").queue();
                    return;
                }
                numberOfQuestions = 3;
                numberOfStartingQuestions = 3;
                currentCategory = "random";
                tryStartGame();
                return;    
            // Handles case of !trivia [number, stop, help, next, category]
            case 2:
                // Handles case !trivia number
                if (isValidNumber(message[1])) {
                    // Checks if there is any active trivia and doesn't start a new game
                    if (isTriviaActive) {
                        triviaChannel.sendMessage("**Trivia is already in progress**").queue();
                        return;
                    }
                    numberOfQuestions = Integer.parseInt(message[1]);
                    numberOfStartingQuestions = Integer.parseInt(message[1]);
                    currentCategory = "random";
                    tryStartGame();
                    return;
                }
                switch (message[1]) {
                    // Handles case !trivia stop
                    case "stop":
                        if (!isTriviaActive) {
                            triviaChannel.sendMessage("**There is no trivia in progress**").queue();
                            return;
                        }
                        triviaChannel.sendMessage("**The answer is " + trivia[1] + "**").queue();
                        triviaChannel.sendMessage("**Trivia Stoped**").queue();
                        numberOfQuestions = 0;
                        endQuestion();
                        return;
                        // Handles case !trivia help
                    case "help":
                        String helpMessage = String.format("Here are the available commands for %strivia:\n\n" +
                        "%strivia [category (default - random)] [number of games (default - 3)]- Starts a new trivia game. " +
                        "These are the categories: *%s.*\n%strivia next - Ends the current trivia question and asks a new question " +
                        "in the same trivia game. If there are no more questions in current trivia game it starts a new one (with a previous category).\n" +
                        "%strivia stop - Stops the current trivia game.\n%strivia help - Shows this help message.\n" +
                        "([] are not needed in commands)\n\nDuring a trivia game, you can answer questions by typing your answer " +
                        "in the chat. The bot will let you know if your answer is correct or incorrect.\n\n" + 
                        "Good luck and have fun!", PREFIX, PREFIX, Arrays.deepToString(POSSIBLE_CATEGORIES), PREFIX, PREFIX, PREFIX);
                        triviaChannel.sendMessage(helpMessage).queue();
                        return;
                        // Handles case !trivia next
                    case "next":
                        if (!isTriviaActive) {
                            triviaChannel.sendMessage("**There is no trivia in progress**").queue();
                            return;
                        }
                        isTriviaActive = false;
                        triviaChannel.sendMessage("**The answer is " + trivia[1] + "**").queue();
                        // If number of questions left in a trivia game are more than 0 it skips to the next question
                        if (numberOfQuestions > 0) {
                            triviaChannel.sendMessage("**Ending previous trivia question and sending a new one.**").queue();
                            endQuestion();
                            return;
                        }
                        // If number of questions left in a trivia game are less than 1 it stops the current game and starts a new one with previos settings
                        triviaChannel.sendMessage("**Ending previous trivia game and stating new one.**").queue();
                        triviaChannel.sendMessage("**Your score was " + numberOfCorrectQuestions + "/" + numberOfStartingQuestions + "**").queue();
                        task1.cancel();
                        numberOfCorrectQuestions = 0;
                        numberOfQuestions = numberOfStartingQuestions;
                        tryStartGame();
                        return;
                        // Handles case !trivia category
                    default:
                        // Checks if there is any active trivia and doesn't start a new game
                        if (isTriviaActive) {
                            triviaChannel.sendMessage("**Trivia is already in progress**").queue();
                            return;
                        }
                        if (!isValidCategory(message[1])) {
                            return;
                        }
                        numberOfQuestions = 3;
                        numberOfStartingQuestions = 3;
                        currentCategory = message[1];
                        tryStartGame();
                        return;
                }   
            // Handles case !trivia category number
            case 3:
                // Checks if there is any active trivia and doesn't start a new game
                if (isTriviaActive) {
                    triviaChannel.sendMessage("**Trivia is already in progress**").queue();
                    return;
                }
                if (!isValidCategory(message[1])) {
                    return;
                }
                if (!isValidNumber(message[2])) {
                    return;
                }
                numberOfQuestions = Integer.parseInt(message[2]);
                numberOfStartingQuestions = Integer.parseInt(message[2]);
                currentCategory = message[1];
                tryStartGame();
                return;
            // Handles any mistyped/wrong commands
            default:
                triviaChannel.sendMessage("**Wrong command**").queue();
                return;
        }
    }

    // Method checks if number is valid
    public boolean isValidNumber(String number) {
        if ((number.chars().allMatch(Character::isDigit))) {
            if (Integer.parseInt(number) < 1) {
                triviaChannel.sendMessage("Sorry," + number + "is number smaller than 1").queue();
                return false;
            }
            return true;
        }
        return false;
    }

    // Method checks if category is valid
    public boolean isValidCategory(String category) {
        for (String string : POSSIBLE_CATEGORIES) {
            if (string.equals(category)) {
                currentCategory = category;
                found = true;
                break;
            }
        }
        if (!found) {
            triviaChannel.sendMessage("Sorry, I couldn't find the cathegory " + category).queue();
        }
        return found;
    }

    // Method calls method startGame if it gets trivia
    public void tryStartGame() {
        try {
            trivia = getTrivia(currentCategory);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        startGame();
    }

    // Method ends a trivia question or game depending on number of questions left in a game
    public void endQuestion() {
        task1.cancel();
        if (numberOfQuestions < 1) {
            triviaChannel.sendMessage("**Your score was " + numberOfCorrectQuestions + "/" + numberOfStartingQuestions + "**").queue();
            trivia[0] = null;
            trivia[1] = null;
            numberOfStartingQuestions = 0;
            numberOfCorrectQuestions = 0;
            isTriviaActive = false;
            currentCategory = null;
            found = false;
            return;
        }
        tryStartGame();
    }

    // Method starts trivia game
    public void startGame() {
        // Checks if trivia got retrived from an API
        if (trivia == null) {
            triviaChannel.sendMessage("Sorry, I couldn't find any trivia").queue();
        }
        numberOfQuestions--;
        isTriviaActive = true;
        // Asks a new trivia question and starts a countdown from 30
        triviaChannel.sendMessage("**" + trivia[0] + " " + (numberOfStartingQuestions-numberOfQuestions) + "/" + numberOfStartingQuestions + "**").queue();
        task1 = new TimerTask() {
            int TimeLeft = 30;
            String sentMessageId = "";
            @Override
            public void run() {
                if (TimeLeft == 30) {
                    triviaChannel.sendMessage(TimeLeft + " second/s left").queue(message -> {
                        sentMessageId = message.getId();
                    });
                } else if (sentMessageId != "") {
                    triviaChannel.editMessageById(sentMessageId, TimeLeft + " second/s left").queue();
                }
                if (TimeLeft == 0) {
                    triviaChannel.sendMessage("**No time left, the answer is " + trivia[1] + ".**").queue();
                    isTriviaActive = false;
                    endQuestion();
                }
                TimeLeft--;
            }
        };
        timer.schedule(task1, 0, 1000);
    }

    // Method allows to reuse task1
    TimerTask task1 = new TimerTask() {
        @Override
        public void run() {}
    };

    // Method for retrieving the the trivia for a given category
    private static String[] getTrivia(String category) throws IOException, InterruptedException {
        String currentURL = API_URL;
        if (category.equals("random")) {
            currentURL += "limit=30";
        } else {
            currentURL += "category=" + category + "&limit=30";
        }
        HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(currentURL))
        .header("X-RapidAPI-Key", API_KEY)
        .header("X-RapidAPI-Host", "trivia-by-api-ninjas.p.rapidapi.com")
        .method("GET", HttpRequest.BodyPublishers.noBody())
        .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        int randomNumber = rand.nextInt(30);
        JsonObject questionAndAnswer = new Gson().fromJson(response.body(), JsonArray.class).get(randomNumber).getAsJsonObject();
        String[] Question = {questionAndAnswer.get("question").getAsString(), questionAndAnswer.get("answer").getAsString()};
        return Question;
    }
}