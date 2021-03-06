package win.skademaskinen;

import java.util.List;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class MusicBot {
    static private AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private AudioPlayer player = playerManager.createPlayer();
    private TrackScheduler scheduler;
    private AudioChannel channel;
    private Guild guild;
    private AudioManager audioManager;

    public MusicBot(AudioChannel channel, SlashCommandInteractionEvent initial_message){
        this.channel = channel;
        guild = initial_message.getGuild();
        audioManager = guild.getAudioManager();
        scheduler = new TrackScheduler(audioManager);
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        audioManager.setSendingHandler(new AudioPlayerSendHandler(player));
        player.addListener(scheduler);
        connect();

    }
    private void connect() {
        if(!audioManager.isConnected()){
            audioManager.openAudioConnection(channel);
        }
    }
    public void play(String url, SlashCommandInteractionEvent event) {
        playerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if(player.getPlayingTrack() != null){
                    scheduler.enqueue(track);
                    event.reply("Track queued").queue(); 
                }
                else{
                    player.startTrack(track, false);
                    event.reply("Track started").queue();
                }
            } 
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                event.reply("Playlist loaded").queue();
                List<AudioTrack> tracks = playlist.getTracks();
                for(AudioTrack track : tracks){
                    scheduler.enqueue(track);
                }
                if(player.getPlayingTrack() == null){
                    player.startTrack(scheduler.dequeue(), false);
                }
            }
            @Override
            public void noMatches() {
                event.reply("No matches").queue();
            }
            @Override
            public void loadFailed(FriendlyException e) {
                event.reply(e.getMessage()).queue();
                System.out.println(e.severity);
            }
        });
    }
    public void skip(SlashCommandInteractionEvent event) {
        if(player.getPlayingTrack() != null){
            player.stopTrack();
            if(!scheduler.isEmpty()){
                AudioTrack track = scheduler.dequeue();
                player.startTrack(track, false);
                event.reply("Started track: " + track.getInfo().title).queue();
            }
            else{
                event.reply("Queue is now empty").queue();
            }
        }
    }
    public List<AudioTrack> getQueue() {
        return scheduler.getQueue();
    }
    public AudioTrack getCurrentTrack() {
        return player.getPlayingTrack();
    }
    public void disconnect() {
        scheduler.emptyQueue();
        player.stopTrack();
        audioManager.closeAudioConnection();
    }
    public boolean pause() {
        if(player.isPaused()){
            player.setPaused(false);
            return false;
        }
        else{
            player.setPaused(true);
            return true;
        }
    }
    public void clear() {
        scheduler.emptyQueue();
    }
    public void connectToVoiceChannel(AudioChannel channel) {
        audioManager.openAudioConnection(channel);
    }


}
