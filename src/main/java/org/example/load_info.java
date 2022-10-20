package org.example;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class load_info implements Runnable{
    public static Set<MessageChannel> channels = new HashSet<>();
    public static Set<Role> roles = new HashSet<>();
    public static String game_name = null;
    public static int createPoints = 0;
    public static int messagePoints = 0;
    public static Set<Member> players = new HashSet<>();

    @Override
    public void run() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {

                    ResultSet set = database.connection.createStatement().executeQuery("SELECT * FROM games WHERE enabled == true");
                    game_name = set.getString("name");
                    createPoints = set.getInt("createPoints");
                    messagePoints = set.getInt("messagePoints");
                    try{
                        Arrays.stream((set.getString("roles").split(" "))).forEach(roleId ->{
                            roles.add(Main.jda.getRoleById(roleId));
                        });

                        Arrays.stream(set.getString("channels").split(" ")).forEach(channelId ->{
                            channels.add(Main.jda.getTextChannelById(channelId));
                        });
                    }catch (Exception ignored){}
                    roles.forEach(role ->{
                       role.getGuild().findMembersWithRoles(role).onSuccess(members -> members.forEach(member -> players.add(member)));
                    });
                } catch (SQLException ignored){}
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, 60_000);
    }
}
