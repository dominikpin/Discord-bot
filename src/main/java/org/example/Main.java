package org.example;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.example.events.*;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;
import java.io.IOException;

public class Main {
    
    // Load environment variables from the .env file
    private static Dotenv dotenv = Dotenv.load();

    // Define constants from environment variables
    private static final String LOCK_FILE_PATH = dotenv.get("LOCK_FILE_PATH");
    private static final String TOKEN = dotenv.get("DISCORD_TOKEN");
    private static final String AFK_CHANNEL_ID = dotenv.get("AFK_CHANNEL_ID");
    private static final String TRIVIA_CHANNERL_ID = dotenv.get("TRIVIA_CHANNERL_ID");
    private static final String TICTACTOE_CHANNERL_ID = dotenv.get("TICTACTOE_CHANNERL_ID");
    private static final String GUILD_ID = dotenv.get("GUILD_ID");
    private static final String API_KEY = dotenv.get("API_KEY");
    private static final String OWNER_ID = dotenv.get("OWNER_ID");
    private static final String BOT_ID = dotenv.get("BOT_ID");
    private static final String ZALBIK_ID = dotenv.get("ZALBIK_ID");
    private static final String PREFIX_FILE_PATH = dotenv.get("PREFIX_FILE_PATH");

    // Main method
    public static void main(String[] args) {
        // Create a lock file to prevent multiple instances of the bot from running
        File lockFile = new File(LOCK_FILE_PATH);
        if (lockFile.exists()) {
            System.out.println("Another instance of the bot is already running.");
            return;
        }

        try {
            lockFile.createNewFile();
            System.out.println("Lock file created.");
        } catch (IOException e) {
            System.out.println("Failed to create lock file.");
            return;
        }

        // Build JDA client with necessary intents and event listeners
        JDABuilder jdaBuilder = JDABuilder.createDefault(TOKEN);
        jdaBuilder
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(new ReadyEventListener(), new UrbanDictionaryBotEvent(API_KEY), new DeafenListener(ZALBIK_ID), new AFKListener(AFK_CHANNEL_ID, GUILD_ID),  new ShutdownBotCommand(OWNER_ID), new TriviaQuizEvent(API_KEY, GUILD_ID, TRIVIA_CHANNERL_ID), new PrefixChangeEvent(PREFIX_FILE_PATH), new TicTacToeEvent(GUILD_ID, TICTACTOE_CHANNERL_ID, BOT_ID))
                .build();

        // Delete lock file when the bot is shut down
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!(lockFile.delete())) {
                System.out.println("Failed to delete lock file.");
            }
            System.out.println("Lock file deleted.");
        }));
    }
}
