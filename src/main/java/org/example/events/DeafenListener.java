package org.example.events;

import org.example.Main;

import net.dv8tion.jda.api.events.guild.voice.GuildVoiceDeafenEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DeafenListener extends ListenerAdapter {

    public static String ZALBIK_ID = Main.ZALBIK_ID;

    @Override
    public void onGuildVoiceDeafen(GuildVoiceDeafenEvent event) {
        if (event.getMember().getUser().isBot()) {
            return; // Ignore bot users
        }
        if (event.getVoiceState().isDeafened()) {
            //System.out.printf("%s has deafened themselves in %s%n", event.getMember().getEffectiveName(), event.getVoiceState().getChannel().getName());
            if(event.getMember().getUser().getId().equals(ZALBIK_ID)) {
                String currentNickname = event.getMember().getNickname(); //ZALBIK 15 EWRČL1547 number= 1546
                String NewNickname = currentNickname;
                int numericInt = Integer.parseInt(currentNickname.replaceAll("\\D+", ""));
                for (int i = numericInt; i > 0; i/=10) {
                    //System.out.println(i);
                    NewNickname = NewNickname.substring(0, NewNickname.length() - 1);
                }
                NewNickname = NewNickname + (numericInt+1);
                //System.out.println(NewNickname);
                event.getMember().modifyNickname(NewNickname).queue();

            }
        } else {
            //System.out.printf("%s has undeafened themselves in %s%n", event.getMember().getEffectiveName(), event.getVoiceState().getChannel().getName());
        }
        // Perform actions when user has deafened or undeafened themselves
    }
}


