package win.skademaskinen.listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;


import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import win.skademaskinen.WorldOfWarcraft.RaidTeamManager;
import win.skademaskinen.musicbot.MusicBot;
import win.skademaskinen.utils.Config;
import win.skademaskinen.utils.ModalData;
import win.skademaskinen.utils.Utils;

public class ButtonListener extends ListenerAdapter{

    @SuppressWarnings("unchecked")
	public void onButtonInteraction(ButtonInteractionEvent event){
        EmbedBuilder builder = new EmbedBuilder();
        Guild guild = event.getGuild();

        TextInput messageId = TextInput.create("message_id", "Id of the message being edited, DO NOT EDIT", TextInputStyle.SHORT).setValue(event.getMessageId()).build();
        switch (event.getButton().getId()) {
            case "apply_button":
            TextInput name = TextInput.create("name", "Character name", TextInputStyle.SHORT)
                    .setPlaceholder("Your character name")
                    .build();
                TextInput server = TextInput.create("server", "Character server", TextInputStyle.SHORT)
                    .setPlaceholder("Your character server, example: argent-dawn")
                    .setValue("argent-dawn")
                    .build();
                TextInput role = TextInput.create("role", "Your role", TextInputStyle.SHORT)
                    .setPlaceholder("Healer, Tank, Ranged Damage or Melee Damage")
                    .build();
                TextInput raidtimes = TextInput.create("raidtimes", "Wednesday and Sunday 19:30 - 22:30?", TextInputStyle.SHORT)
                    .setPlaceholder("Can you raid with us? (yes/no)")
                    .setValue("yes")
                    .build();

                Modal modal = Modal.create("Application form", "application")
                    .addActionRows(ActionRow.of(name), ActionRow.of(role), ActionRow.of(server), ActionRow.of(raidtimes))
                    .build();
                event.replyModal(modal).queue();
                break;
            case "finish_button":
                if(event.getMember().hasPermission(Permission.ADMINISTRATOR)){
                    event.getMessage().editMessageComponents().queue();
                }
                break;
            case "set_title":
                if(event.getMember().hasPermission(Permission.ADMINISTRATOR)){
                    TextInput titleInput = TextInput.create("title_input", "New Title", TextInputStyle.SHORT).build();
                    Modal titleModal = Modal.create("title_modal", "Set title").addActionRows(ActionRow.of(titleInput), ActionRow.of(messageId)).build();
                    event.replyModal(titleModal).queue();
                }
                break;
            case "set_description":
                if(event.getMember().hasPermission(Permission.ADMINISTRATOR)){
                    TextInput descriptionInput = TextInput.create("description_input", "New description", TextInputStyle.PARAGRAPH).build();
                    Modal descriptionModal = Modal.create("description_modal", "Set description").addActionRows(ActionRow.of(descriptionInput), ActionRow.of(messageId)).build();
                    event.replyModal(descriptionModal).queue();
                }
                break;
            case "add_field":
                if(event.getMember().hasPermission(Permission.ADMINISTRATOR)){
                    TextInput inline = TextInput.create("inline_input", "inline?", TextInputStyle.SHORT).setValue("yes").build();
                    TextInput title = TextInput.create("field_title", "Field title", TextInputStyle.SHORT).build();
                    TextInput body = TextInput.create("field_body", "Field Body", TextInputStyle.PARAGRAPH).build();
                    Modal fieldModal = Modal.create("field_modal", "Add field").addActionRows(ActionRow.of(inline), ActionRow.of(title), ActionRow.of(body), ActionRow.of(messageId)).build();
                    event.replyModal(fieldModal).queue();
                }
                break;
            case "add_image":
                if(event.getMember().hasPermission(Permission.ADMINISTRATOR)){
                    TextInput url = TextInput.create("url_input", "Image url", TextInputStyle.SHORT).build();
                    Modal imageModal = Modal.create("image_modal", "Set image").addActionRows(ActionRow.of(url), ActionRow.of(messageId)).build();
                    event.replyModal(imageModal).queue();
                }
                break;
            case "clear_embed":
                if(event.getMember().hasPermission(Permission.ADMINISTRATOR)){
                    event.getMessage().editMessageEmbeds(new EmbedBuilder().setTitle("Empty embed").build()).queue();
					event.reply("Cleared embed").setEphemeral(true).queue();
                }
                break;
            case "poll_button":
                builder.setTitle("Poll results:");
                HashMap<String, Integer> amounts = new HashMap<String, Integer>();
                HashMap<String, Object> poll = (HashMap<String, Object>) Config.getFile("polls.json").get(event.getMessageId());
                int i = 0;
                for(String key : poll.keySet()){
                    i++;
                    if(i == 4){
                        builder.addBlankField(false);
                        i = 0;
                    }
                    ArrayList<String> options = (ArrayList<String>) poll.get(key);
                    String body = "";
                    for(String option : options){
                        body+=option+"\n";
                        if(amounts.containsKey(option)){
                            int amount = amounts.get(option)+1;
                            amounts.put(option, amount);
                        }
                        else{
                            amounts.put(option, 1);
                        }
                    }
                    builder.addField(event.getGuild().retrieveMemberById(key).complete().getEffectiveName(), body, true);
                }
                List<Entry<String, Integer>> entries = new LinkedList<Entry<String, Integer>>(amounts.entrySet());
                Collections.sort(entries, new Comparator<Entry<String, Integer>>() {
                    @Override
                    public int compare(Entry<String, Integer> val1, Entry<String, Integer> val2) {
                        return val2.getValue().compareTo(val1.getValue());
                    }
                });
                for(Entry<String, Integer> entry : entries){
                    builder.appendDescription(entry.getKey() + ": " + entry.getValue()+"\n");
                }
                builder.setThumbnail("https://cdn-icons-png.flaticon.com/512/1246/1246239.png");
                event.replyEmbeds(builder.build()).setEphemeral(true).queue();
                break;
            case "add more":
                TextInput input = TextInput.create("url", "URL or search term", TextInputStyle.SHORT).build();
                event.replyModal(Modal.create("add more", "Add More").addActionRow(input).build()).queue();
                break;
            case "show queue":
                if(event.getMember().getVoiceState().inAudioChannel() && MusicBot.getBots().containsKey(event.getGuild())){
                    int page = 0;
                    List<AudioTrack> tracks;
                    if(MusicBot.getBots().get(event.getGuild()).getQueue().size()< 15){
                        tracks = MusicBot.getBots().get(event.getGuild()).getQueue();
                    }
                    else{
                        tracks = MusicBot.getBots().get(event.getGuild()).getQueue().subList(page*15, (page*15)+15);
                    }
                    builder.setTitle("Track queue");
                    int totalTime = 0;
                    for(AudioTrack track : tracks){
                        builder.addField("", "["+track.getInfo().title+"]("+track.getInfo().uri+")\n Duration: "+Utils.getTime(track.getDuration()), false);
                        totalTime += track.getDuration();
                    }
                    builder.setFooter("Total time remaining: " + Utils.getTime(totalTime-MusicBot.getBots().get(event.getGuild()).getCurrentTrack().getDuration()) + " | Total tracks in queue: " + MusicBot.getBots().get(event.getGuild()).getQueue().size());
                    AudioTrack current = MusicBot.getBots().get(guild).player.getPlayingTrack();
                    builder.setDescription("Currently playing track:\n["+current.getInfo().title+"]("+current.getInfo().uri+")");
                    builder.appendDescription("\nDuration: "+Utils.getTime(current.getDuration()));
                    builder.setThumbnail("http://img.youtube.com/vi/"+current.getIdentifier()+"/0.jpg");
                    event.replyEmbeds(builder.build()).queue();
                }
                break;
            case "skip":
                if(MusicBot.getBots().containsKey(guild)){
                    event.deferReply().queue();
                    AudioTrack selectedTrack = null;
                    for(AudioTrack track : MusicBot.getBots().get(guild).getQueue()){
                        if(track.getInfo().title.equals(event.getMessage().getEmbeds().get(0).getAuthor().getName())){
                            selectedTrack = track;
                        }
                    }
                    MusicBot.getBots().get(guild).skip(selectedTrack, event.getHook());
                }
                break;
                
		}

        if(event.getComponentId().contains("add all")){
            event.deferReply().queue();
            List<SelectOption> urls = MusicBot.getBots().get(event.getGuild()).selectMenus.get(event.getComponentId().replace("add all", "")).getOptions();
            for(SelectOption url : urls){
                if(!MusicBot.getBots().containsKey(event.getGuild())){
                    MusicBot.getBots().put(event.getGuild(), new MusicBot(event.getMember().getVoiceState().getChannel().asVoiceChannel()));
                }
                MusicBot.getBots().get(event.getGuild()).play(url.getValue(), event.getHook());
            }
        }

        if(event.getMember().hasPermission(Permission.ADMINISTRATOR)){
            if (event.getButton().getId().contains("approve_button")) {
                String id = event.getButton().getId().replace("approve_button", "");
                ModalData modal = null;
                for(ModalData m : Config.modals){
                    if (m.getId().equals(id)){
                        modal = m;
                        break;
                    }
                }
                event.reply("Approved " + event.getGuild().retrieveMemberById(modal.getMemberId()).complete().getAsMention() + "s application, you should have a dps/heal/tanking check!").setEphemeral(true).queue();
                event.getGuild().retrieveMemberById(modal.getMemberId()).complete().getUser().openPrivateChannel().complete().sendMessage("Your application to The Nut Hut raiding team has been approved, you will need to have a dps, healing or tanking check to join!").queue();
            }
            else if(event.getButton().getId().contains("decline_button")){
                String id = event.getButton().getId().replace("decline_button", "");
                ModalData modal = null;
                for(ModalData m : Config.modals){
                    if (m.getId().equals(id)){
                        modal = m;
                        Config.modals.remove(m);
                        break;
                    }
                }
                event.reply("Declined " + event.getGuild().retrieveMemberById(modal.getMemberId()).complete().getAsMention() + "s application").setEphemeral(true).queue();
                event.getMessage().delete().complete();
                event.getGuild().retrieveMemberById(modal.getMemberId()).complete().getUser().openPrivateChannel().complete().sendMessage("Your application to The Nut Hut raiding team has been declined, please refer to your application below:").queue();
                event.getGuild().retrieveMemberById(modal.getMemberId()).complete().getUser().openPrivateChannel().complete().sendMessageEmbeds(event.getMessage().getEmbeds().get(0)).queue();
            }
            else if(event.getButton().getId().contains("add_button")){
                event.deferReply(true).queue();
                String id = event.getButton().getId().replace("add_button", "");
                ModalData modal = null;
                for(ModalData m : Config.modals){
                    if (m.getId().equals(id)){
                        modal = m;
                        Config.modals.remove(m);
                        break;
                    }
                }
                RaidTeamManager.addRaider(modal.get("name"), 
                    modal.get("server"), 
                    modal.get("role"), 
                    modal.getMemberId(), 
                    event.getGuild());
                event.getHook().editOriginal("Successfully added raider to the team and deleted application!").queue();
                event.getMessage().delete().complete();
            }
        }
        else{
            event.reply("You are not an administrator!").setEphemeral(true).queue();
        }
    }
}