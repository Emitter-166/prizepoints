package org.example;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.sql.SQLException;

public class commands extends ListenerAdapter {
    static Permission permission = Permission.MODERATE_MEMBERS;
    public void onMessageReceived(MessageReceivedEvent e){
        if(e.getAuthor().isBot()) return;
        String message  = e.getMessage().getContentRaw();
        if(!message.startsWith("!egb_leaderboard ")) return;
        String command = message.split(" ")[1];
        switch (command){
            case "help":
                e.getMessage().reply(String.format("```!egb_leaderboard create <name> - create a game \n" +
                                "!egb_leaderboard addRole <roleId> - whitelist roles \n" +
                                "!egb_leaderboard addChannel <channelId> - whitelist channels \n" +
                                "!egb_leaderboard enable - enables the game \n" +
                                "!egb_leaderboard removeRole <roleId> - remove roles \n" +
                                "!egb_leaderboard removeChannel <channelId> - remove channels \n" +
                                "!egb_leaderboard disable - disable the game \n" +
                                "!egb_leaderboard showGames - shows all the games \n" +
                                "!egb_leaderboard <gameName> - see leaderboard + game info for that game```"))
                            .mentionRepliedUser(false).queue();
                break;
            case "create":
                if(!e.getMember().hasPermission(permission)) return;
                if(message.split(" ").length == 3){
                    String name = message.split(" ")[2];
                    try {
                        database.connection.createStatement().execute(String.format("INSERT INTO games(name) VALUES ('%s')", name));
                        database.connection.createStatement().execute(String.format("CREATE TABLE %s (userId TEXT UNIQUE, points INTEGER)", name));
                    } catch (SQLException ex) {
                       e.getMessage().reply("```error: \n" +
                               "-> that game already exists!```").queue();
                       return;
                    }
                    e.getMessage().reply(String.format("```Created %s, please use: \n" +
                                    "!egb_leaderboard addRoles <roleId0, roleId2, roleId3....> - whitelist roles \n" +
                                    "!egb_leaderboard addChannels <channelId0, channelId2, channelId3> - whitelist channels \n" +
                                    "!egb_leaderboard enable - enables the game``` ", name))
                            .mentionRepliedUser(false).queue();
                }else{
                    e.getMessage().reply("```error: \n" +
                            "-> must provide a name!```").queue();
                }
                break;

            case ""



        }
    }
}
