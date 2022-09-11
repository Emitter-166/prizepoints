package org.example;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

public class Main {
    public static JDA jda;
    public static void main(String[] args) throws LoginException {
       jda = JDABuilder.createLight(tokens.TOKEN)
               .enableIntents(GatewayIntent.GUILD_MESSAGES)
               .addEventListeners(new commands())
               .build();
    }
}