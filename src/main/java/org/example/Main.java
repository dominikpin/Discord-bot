package org.example;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.example.events.*;

import javax.security.auth.login.LoginException;

public class Main {
    public static void main(String[] args) throws LoginException {
        final String TOKEN = "MTA3NzY3OTc1NzQ0Mjk2MTUzOQ.GDhpas.UZEQEpQB8UplKI5hVzTVssraKZSIDwP-qyFkb8";
        JDABuilder jdaBuilder = JDABuilder.createDefault(TOKEN);
        jdaBuilder
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(new ReadyEventListener(), new MessageEventListener(), new DeafenListener())
                .build();

    }
}