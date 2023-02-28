package org.example.events;

import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.*;

import java.util.*;

import org.example.Main;

public class AFKListener extends ListenerAdapter {
    public static Map<String, Integer> Users = new HashMap<>();
    private static final String AFK_CHANNEL_ID = Main.AFK_CHANNEL_ID;
    private static final String GUILD_ID = Main.GUILD_ID;
    private static Guild guild = null;

    public Timer timer = new Timer();

    public AFKListener() {
        timer.schedule(new addOneSecond(), 0, 1000);
    }

    @Override
    public void onReady(ReadyEvent event) {
        guild = event.getJDA().getGuildById(GUILD_ID);
        if (guild != null) {
            checkMutedUsers(guild);
        }
    }

    public void checkMutedUsers(Guild Guild) {
        VoiceChannel afkChannel = guild.getVoiceChannelById(AFK_CHANNEL_ID);
        if (afkChannel == null) {
            return;
        }
        for (VoiceChannel voiceChannel : guild.getVoiceChannels()) {
            for (Member member : voiceChannel.getMembers()) {
                if (member.getVoiceState().isMuted()) {
                    if (member.getUser().isBot()) {
                        continue;
                    }
                    if (member.getVoiceState().getChannel().asVoiceChannel().equals(afkChannel)) {
                        continue;
                    }
                    //System.out.printf("%s is muted in %s%n", member.getEffectiveName(), voiceChannel.getName());
                    saveUser(member.getUser().getId());
                }
            }
        }
    }

    @Override
    public void onGenericGuildVoice(GenericGuildVoiceEvent event) {
        guild = event.getGuild();
        VoiceChannel afkChannel = guild.getVoiceChannelById(AFK_CHANNEL_ID);
        if (event.getMember().getUser().isBot()) {
            return;
        }
        if (event.getVoiceState().getChannel() == null) {
            return;
        }
        if (!(event.getVoiceState().getChannel() instanceof VoiceChannel)) {
            return;
        }
        if ((event.getVoiceState().getChannel().asVoiceChannel()).equals(afkChannel)) {
            removeUser(event.getMember().getUser().getId());
            return;
        }
        if (event instanceof GuildVoiceUpdateEvent) {
            //System.out.println(event.getMember().getUser().getId());
            if (event.getVoiceState().isMuted()) {
                //System.out.printf("%s muted themselves in %s%n", event.getMember().getUser().getName(), event.getVoiceState().getChannel().getName());
                saveUser(event.getMember().getUser().getId());
            }
        }
    }
    

    @Override
    public void onGuildVoiceMute(GuildVoiceMuteEvent event) {
        if (event.getMember().getUser().isBot()) {
            return; // Ignore bot users
        }
        guild = event.getGuild();
        VoiceChannel afkChannel = guild.getVoiceChannelById(AFK_CHANNEL_ID);
        VoiceChannel currentChannel = event.getMember().getVoiceState().getChannel().asVoiceChannel();
        if (currentChannel.equals(afkChannel)) {
            removeUser(event.getMember().getUser().getId());
            // Member is already in the AFK channel
            return;
        }
        if (event.getVoiceState().isMuted()) {

            //System.out.printf("%s muted themselves in %s%n", event.getMember().getUser().getName(), event.getVoiceState().getChannel().getName());
            saveUser(event.getMember().getUser().getId());
            return;
        }

        //System.out.printf("%s unmuted themselves in %s%n", event.getMember().getUser().getName(), event.getVoiceState().getChannel().getName());
        removeUser(event.getMember().getUser().getId());
    }

    public static void saveUser(String user) {
        Users.put(user, 0);
    }
    public static void removeUser(String user) {
        Users.remove(user);
    }

    private static class addOneSecond extends TimerTask {
        public void run() {
            Map<String, Integer> usersCopy = new HashMap<>(Users); // create a copy of the Users map
            for (Map.Entry<String, Integer> user : usersCopy.entrySet()) {
                if (user.getValue().equals(60)) {
                    removeUser(user.getKey());
                    //System.out.println("1");
                    moveToAfkVoiceChannel(user.getKey());
                    continue;
                }
                Users.put(user.getKey(), user.getValue()+1);
            }
            //for (Map.Entry<String, Integer> entry : Users.entrySet()) {
            //    System.out.println(entry.getKey() + " => " + entry.getValue());
            //}
        }
    }

    public static void moveToAfkVoiceChannel(String username) {
        //System.out.println("2");
        if (guild == null) {
            // Guild not found
            return;
        }
        //System.out.println("3");
        Member member = guild.getMemberById(username);
        if (member == null) {
            // Member not found
            return;
        }
        //System.out.println("4");
        if (member.getVoiceState() == null || member.getVoiceState().getChannel() == null) {
            // Member is not connected to a voice channel
            return;
        }
        //System.out.println("5");
        VoiceChannel afkChannel = guild.getVoiceChannelById(AFK_CHANNEL_ID);
        if (afkChannel == null) {
            // AFK channel not found
            return;
        }
        //System.out.println("6");
        VoiceChannel currentChannel = member.getVoiceState().getChannel().asVoiceChannel();

        if (currentChannel.equals(afkChannel)) {
            // Member is already in the AFK channel
            return;
        }
        //System.out.println("7");
        guild.moveVoiceMember(member, afkChannel).queue();
    }
}