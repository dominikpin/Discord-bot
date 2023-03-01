package org.example;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.example.events.*;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.io.IOException;

public class Main {
    // Load environment variables from .env file
    private static Dotenv dotenv = Dotenv.load();

    private static final String LOCK_FILE_PATH = dotenv.get("LOCK_FILE_PATH");
    private static final String OWNER_ID = dotenv.get("OWNER_ID");
    private static final String TOKEN = dotenv.get("DISCORD_TOKEN");
    public static final String AFK_CHANNEL_ID = dotenv.get("AFK_CHANNEL_ID");
    public static final String GUILD_ID = dotenv.get("GUILD_ID");
    public static final String API_KEY = dotenv.get("API_KEY");
    public static final String ZALBIK_ID = dotenv.get("ZALBIK_ID");
    public static final String PREFIX_FILE_PATH = dotenv.get("PREFIX_FILE_PATH");


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
        JDABuilder jdaBuilder = JDABuilder.createDefault(TOKEN);
        jdaBuilder
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(new ReadyEventListener(), new UrbanDictionaryBotEvent(), new DeafenListener(), new AFKListener(),  new ShutdownBotCommand(OWNER_ID), new TriviaQuizEvent(), new PrefixChangeEvent())
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
