package org.example;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.example.events.*;

import java.io.File;
import java.io.IOException;

public class Main {
    private static final String LOCK_FILE_PATH = "C:\\Users\\D\\IdeaProjects\\discord-bot\\bot.lock";
    private static final String OWNER_ID = "433690506783096834";

    public static void main(String[] args) {
        // Check for lock file
        File lockFile = new File(LOCK_FILE_PATH);
        if (lockFile.exists()) {
            System.out.println("Another instance of the bot is already running.");
            return;
        }

        // Create lock file
        try {
            lockFile.createNewFile();
            System.out.println("Bot started.");
        } catch (IOException e) {
            System.out.println("Failed to create lock file.");
            return;
        }

        // Start bot
        final String TOKEN = "MTA3NzY3OTc1NzQ0Mjk2MTUzOQ.GDhpas.UZEQEpQB8UplKI5hVzTVssraKZSIDwP-qyFkb8";
        JDABuilder jdaBuilder = JDABuilder.createDefault(TOKEN);
        jdaBuilder
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(new ReadyEventListener(), new UrbanDictionaryBotEvent(), new DeafenListener(), new AFKListener(),  new ShutdownBotCommand(OWNER_ID))
                .build();

        // Delete lock file on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (lockFile.delete()) {
                System.out.println("Lock file deleted.");
            } else {
                System.out.println("Failed to delete lock file.");
            }
        }));
    }
}
