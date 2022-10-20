package org.example;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class commands extends ListenerAdapter {
    static Permission permission = Permission.ADMINISTRATOR;
    public void onMessageReceived(MessageReceivedEvent e){
        if(e.getAuthor().isBot()) return;
        String message  = e.getMessage().getContentRaw();
        if(!message.startsWith("!egb_leaderboard ")) return;
        String[] args = message.split(" ");
        String command = args[1];
        switch (command){
            case "help":
                System.out.println("yes");
                e.getMessage().reply(String.format("```!egb_leaderboard create <name> - create a game \n" +
                                "!egb_leaderboard addRole <gameName> <roleId> - whitelist roles \n" +
                                "!egb_leaderboard removeRole <gameName> <roleId> - remove roles \n" +
                                "!egb_leaderboard addChannel <gameName> <channelId> - whitelist channels \n" +
                                "!egb_leaderboard removeChannel <gameName> <channelId> - remove channels \n" +
                                "!egb_leaderboard enable <gameName> - enables the game \n" +
                                "!egb_leaderboard disable <gameName> - disable the game \n" +
                                "!egb_leaderboard createPoints <gameName> <points> \n" +
                                "!egb_leaderboard messagePoints <gameName> <points> \n"+
                                "!egb_leaderboard showGames - shows all the games + game info\n" +
                                "!egb_leaderboard <gameName> - see leaderboard for that game```"))
                            .mentionRepliedUser(false).queue();
                break;
            case "create":
                if(!e.getMember().hasPermission(permission)) return;
                if(args.length == 3){
                    String name = args[2];
                    try {
                        database.connection.createStatement().execute(String.format("INSERT INTO games(name) VALUES ('%s')", name));
                        database.connection.createStatement().execute(String.format("CREATE TABLE IF NOT EXISTS %s (userId TEXT UNIQUE, points INTEGER)", name));
                    } catch (SQLException ex) {
                       e.getMessage().reply("```error: \n" +
                               "-> that game already exists!```").queue();
                       return;
                    }
                    e.getMessage().reply(String.format("```Created %s, please use: \n" +
                                    "!egb_leaderboard addRole <gameName> <roleId> - whitelist roles \n" +
                                    "!egb_leaderboard addChannel <gameName> <channelId> - whitelist channels \n" +
                                    "!egb_leaderboard enable <gameName> - enables the game``` ", name))
                            .mentionRepliedUser(false).queue();
                }else{
                    e.getMessage().reply("```error: \n" +
                            "-> must provide a name!```").queue();
                }
                return;

            case "addRole":
                if(!e.getMember().hasPermission(permission)) return;
                if(args.length == 4){
                    String gameName = args[2];
                    String roleName = null;
                    try {
                        String pastRoleString = database.connection.createStatement().executeQuery("SELECT roles FROM games WHERE name == '" + gameName + "'").getString("roles");
                        String toInsertRole;

                        if(pastRoleString != null){
                            if(pastRoleString.contains(args[3])){
                                e.getMessage().reply("```error: \n" +
                                        "Role already exists!```").queue();
                                return;
                            }
                             toInsertRole = pastRoleString + args[3] + " ";
                        }else{
                            toInsertRole = args[3] + " ";
                        }
                        try{
                            roleName = e.getJDA().getRoleById(args[3]).getName();
                        }catch (Exception exception){
                            e.getMessage().reply("```error: \n" +
                                    "-> Not a valid roleId!  ```").queue();
                            return;
                        }
                        database.connection.createStatement().execute(String.format("UPDATE games SET roles = '%s' WHERE name == '%s'", toInsertRole, gameName));
                    } catch (SQLException exception){
                        e.getMessage().reply("```error: \n" +
                            "-> must provide a valid gameName!```").queue();
                    }
                        e.getMessage().reply(String.format("```Added role: \n" +
                                "game: '%s' \n" +
                                "role: '%s(%s)'```", gameName, roleName , args[3])).mentionRepliedUser(false).queue();
                }else{
                    e.getMessage().reply("```error: \n" +
                            "-> must provide a name or roleId!```").queue();
                }
                return;

            case "removeRole":
                if(!e.getMember().hasPermission(permission)) return;
                if(args.length == 4){
                    String gameName = args[2];
                    String roleName;
                    try{
                        roleName = e.getJDA().getRoleById(args[3]).getName();
                    }catch (Exception exception){
                        e.getMessage().reply("```error: \n" +
                                "-> Not a valid roleId!  ```").queue();
                        return;
                    }

                    try {
                        String pastRoleString = database.connection.createStatement().executeQuery("SELECT roles FROM games WHERE name == '" + gameName + "'").getString("roles");
                        if(pastRoleString == null){
                            e.getMessage().reply("```error: \n" +
                                    "No roles to remove```").queue();
                        }
                        String toInsertRole = pastRoleString.replace(args[3] + " ", "");
                        database.connection.createStatement().execute(String.format("UPDATE games SET roles = '%s' WHERE name == '%s'", toInsertRole, gameName));
                    } catch (SQLException exception){
                        e.getMessage().reply("```error: \n" +
                                "-> must provide a valid gameName!```").queue();
                    }
                    e.getMessage().reply(String.format("```Removed role: \n" +
                            "game: '%s' \n" +
                            "role: '%s(%s)'```", gameName, roleName, args[3])).mentionRepliedUser(false).queue();
                }else{
                    e.getMessage().reply("```error: \n" +
                            "-> must provide a name or roleId!```").queue();
                }
                return;

            case "showGames":
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter("games.txt"));
                    ResultSet set = database.connection.createStatement().executeQuery("SELECT * FROM games");
                    StringBuilder builder = new StringBuilder();
                    builder.append("Id             name             roles             channels             enabled?             createPoints             messagePoints\n")
                            .append("------        -------------    -------------     ----------------     ----------           ------------             -------------- \n");
                    String id;
                    String name;
                    String roleIds;
                    String channelIds;
                    boolean enabled;
                    int createPoints;
                    int messagePoints;
                    while (set.next()){
                        id = set.getString("_id");
                        name = set.getString("name");
                        roleIds = set.getString("roles");
                        StringBuilder roles = new StringBuilder();
                        channelIds = set.getString("channels");
                        StringBuilder channels = new StringBuilder();
                        enabled = set.getBoolean("enabled");
                        createPoints = set.getInt("createPoints");
                        messagePoints = set.getInt("messagePoints");

                        if(channelIds != null){
                            Arrays.stream(channelIds.split(" ")).forEach(channel -> {
                                try{
                                    channels.append(e.getJDA().getTextChannelById(channel).getName()).append(", ");
                                }catch (Exception exception){
                                    channels.append("NO CHANNELS");
                                }
                            });
                        }else{
                            channels.append("NO CHANNELS");
                        }
                        if(roleIds  != null){
                            Arrays.stream(roleIds.split(" ")).forEach(role ->{
                                       try{
                                           roles.append(e.getJDA().getRoleById(role).getName()).append(", ");
                                       }catch (Exception exception){
                                           roles.append("NO ROLES");
                                       }
                                    });
                        }else{
                            roles.append("NO ROLES");
                        }
                        builder.append(String.format("%s.             %s             %s             %s             %s             %s             %s \n", id, name, roles, channels, enabled, createPoints, messagePoints));

                    }
                    writer.write(builder.toString());
                    writer.close();
                    e.getMessage().reply("ㅤ")
                            .addFiles(FileUpload.fromData(new File("games.txt"))).queue();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                return;

            case "addChannel":
                if(!e.getMember().hasPermission(permission)) return;
                if(args.length == 4){
                    String gameName = args[2];
                    String channelName = null;
                    try {
                        String pastChannelString = database.connection.createStatement().executeQuery("SELECT channels FROM games WHERE name == '" + gameName + "'").getString("channels");
                        String toInsertChannel;
                        if(pastChannelString != null){
                            if(pastChannelString.contains(args[3])){
                                e.getMessage().reply("```error: \n" +
                                        "Channel already exists!```").queue();
                                return;
                            }
                            toInsertChannel = pastChannelString + args[3] + " ";
                        }else{
                            toInsertChannel = args[3] + " ";
                        }
                        try {
                            channelName = e.getJDA().getTextChannelById(args[3]).getName();
                        }catch (Exception exception){
                            e.getMessage().reply("```error: \n" +
                                    "-> Not a valid channel Id!```").queue();
                            return;
                        }
                        database.connection.createStatement().execute(String.format("UPDATE games SET channels = '%s' WHERE name == '%s'", toInsertChannel, gameName));
                    } catch (SQLException exception){
                        exception.printStackTrace();
                        e.getMessage().reply("```error: \n" +
                                "-> must provide a valid gameName!```").queue();
                    }
                        e.getMessage().reply(String.format("```Added channel: \n" +
                                "game: '%s' \n" +
                                "channel: '%s(%s)'```", gameName, channelName, args[3])).mentionRepliedUser(false).queue();
                }else{
                    e.getMessage().reply("```error: \n" +
                            "-> must provide a name or channelId!```").queue();
                }
                return;

            case "removeChannel":
                if(!e.getMember().hasPermission(permission)) return;
                if(args.length == 4){
                    String gameName = args[2];
                    String ChannelName;
                    try{
                        ChannelName = e.getJDA().getTextChannelById(args[3]).getName();
                    }catch (Exception exception){
                        e.getMessage().reply("```error: \n" +
                                "-> Not a valid channelId!  ```").queue();
                        return;
                    }

                    try {
                        String pastChanelString = database.connection.createStatement().executeQuery("SELECT channels FROM games WHERE name == '" + gameName + "'").getString("channels");
                        if(pastChanelString == null){
                            e.getMessage().reply("```error: \n" +
                                    "No channel to remove```").queue();
                        }
                        String toInsertChannel = pastChanelString.replace(args[3] + " ", "");
                        database.connection.createStatement().execute(String.format("UPDATE games SET channels = '%s' WHERE name == '%s'", toInsertChannel, gameName));
                    } catch (SQLException exception){
                        e.getMessage().reply("```error: \n" +
                                "-> must provide a valid gameName!```").queue();
                    }
                    e.getMessage().reply(String.format("```Removed channel: \n" +
                            "game: '%s' \n" +
                            "channel: '%s(%s)'```", gameName, ChannelName, args[3])).mentionRepliedUser(false).queue();
                }else{
                    e.getMessage().reply("```error: \n" +
                            "-> must provide a name or channelId!```").queue();
                }
                return;

            case "enable":
                if(!e.getMember().hasPermission(permission)) return;
                if(args.length == 3){
                    try{
                        ResultSet set = database.connection.createStatement().executeQuery("SELECT * FROM games WHERE name == '" + args[2] +"'");
                        String channels = set.getString("channels");
                        String roles = set.getString("roles");

                        if(channels == null || roles == null){
                            StringBuilder  builder = new StringBuilder();
                            builder.append("```error: \n" +
                            "-> Some required values missing! \n" +
                                    "missing values: ");
                            if(channels == null){
                                builder.append("whitelisted channels, ");
                            }
                            if(roles == null){
                                builder.append("whitelisted roles missing!, ");
                            }
                            builder.append("```");
                            e.getMessage().reply(builder.toString()).queue();
                            return;
                        }
                        database.connection.createStatement().execute("UPDATE games SET enabled = false WHERE enabled == true");
                        try{
                                database.connection.createStatement().execute(String.format("UPDATE games SET enabled = true WHERE name == '%s'", args[2]));
                                e.getMessage().reply(String.format("```Enabled game: \n" +
                                        "game: '%s' \n" +
                                        "Enabled: '%s'```", args[2], true )).mentionRepliedUser(false).queue();
                        }catch (Exception exception){
                            exception.printStackTrace();
                            e.getMessage().reply("```error: \n" +
                                    "-> Not a valid gameName!```").queue();
                        }
                    }catch (Exception ignored){
                    }

                }else{
                    e.getMessage().reply("```error: \n" +
                            "-> must provide a name!```").queue();
                }
                return;

            case "disable":
                if(!e.getMember().hasPermission(permission)) return;
                if(args.length == 3){
                   try{
                       database.connection.createStatement().execute(String.format("UPDATE games SET enabled = false WHERE name == '%s'", args[2]));
                       e.getMessage().reply(String.format("```Disabled game: \n" +
                               "game: '%s' \n" +
                               "Disabled: '%s'```", args[2], false )).mentionRepliedUser(false).queue();
                   }catch (Exception exception){
                       e.getMessage().reply("```error: \n" +
                               "-> Not a valid gameName!```").queue();
                   }
                }else{
                    e.getMessage().reply("```error: \n" +
                            "->Must provide a name!```").queue();
                }
                return;
            default:
                StringBuilder leaderboard = new StringBuilder();
                if(args.length == 2){
                    String s = null;
                    try {
                        s = database.connection.createStatement().executeQuery("SELECT * FROM games WHERE name == '" + args[1] + "'").getString("name");
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                    if(s == null){
                            leaderboard.append(String.format("`The game %s doesn't exist!`", args[1]));
                            e.getMessage().replyEmbeds(new EmbedBuilder()
                                    .setTitle(String.format("Game leaderboard: %s", args[1]))
                                    .setDescription(leaderboard.toString())
                                    .build()).mentionRepliedUser(false).queue();
                            return;
                    }
                    Map<String, Integer> points = new HashMap<>();
                    try{
                       ResultSet set = database.connection.createStatement().executeQuery(String.format("SELECT * FROM %s", args[1]));
                       while(set.next()){
                           points.put(set.getString("userId"), set.getInt("points"));
                       }
                    }catch (Exception ignored){}
                    List<Map.Entry<String, Integer>> sorted = points.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).collect(Collectors.toList());
                    AtomicInteger no = new AtomicInteger();
                    sorted.forEach(entry ->{
                        no.getAndIncrement();
                        leaderboard.append(String.format("`%s.` <@%s> - `%s` points \n", no.get(), entry.getKey(), entry.getValue()));
                    });
                    e.getMessage().replyEmbeds(new EmbedBuilder()
                            .setTitle(String.format("Game leaderboard: %s", args[1]))
                                    .setColor(Color.WHITE)
                            .setDescription(leaderboard.toString())
                            .build()).mentionRepliedUser(false).queue();
                }else{
                    e.getMessage().reply("```error: \n" +
                            "-> Must provide a gameName```").queue();
                }
                return;
            case "createPoints":
                if(args.length == 4){
                    try{
                        database.connection.createStatement().execute(String.format("UPDATE games SET createPoints = %s WHERE name == '%s'", args[3], args[2]));
                        e.getMessage().reply(String.format("```create points: \n" +
                                "game: '%s' \n" +
                                "createPoints: '%s'```", args[2], args[3])).mentionRepliedUser(false).queue();
                    }catch (Exception exception){
                        e.getMessage().reply("```error: \n" +
                                "-> Not a valid gameName!```").queue();
                    }
                }else{
                    e.getMessage().reply("```error: \n" +
                            "->Must provide a name or value!```").queue();
                }
                return;

            case "messagePoints":
                if(args.length == 4){
                    try{
                        database.connection.createStatement().execute(String.format("UPDATE games SET messagePoints = %s WHERE name == '%s'", args[3], args[2]));
                        e.getMessage().reply(String.format("```message points: \n" +
                                "game: '%s' \n" +
                                "messagePoints: '%s'```", args[2], args[3])).mentionRepliedUser(false).queue();
                    }catch (Exception exception){
                        e.getMessage().reply("```error: \n" +
                                "-> Not a valid gameName!```").queue();
                    }
                }else{
                    e.getMessage().reply("```error: \n" +
                            "->Must provide a name or value!```").queue();
                }

        }
    }
}
