package org.example;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class capture extends ListenerAdapter {
    public void onMessageReceived(MessageReceivedEvent e){
        if(!e.getChannel().getType().isThread()) return;

    }


    void increment(int points, String userId){

    }
}
