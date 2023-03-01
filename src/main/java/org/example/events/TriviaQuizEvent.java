package org.example.events;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Timer;
import java.util.TimerTask;

import org.example.Main;

import java.util.Random;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

public class TriviaQuizEvent extends ListenerAdapter {

    private static final String API_URL = "https://trivia-by-api-ninjas.p.rapidapi.com/v1/trivia?";
    private static final String API_KEY = Main.API_KEY;
    private boolean triviaActive = false;
    private boolean found = false;
    private TextChannel currentTriviaChannel = null;
    private String currentCategory = null;
    private String[] possibleTriviaCategorys = {"artliterature", "language", "sciencenature", "general", "fooddrink", "peopleplaces", "geography", "historyholidays", "entertainment", "toysgames", "music", "mathematics", "religionmythology", "sportsleisure", "random"};
    private static String PREFIX = PrefixChangeEvent.PREFIX;
    private int numberOfQuestionsStart = 0;
    private int numberOfQuestionsCorrect = 0;
    private int numberOfQuestions = 0;

    public static void updatePrefix(String newPrefix) {
        PREFIX = newPrefix;
    }

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
        if (message[0].equals(PREFIX + "trivia")) {
            checkWhichTriviaCommand(event, message);
            return;
        }
        //System.out.println("0");
        //System.out.println(currentTriviaChannel.equals(event.getChannel().asTextChannel()));
        if (triviaActive && currentTriviaChannel.equals(event.getChannel().asTextChannel())) {
            //System.out.println("1");
            String userAnswer = event.getMessage().getContentRaw().trim();
            if (userAnswer.equalsIgnoreCase(trivia[1])) {
                currentTriviaChannel.sendMessage("**The answer " + trivia[1] + " is correct**").queue();
                task1.cancel();
                numberOfQuestionsCorrect++;
                endGame(event);
                //System.out.println("2");
            }
        }
    }

    public void checkWhichTriviaCommand(MessageReceivedEvent event, String[] message) {
        if (message.length == 3) {
            for (String string : possibleTriviaCategorys) {
                if (string.equals(message[1])) {
                    currentCategory = message[1];
                    found = true;
                    break;
                }
            }
            if (found) {
                if (!triviaActive) {
                    if (message[2].chars().allMatch(Character::isDigit)) {
                        if (Integer.parseInt(message[2]) > 0) {
                            try {
                                numberOfQuestions = Integer.parseInt(message[2]);
                                numberOfQuestionsStart = Integer.parseInt(message[2]);
                                trivia = getTrivia(currentCategory);
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                            startGame(event);
                        } else {

                        }
                    } else {
                        currentTriviaChannel.sendMessage("Sorry," + message[2] + "is not a number").queue();
                    }
                } else {
                    currentTriviaChannel.sendMessage("**Trivia is already in progress**").queue();
                }
            } else {
                currentTriviaChannel.sendMessage("Sorry, I couldn't find the cathegory " + message[1]).queue();
            }
        } else if (message.length == 2) {
            if (message[1].equals("stop")) {
                if (triviaActive) {
                    currentTriviaChannel.sendMessage("**The answer is " + trivia[1] + "**").queue();
                    currentTriviaChannel.sendMessage("**Trivia Stoped**").queue();
                    numberOfQuestions = 0;
                    endGame(event);
                    task1.cancel();
                    return;
                } else {
                    currentTriviaChannel.sendMessage("**There is no trivia in progress**").queue();
                    return;
                }
            }
            if (message[1].equals("help")) {
                String helpMessage = String.format("Here are the available commands for %strivia:\n\n%strivia [category (default - random)] [number of games (default - 3)]- Starts a new trivia game. These are the categories: *artliterature, language, sciencenature, general, fooddrink, peopleplaces, geography, historyholidays, entertainment, toysgames, music, mathematics, religionmythology, sportsleisure.*\n%strivia next - Ends the current trivia question and asks a new question in the same trivia game. If there are no more questions in current trivia game it starts a new one (with a previous category).\n%strivia stop - Stops the current trivia game.\n%strivia help - Shows this help message.\n([] are not needed in commands)\n\nDuring a trivia game, you can answer questions by typing your answer in the chat. The bot will let you know if your answer is correct or incorrect.\n\nGood luck and have fun!", PREFIX, PREFIX, PREFIX, PREFIX, PREFIX);
                currentTriviaChannel.sendMessage(helpMessage).queue();
                return;
            }
            if (message[1].equals("next")) {
                if (triviaActive) {
                    triviaActive = false;
                    currentTriviaChannel.sendMessage("**The answer is " + trivia[1] + "**").queue();
                    //System.out.println(numberOfQuestions);
                    if (numberOfQuestions == 0) {
                        currentTriviaChannel.sendMessage("**Ending previous trivia game and stating new one.**").queue();
                        currentTriviaChannel.sendMessage("**Your score was " + numberOfQuestionsCorrect + "/" + numberOfQuestionsStart + "**").queue();
                        numberOfQuestionsCorrect = 0;
                        task1.cancel();
                        String[] msg = {PREFIX + "trivia", currentCategory, numberOfQuestionsStart + ""};
                        checkWhichTriviaCommand(event, msg);
                        return;
                    } else {
                        currentTriviaChannel.sendMessage("**Ending previous trivia question and sending a new one.**").queue();
                        task1.cancel();
                        endGame(event);
                        return;
                    }

                } else {
                    currentTriviaChannel.sendMessage("**There is no trivia in progress**").queue();
                    return;
                }
            }
            if (message[1].chars().allMatch(Character::isDigit)) {
                if (!triviaActive) {
                    try {
                        currentCategory = "random";
                        numberOfQuestions = Integer.parseInt(message[1]);
                        numberOfQuestionsStart = Integer.parseInt(message[1]);
                        trivia = getTrivia(currentCategory);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    startGame(event);
                    return;
                } else {
                    currentTriviaChannel.sendMessage("**Trivia is already in progress**").queue();
                    return;
                }
            }
            for (String string : possibleTriviaCategorys) {
                if (string.equals(message[1])) {
                    currentCategory = message[1];
                    found = true;
                    break;
                }
            }
            if (found) {
                if (!triviaActive) {
                    try {
                        numberOfQuestions = 3;
                        numberOfQuestionsStart = 3;
                        trivia = getTrivia(currentCategory);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    startGame(event);
                } else {
                    currentTriviaChannel.sendMessage("**Trivia is already in progress**").queue();
                }
            } else {
                currentTriviaChannel.sendMessage("Sorry, I couldn't find the cathegory " + message[1]).queue();
            }
        } else if (message.length == 1) {
            if (!triviaActive) {
                try {
                    currentCategory = "random";
                    numberOfQuestions = 3;
                    numberOfQuestionsStart = 3;
                    trivia = getTrivia(currentCategory);
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
        if (category.equals("random")) {
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
        int randomNumber = rand.nextInt(30);
        String question = new Gson().fromJson(response.body(), JsonArray.class).get(randomNumber).getAsJsonObject().get("question").getAsString();
        String answer = new Gson().fromJson(response.body(), JsonArray.class).get(randomNumber).getAsJsonObject().get("answer").getAsString();
        String[] Question = {question, answer};
        //System.out.println(answer);
        return Question;
    }

    public void startGame(MessageReceivedEvent event) {
        numberOfQuestions--;
        currentTriviaChannel = event.getChannel().asTextChannel();
        if (trivia == null) {
            currentTriviaChannel.sendMessage("Sorry, I couldn't find any trivia").queue();
        } else {
            triviaActive = true;
            currentTriviaChannel.sendMessage("**" + trivia[0] + " " + (numberOfQuestionsStart-numberOfQuestions) + "/" + numberOfQuestionsStart + "**").queue();
            //System.out.println(trivia[1]);
            task1 = new TimerTask() {
                int TimeLeft = 31;
                String sentMessageId = "";
                @Override
                public void run() {
                    TimeLeft--;
                    if (TimeLeft == 30) {
                        currentTriviaChannel.sendMessage(TimeLeft + " second/s left").queue(message -> {
                            sentMessageId = message.getId();
                        });
                    } else if (sentMessageId != "") {
                        currentTriviaChannel.editMessageById(sentMessageId, TimeLeft + " second/s left").queue();
                    }
                    if (TimeLeft == 0) {
                        currentTriviaChannel.sendMessage("**No time left, the answer is " + trivia[1] + "**").queue();
                        endGame(event);
                        cancel();
                    }
                }
            };
            timer.schedule(task1, 0, 1000);
        }
    }

    TimerTask task1 = new TimerTask() {
        @Override
        public void run() {}
    };

    public void endGame(MessageReceivedEvent event) {
        if (numberOfQuestions > 0) {
            try {
                trivia = getTrivia(currentCategory);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            startGame(event);
        } else {
            currentTriviaChannel.sendMessage("**Your score was " + numberOfQuestionsCorrect + "/" + numberOfQuestionsStart + "**").queue();
            numberOfQuestionsStart = 0;
            numberOfQuestionsCorrect = 0;
            currentTriviaChannel = null;
            triviaActive = false;
            currentCategory = null;
            found = false;
        }
    }
}