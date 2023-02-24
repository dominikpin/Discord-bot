package org.example.events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.util.HashMap;
import java.util.Map;

import java.util.Timer;
import java.util.TimerTask;

public class AFKListener extends ListenerAdapter {
    public static Map<String, Integer> Users = new HashMap<>();

    public Timer timer = new Timer();
    private static Guild guild = null;
    public AFKListener() {
        timer.schedule(new addOneSecond(), 0, 1000);
    }

    @Override
    public void onReady(ReadyEvent event) {
        if (event.getJDA().getGuildById("622504554428235806") != null) {
            checkMutedUsers(event.getJDA().getGuildById("622504554428235806"));
        }
    }

    public void checkMutedUsers(Guild guild) {
        for (VoiceChannel voiceChannel : guild.getVoiceChannels()) {
            for (Member member : voiceChannel.getMembers()) {
                if (member.getVoiceState().isMuted()) {
                    //System.out.printf("%s is muted in %s%n", member.getEffectiveName(), voiceChannel.getName());
                    saveUser(member.getUser().getId());
                }
            }
        }
    }

    @Override
    public void onGenericGuildVoice(GenericGuildVoiceEvent event) {
        if (event.getMember().getUser().isBot()) {
            return; // Ignore bot users
        }
        if (event instanceof GuildVoiceUpdateEvent) {
            //System.out.println(event.getMember().getUser().getId());
            if (event.getVoiceState().isMuted()) {
                //System.out.printf("%s muted themselves in %s%n", event.getMember().getUser().getName(), event.getVoiceState().getChannel().getName());
                saveUser(event.getMember().getUser().getId());
                return;
            }
        }
    }

    @Override
    public void onGuildVoiceMute(GuildVoiceMuteEvent event) {
        if (event.getMember().getUser().isBot()) {
            return; // Ignore bot users
        }
        guild = event.getGuild();
        VoiceChannel afkChannel = guild.getVoiceChannelsByName("afk", true).stream().findFirst().orElse(null);
        VoiceChannel currentChannel = event.getMember().getVoiceState().getChannel().asVoiceChannel();
        if (currentChannel.equals(afkChannel)) {
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
            for (Map.Entry<String, Integer> user : Users.entrySet()) {
                if (user.getValue().equals(600)) {
                    removeUser(user.getKey());
                    //System.out.println("1");
                    moveToAfkVoiceChannel(user.getKey());
                    continue;
                }
                Users.put(user.getKey(), user.getValue()+1);
            }
            for (Map.Entry<String, Integer> entry : Users.entrySet()) {
                System.out.println(entry.getKey() + " => " + entry.getValue());
            }
        }
    }

    public static void moveToAfkVoiceChannel(String username) {
        //System.out.println("2");
        if (guild == null) {
            // Guild not found
            return;
        }
        //System.out.println("3");
        Member member = (Member) guild.getMemberById(username);
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
        VoiceChannel afkChannel = guild.getVoiceChannelsByName("afk", true).stream().findFirst().orElse(null);
        if (afkChannel == null) {
            // AFK channel not found
            return;
        }
        //System.out.println("6");
        GuildVoiceState voiceState = member.getVoiceState();
        VoiceChannel currentChannel = member.getVoiceState().getChannel().asVoiceChannel();

        if (currentChannel.equals(afkChannel)) {
            // Member is already in the AFK channel
            return;
        }
        //System.out.println("7");
        guild.moveVoiceMember(member, afkChannel).queue();
    }
}