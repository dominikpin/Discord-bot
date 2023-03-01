package org.example.events;

import net.dv8tion.jda.api.events.guild.voice.GuildVoiceDeafenEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DeafenListener extends ListenerAdapter {
    // Declaring ZALBIK_ID as class variable
    private static String ZALBIK_ID;

    // Constructor that initializes the value of ZALBIK_ID
    public DeafenListener(String ZALBIK_ID) {
        DeafenListener.ZALBIK_ID = ZALBIK_ID;
    }

    @Override
    public void onGuildVoiceDeafen(GuildVoiceDeafenEvent event) {
        // Ignore bot's own deafen events
        if (event.getMember().getUser().isBot()) {
            return;
        }
        // Ignore undeafen events
        if (!(event.getVoiceState().isDeafened())) {
            return;
        }
        // Ignore events from users other than ZALBIK_ID
        if(!(event.getMember().getUser().getId().equals(ZALBIK_ID))) {
            return;
        }
        // Increment the user's nickname with a numeric value
        String currentNickname = event.getMember().getNickname();
        String NewNickname = currentNickname;
        int numericInt = Integer.parseInt(currentNickname.replaceAll("\\D+", ""));
        for (int i = numericInt; i > 0; i/=10) {
            NewNickname = NewNickname.substring(0, NewNickname.length() - 1);
        }
        NewNickname = NewNickname + (numericInt+1);
        event.getMember().modifyNickname(NewNickname).queue();
    }
}
