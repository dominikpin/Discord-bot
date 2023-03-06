package org.example.events;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class FourInARowEvent extends ListenerAdapter{
    private static String[][] board = new String[6][9];
    private static String BOT_ID;
    private static String GUILD_ID;
    private static String FOURINAROW_CHANNEL_ID;
    private static String PREFIX; 
    private static Guild guild;
    private static TextChannel fourInARowChannel;


    public FourInARowEvent(String GUILD_ID, String FOURINAROW_CHANNEL_ID, String BOT_ID) {
        FourInARowEvent.GUILD_ID = GUILD_ID;
        FourInARowEvent.FOURINAROW_CHANNEL_ID = FOURINAROW_CHANNEL_ID;
        FourInARowEvent.BOT_ID = BOT_ID;
    }

    // Define a static method for updating the command prefix
    public static void updatePrefix(String newPrefix) {
        PREFIX = newPrefix;
    }

    @Override
    public void onReady(ReadyEvent event) {
        guild = event.getJDA().getGuildById(GUILD_ID);
        fourInARowChannel = guild.getTextChannelById(FOURINAROW_CHANNEL_ID);
    }
}
