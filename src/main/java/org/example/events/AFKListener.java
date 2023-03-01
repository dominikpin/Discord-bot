package org.example.events;

import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.*;

import java.util.*; 

public class AFKListener extends ListenerAdapter {
    // A map that stores the user ID and how long they have been muted for
    private static Map<String, Integer> Users = new HashMap<>();
    // Declaring constant AFK_CHANNEL_ID and GUILD_ID and guild as class variables
    private static String AFK_CHANNEL_ID;
    private static String GUILD_ID;
    private static Guild guild;
    private static VoiceChannel afkChannel;
    private static final int AFKTime = 600;

    public Timer timer = new Timer();

    // Constructor that initializes the values AFK_CHANNEL_ID and GUILD_ID
    public AFKListener(String AFK_CHANNEL_ID, String GUILD_ID) {
        AFKListener.AFK_CHANNEL_ID = AFK_CHANNEL_ID;
        AFKListener.GUILD_ID = GUILD_ID;
        timer.schedule(new addOneSecond(), 0, 1000);
    }

    // Method is called when the bot is ready
    @Override
    public void onReady(ReadyEvent event) {
        guild = event.getJDA().getGuildById(GUILD_ID);
        afkChannel = guild.getVoiceChannelById(AFK_CHANNEL_ID);
        if (guild != null) {
            checkMutedUsers(guild);
        }
    }

    // Method checks if any users are already muted when the bot starts up and saves them in Users map
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
                    saveUser(member.getUser().getId());
                }
            }
        }
    }

    // Method is called whenever a voice state changes in the guild
    @Override
    public void onGenericGuildVoice(GenericGuildVoiceEvent event) {
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
        // Removes a user from Users map if user is in afk channel
        if ((event.getVoiceState().getChannel().asVoiceChannel()).equals(afkChannel)) {
            removeUser(event.getMember().getUser().getId());
            return;
        }
        // Saves a user in Users map if user is mutes themselves
        if (event instanceof GuildVoiceUpdateEvent && event.getVoiceState().isMuted()) {
            saveUser(event.getMember().getUser().getId());
        }
    }
    
    // Method is called whenever a voice state changes in the guild
    @Override
    public void onGuildVoiceMute(GuildVoiceMuteEvent event) {
        if (event.getMember().getUser().isBot()) {
            return;
        }
        VoiceChannel currentChannel = event.getMember().getVoiceState().getChannel().asVoiceChannel();
        // Removes a user from Users map if user is in afk channel
        if (currentChannel.equals(afkChannel)) {
            removeUser(event.getMember().getUser().getId());
            return;
        }
        // Saves a user in Users map if user is mutes themselves
        if (event.getVoiceState().isMuted()) {
            saveUser(event.getMember().getUser().getId());
            return;
        }
        // Removes a user from Users map if user is unmutes themselves
        removeUser(event.getMember().getUser().getId());
    }

    // Method saves a user in Users
    public static void saveUser(String user) {
        Users.put(user, 0);
    }
    // Method removes a user from Users
    public static void removeUser(String user) {
        Users.remove(user);
    }

    // Method increases timer of every user in map Users and if timer hits {AFKTime}, user is moved to afk channel
    private static class addOneSecond extends TimerTask {
        public void run() {
            Map<String, Integer> usersCopy = new HashMap<>(Users);
            for (Map.Entry<String, Integer> user : usersCopy.entrySet()) {
                if (user.getValue().equals(AFKTime)) {
                    removeUser(user.getKey());
                    moveToAfkVoiceChannel(user.getKey());
                    continue;
                }
                Users.put(user.getKey(), user.getValue()+1);
            }
        }
    }

    // Method moves user to afk channel
    public static void moveToAfkVoiceChannel(String username) {
        if (guild == null) {
            return;
        }
        Member member = guild.getMemberById(username);
        if (member == null) {
            return;
        }
        if (member.getVoiceState() == null || member.getVoiceState().getChannel() == null) {
            return;
        }
        VoiceChannel afkChannel = guild.getVoiceChannelById(AFK_CHANNEL_ID);
        if (afkChannel == null) {
            return;
        }
        VoiceChannel currentChannel = member.getVoiceState().getChannel().asVoiceChannel();
        if (currentChannel.equals(afkChannel)) {
            return;
        }
        guild.moveVoiceMember(member, afkChannel).queue();
    }
}