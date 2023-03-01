package org.example.events;

import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ShutdownBotCommand extends ListenerAdapter {

    // Declaring PREFIX and OWNER_ID as class variables
    private static String PREFIX;
    private final String OWNER_ID;

    // Constructor that initializes the value of OWNER_ID
    public ShutdownBotCommand(String OWNER_ID) {
        this.OWNER_ID = OWNER_ID;
    }

    // Method that updates the value of PREFIX
    public static void updatePrefix(String newPrefix) {
        PREFIX = newPrefix;
    }

    // Overriding the onMessageReceived method to handle incoming messages
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Checking if the message was sent by the bot's owner and if the content of the message is the shutdown command
        if (event.getAuthor().getId().equals(OWNER_ID) && event.getMessage().getContentRaw().equals(PREFIX + "shutdown")) {
            System.out.println("Shutting down...");
            CompletableFuture<Message> future = event.getChannel().sendMessage("Shutting down...").submit();
            future.join();
            // Shutting down the JDA instance and exiting the program
            event.getJDA().shutdown();
            System.exit(0);
        }
    }
}
