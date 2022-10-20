package org.example;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.sql.SQLException;

import static org.example.capture.increment;
import static org.example.load_info.*;

class Process implements Runnable{
    MessageReceivedEvent e;
    public Process(MessageReceivedEvent e) {
        this.e = e;
    }
    @Override
    public void run() {
        ThreadChannel channel = e.getChannel().asThreadChannel();
            try{
               if (!channels.contains(channel.getParentMessageChannel())) return;
               if(!(channel.retrieveParentMessage().complete().getAuthor().equals(e.getMessage().getAuthor()))) return;
           }catch (NullPointerException ignored){}
           Member player = e.getGuild().retrieveMemberById(channel.getOwnerId()).complete();
           if(!players.contains(player)) return;
           try {
               increment(false, player.getId(), e.getChannel().getId());
           } catch (SQLException ex) {
               throw new RuntimeException(ex);
           }
    }
}
public class capture extends ListenerAdapter {
    public void onMessageReceived(MessageReceivedEvent e){
        if (!e.getChannel().getType().isThread()) return;
        Thread thread = new Thread(new Process(e));
        thread.start();
    }


   public void onChannelCreate(ChannelCreateEvent e){
        if(!e.getChannelType().isThread()) return;
       ThreadChannel channel = e.getChannel().asThreadChannel();
       try{
           if (!channels.contains(channel.getParentMessageChannel())) return;
       }catch (NullPointerException ignored){}
       Member player = e.getGuild().retrieveMemberById(channel.getOwnerId()).complete();
       if(!players.contains(player)) return;
       try {
           increment(true, player.getId(), e.getChannel().getId());
       } catch (SQLException ex) {
           throw new RuntimeException(ex);
       }
   }


    public static void increment(boolean createPoints, String userId, String threadId) throws SQLException {
        int points;
        int thread_points;
        if(game_name == null){
            System.out.println("No active games!!");
            return;
        }
        points = database.connection.createStatement().executeQuery(String.format("SELECT points FROM %s WHERE userId == '%s'", game_name, userId)).getInt("points");
        thread_points = database.connection.createStatement().executeQuery(String.format("SELECT * FROM threads WHERE _id == '%s' ", threadId)).getInt("points");
        if(thread_points == 0){
            thread_points = createPoints ? load_info.createPoints : messagePoints;
            database.connection.createStatement().execute(String.format("INSERT INTO threads VALUES ('%s', %s)", threadId, thread_points));
        }else{
            thread_points = createPoints ? thread_points + load_info.createPoints : thread_points + messagePoints;
            database.connection.createStatement().execute(String.format("UPDATE threads SET points = %s WHERE _id == '%s' ", thread_points, threadId));
        }

        if(thread_points > 200) return;

        if(points == 0){
            points = createPoints ? load_info.createPoints : messagePoints;
            database.connection.createStatement().execute(String.format("INSERT INTO %s VALUES ('%s', %s)", game_name, userId, points));
        }else{
            points = createPoints ? points + load_info.createPoints : points + messagePoints;
            database.connection.createStatement().execute(String.format("UPDATE %s SET points = %s WHERE userId == '%s'", game_name, points, userId));
        }
    }
}
