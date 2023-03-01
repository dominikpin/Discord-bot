package org.example.events;

import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.Message;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ShutdownBotCommand extends ListenerAdapter {
    private static String PREFIX = PrefixChangeEvent.PREFIX;
    private final String ownerId;

    public ShutdownBotCommand(String ownerId) {
        this.ownerId = ownerId;
    }

    public static void updatePrefix(String newPrefix) {
        PREFIX = newPrefix;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().getId().equals(ownerId) && event.getMessage().getContentRaw().equals(PREFIX + "shutdown")) {
            System.out.println("Shutting down...");
            CompletableFuture<Message> future = event.getChannel().sendMessage("Shutting down...").submit();
            future.join();
            event.getJDA().shutdown();
            System.exit(0);
        }
    }
}
