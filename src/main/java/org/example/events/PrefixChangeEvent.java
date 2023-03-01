package org.example.events;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.example.Main;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class PrefixChangeEvent extends ListenerAdapter{
    private static final String PREFIX_FILE_PATH = Main.PREFIX_FILE_PATH;
    public static String PREFIX;

    @Override
    public void onReady(ReadyEvent event) {
        loadPrefix();
        updatePrefixInOtherClasses(PREFIX);
        //System.out.println(PREFIX);
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split("\\s+");
        if (message[0].equals(PREFIX + "prefix")){
            if (message.length > 1) {
                if(message[1].equals("help")) {
                    String helpMessage = String.format("Here are the available commands for %sprefix:\n\n%sprefix change [wanted prefix] - changes prefix to [wanted prefix]\n%sprefix help - Shows this help message.\n([] are not needed in commands)", PREFIX, PREFIX, PREFIX);
                    event.getChannel().asTextChannel().sendMessage(helpMessage).queue();
                }
                if(message[1].equals("change")) {
                    savePrefix(message[2]);
                    PREFIX = message[2];
                    updatePrefixInOtherClasses(message[2]);
                    event.getChannel().asTextChannel().sendMessage("**Prefix changed to **" + PREFIX).queue();
                }
            }
        }
    }

    // Save prefix to a file
    private static void savePrefix(String newPrefix) {
        File file = new File(PREFIX_FILE_PATH);
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.print(newPrefix);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Load prefix from a file
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

    private static void updatePrefixInOtherClasses(String newPrefix) {
        ShutdownBotCommand.updatePrefix(newPrefix);
        UrbanDictionaryBotEvent.updatePrefix(newPrefix);
        TriviaQuizEvent.updatePrefix(newPrefix);
    }
}
