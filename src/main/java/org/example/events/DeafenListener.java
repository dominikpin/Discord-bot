package org.example.events;

import net.dv8tion.jda.api.events.guild.voice.GuildVoiceDeafenEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DeafenListener extends ListenerAdapter {

    @Override
    public void onGuildVoiceDeafen(GuildVoiceDeafenEvent event) {
        if (event.getMember().getUser().isBot()) {
            return; // Ignore bot users
        }
        if (event.getVoiceState().isDeafened()) {
            //System.out.printf("%s has deafened themselves in %s%n", event.getMember().getEffectiveName(), event.getVoiceState().getChannel().getName());
            if(event.getMember().getUser().getId().equals("303442341212061696")) {
                String currentNickname = event.getMember().getNickname();
                String NewNickname = currentNickname;
                int numericInt = Integer.parseInt(currentNickname.replaceAll("\\D+", ""));
                for (int i = numericInt; i > 0; i=i/10) {
                    //System.out.println(i);
                    NewNickname = NewNickname.substring(0, NewNickname.length() - 1);
                }
                NewNickname = NewNickname + "" + (numericInt+1);
                //System.out.println(NewNickname);
                event.getMember().modifyNickname(NewNickname).queue();

            }
        } else {
            //System.out.printf("%s has undeafened themselves in %s%n", event.getMember().getEffectiveName(), event.getVoiceState().getChannel().getName());
        }
        // Perform actions when user has deafened or undeafened themselves
    }
}


