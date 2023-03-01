package org.example.events;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class PrefixChangeEvent extends ListenerAdapter{
    // Declaring PREFIX_FILE_PATH and PREFIX as class variables
    private static String PREFIX_FILE_PATH;
    private static String PREFIX;

    // Constructor that initializes the value of PREFIX_FILE_PATH
    public PrefixChangeEvent(String PREFIX_FILE_PATH) {
        PrefixChangeEvent.PREFIX_FILE_PATH = PREFIX_FILE_PATH;
    }

    // Overriding the onReady method to load the prefix and update it in other classes
    @Override
    public void onReady(ReadyEvent event) {
        loadPrefix();
        updatePrefixInOtherClasses(PREFIX);
    }
    
    // Overriding the onMessageReceived method to handle incoming messages
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Splitting the message into an array of strings
        String[] message = event.getMessage().getContentRaw().split("\\s+");
        // Checking if the first string in the message array is the prefix command
        if (!(message[0].equals(PREFIX + "prefix"))){
            return;
        }
        // Checking if the message includes the change or help command
        if (message.length < 2) {
            event.getChannel().asTextChannel().sendMessage("**Wrong command**").queue();
            return;
        }
        switch (message[1]) {
            // Handling the help command
            case "help":
                String helpMessage = String.format("Here are the available commands for %sprefix:\n\n" + 
                "%sprefix change [wanted prefix] - changes prefix to [wanted prefix]\n" + 
                "%sprefix help - Shows this help message.\n" + 
                "([] are not needed in commands)", PREFIX, PREFIX, PREFIX);
                event.getChannel().asTextChannel().sendMessage(helpMessage).queue();
                return;
            // Handling the change command
            case "change":
                // Checking if the message includes new prefix
                if (message.length != 3) {
                    event.getChannel().asTextChannel().sendMessage("**Wrong command**").queue();
                    return;
                }
                savePrefix(message[2]);
                PREFIX = message[2];
                updatePrefixInOtherClasses(message[2]);
                event.getChannel().asTextChannel().sendMessage("**Prefix changed to **" + PREFIX).queue();
                return;
        }
    }

    // Method that saves the new prefix to a file
    private static void savePrefix(String newPrefix) {
        File file = new File(PREFIX_FILE_PATH);
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.print(newPrefix);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Method that loads the prefix from a file
    private static void loadPrefix() {
        File file = new File(PREFIX_FILE_PATH);
        if (file.exists()) {
            try (Scanner scanner = new Scanner(file)) {
                PREFIX = scanner.next();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // Method that updates the prefix in other classes
    private static void updatePrefixInOtherClasses(String newPrefix) {
        ShutdownBotCommand.updatePrefix(newPrefix);
        UrbanDictionaryBotEvent.updatePrefix(newPrefix);
        TriviaQuizEvent.updatePrefix(newPrefix);
    }
}
