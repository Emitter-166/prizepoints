package org.example;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.sql.SQLException;

public class Main {
    public static JDA jda;
    public static void main(String[] args) throws LoginException, InterruptedException, SQLException {
       jda = JDABuilder.createLight(tokens.TOKEN)
               .enableIntents(GatewayIntent.GUILD_MESSAGES)
               .enableIntents(GatewayIntent.MESSAGE_CONTENT)
               .enableIntents(GatewayIntent.GUILD_MEMBERS)
               .addEventListeners(new commands())
               .addEventListeners(new capture())
               .build().awaitReady();
       Thread thread = new Thread(new load_info());
       thread.start();
       Thread.sleep(10_000);
      for(int i = 0; i < 100; i++){
          capture.increment(false, "671016674668838952", "1");
      }
    }
}