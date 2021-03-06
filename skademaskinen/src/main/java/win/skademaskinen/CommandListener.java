package win.skademaskinen;

import java.util.HashMap;
import java.util.List;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.sql.SQLException;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.Modal.Builder;
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction;

public class CommandListener extends ListenerAdapter {
    final private String[] colors = {"blue", "green", "gray", "yellow", "orange", "red", "white", "purple", "pink", "darkgreen"};
    private HashMap<Guild, MusicBot> bots = new HashMap<>();
    private DatabaseHandler databaseHandler;


    public CommandListener() throws ClassNotFoundException, SQLException, IOException, ParseException{
        databaseHandler = new DatabaseHandler();

    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        System.out.println("Command:                " + event.getCommandString());
        System.out.println();
        Guild guild = event.getGuild();
        Member author = event.getMember();
        switch (event.getName().toLowerCase()) {
            case "ping":
                event.reply("Pong").queue();
                break;

            case "color":
                for(String color : colors){
                    if(author.getRoles().contains(guild.getRolesByName(color, true).get(0))){
                        System.out.println("Removing role" + color);
                        guild.removeRoleFromMember(author, guild.getRolesByName(color, true).get(0)).queue();
                    }
                }
                guild.addRoleToMember(author, guild.getRolesByName(event.getOption("color").getAsString(), true).get(0)).queue();
                event.reply("Set color to " + event.getOption("color").getAsString()).queue();
                break;

            //music
            case "play":
            if (author.getVoiceState().inAudioChannel() && bots.containsKey(guild)) {
                    MusicBot bot = bots.get(guild);
                    if(!guild.getSelfMember().getVoiceState().inAudioChannel()){
                        bot.connectToVoiceChannel(author.getVoiceState().getChannel());
                    }
                    bot.play(event.getOption("url").getAsString().strip(), event);
                }
                else if(event.getOptions().size() == 0){
                    error_message(event, "Please specify a track").queue();
                }
                else{
                    bots.put(guild, new MusicBot(author.getVoiceState().getChannel(), event));
                    MusicBot bot = bots.get(guild);
                    bot.play(event.getOption("url").getAsString().strip(), event);
                }
                break;
            case "skip":
                if(author.getVoiceState().inAudioChannel() && bots.containsKey(guild)){
                    bots.get(guild).skip(event);
                }
                break;
            case "queue":
                if(author.getVoiceState().inAudioChannel() && bots.containsKey(guild)){
                    int page = 0;
                    for(OptionMapping option : event.getOptions()){
                        page = option.getAsInt()-1;
                    }
                    System.out.println(bots.get(guild).getQueue().size());
                    List<AudioTrack> tracks;
                    if(bots.get(guild).getQueue().size()< 15){
                        tracks = bots.get(guild).getQueue();
                    }
                    else{
                        tracks = bots.get(guild).getQueue().subList(page*15, (page*15)+15);
                    }
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setTitle("Track queue");
                    for(AudioTrack track : tracks){
                        builder.addField("", "["+track.getInfo().title+"]("+track.getInfo().uri+")\n Duration: "+getTime(track.getDuration()), false);
                    }
                    int totalTime = 0;
                    for(AudioTrack track : tracks){
                        totalTime += track.getDuration();
                    }
                    builder.setFooter("Total time remaining: " + getTime(totalTime-bots.get(guild).getCurrentTrack().getDuration()) + " | Total tracks in queue: " + bots.get(guild).getQueue().size());
                    event.replyEmbeds(builder.build()).queue();
                }
                break;
            case "nowplaying":
                if(author.getVoiceState().inAudioChannel() && bots.containsKey(guild)){
                    AudioTrack track = bots.get(guild).getCurrentTrack();
                    event.reply("Currently playing track: " + "["+track.getInfo().title+"]("+track.getInfo().uri+")").queue();
                }
                break;
            case "disconnect":
                if(author.getVoiceState().inAudioChannel() && bots.containsKey(guild)){
                    bots.get(guild).disconnect();
                }
                event.reply("Successfully disconnected bot").queue();
                break;
            case "pause":
                if(author.getVoiceState().inAudioChannel() && bots.containsKey(guild)){
                    if(bots.get(guild).pause()){
                        event.reply("Bot paused!").queue();
                    }
                    else{
                        event.reply("Bot unpaused!").queue();
                    }
                }
                break;
            case "clear":
                if(author.getVoiceState().inAudioChannel() && bots.containsKey(guild)){
                    bots.get(guild).clear();
                    event.reply("Queue cleared!").queue();
                }
                break;
        }
    }

    private String getTime(long duration) {
        String minutes = String.valueOf((duration/1000)/60);
        if(Integer.parseInt(minutes) < 10){
            minutes = "0" + minutes;
        }
        String seconds = String.valueOf((duration/1000)%60);
        if(Integer.parseInt(seconds) < 10){
            seconds = "0"+seconds;
        }
        return minutes + ":" + seconds;
    }

    public void onMessageReceived(MessageReceivedEvent event){
        System.out.println("Server:                 " + event.getGuild().getName());
        System.out.println("Channel:                " + event.getChannel().getName());
        System.out.println("Author:                 " + event.getAuthor().getName());
        System.out.println("Message:                " + event.getMessage().getContentDisplay());
        System.out.println("Number of attachments:  " + event.getMessage().getAttachments().size());
        for(Attachment url : event.getMessage().getAttachments()){
            System.out.println("Attachment:             " + url.getUrl());
        }
        System.out.println();
    }

    public void onGuildJoined(GuildJoinEvent event) throws SQLException{
        databaseHandler.createPoopTable(event.getGuild());

    }
    public ModalCallbackAction error_message(SlashCommandInteractionEvent event, String message){
        Builder modal = Modal.create("", "Error:\n"+message);
        return event.replyModal(modal.build());
    }
}
