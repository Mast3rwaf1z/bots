package win.skademaskinen.commands;

import java.util.ArrayList;
import java.util.List;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import win.skademaskinen.musicbot.MusicBot;

public class NowPlayingCommand implements Command {

    private boolean successTag;
    private Member author;
    private Guild guild;

    public NowPlayingCommand(SlashCommandInteractionEvent event){
        author = event.getMember();
        guild = event.getGuild();
    }

    @Override
    public String build() {
        return log("author: "+author.getUser().getAsTag()+" server: "+guild.getName(), successTag);
    }

    @Override
    public String run() {
        if(author.getVoiceState().inAudioChannel()){
            if(guild.getSelfMember().getVoiceState().inAudioChannel()){
                AudioTrack track = MusicBot.getBots().get(guild).getCurrentTrack();
                successTag = true;
                return "Currently playing track: " + "["+track.getInfo().title+"]("+track.getInfo().uri+")";
            }
            else{
                successTag = false;
                return "Error: bot is not in a channel!";
            }
        }
        else{
            successTag = false;
            return "Error: you are not in a voicechannel!";
        }
    }

    @Override
    public boolean shouldEphemeral() {
        return false;
    }

    @Override
    public List<ActionRow> getActionRows() {
        return new ArrayList<>();
    }
    
}
