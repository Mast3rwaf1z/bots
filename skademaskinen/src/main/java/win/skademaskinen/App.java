package win.skademaskinen;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import javax.security.auth.login.LoginException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class App 
{
    static private JDA jda;
    public static void main( String[] args ) throws LoginException, InterruptedException, ClassNotFoundException, SQLException, IOException, ParseException{
        System.out.println("Starting bot");
        JSONObject config = Config.getConfig();
        jda = JDABuilder.createDefault(config.get("token").toString()).build();
        jda.addEventListener(new CommandListener());
        jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
        jda.getPresence().setActivity(Activity.playing("v2.0"));
        System.out.println("await ready");
        jda.awaitReady();
        System.out.println("finished await");
        setCommands();
        DatabaseHandler.registerHandler();
        Message raidTeamMessage = jda.getGuildById("642852517197250560")
            .getTextChannelById("987475931004284978")
            .getHistoryAround("987484728724705360", 1)
            .complete()
            .getMessageById("987484728724705360");

        Map<Member, Character> raidTeam = RaidTeamManager.getTeam(jda.getGuildById("642852517197250560"));

        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription("This is the raid team!\nIn this embedded message, you can view an updated list of all raid team members\n");
        ArrayList<String> tanks = new ArrayList<>();
        ArrayList<String> healers = new ArrayList<>();
        ArrayList<String> rangedDamage = new ArrayList<>();
        ArrayList<String> meleeDamage = new ArrayList<>();
        for(Entry<Member, Character> entry : raidTeam.entrySet()){
            Character character = entry.getValue();
            String text = entry.getKey().getAsMention() + ": " + character.main() + " | **" + character.characterClass() + " - " + character.specialization() + "**\n";
            switch (character.role()) {
                case "Tank":
                    tanks.add(text);
                    break;
                case "Healer":
                    healers.add(text);
                    break;
                case "Ranged Damage":
                    rangedDamage.add(text);
                    break;
                case "Melee Damage":
                    meleeDamage.add(text);
                    break;
            }

        }
        builder.appendDescription("**Tanks**\n");
        for(String tank : tanks){
            builder.appendDescription(tank);
        }
        builder.appendDescription("**Healers**\n");
        for(String healer : healers){
            builder.appendDescription(healer);
        }
        builder.appendDescription("**Ranged Damage**\n");
        for(String ranged : rangedDamage){
            builder.appendDescription(ranged);
        }
        builder.appendDescription("**Melee Damage**\n");
        for(String melee : meleeDamage){
            builder.appendDescription(melee);
        }
        builder.setTitle("Raid Team");
        raidTeamMessage.editMessageEmbeds(builder.build()).queue();

    }
    static private void setCommands(){
        System.out.println("Setting commands");
        /*for(Command command : jda.retrieveCommands().complete()){
            command.delete().queue();
        }*/
        jda.updateCommands().addCommands(
            Commands.slash("ping", "send a pong back"),
            Commands.slash("play", "Play a song from youtube")
                .addOption(OptionType.STRING, "url", "Youtube link to the song or playlist", true),
            Commands.slash("skip", "Skip to the next song"),
            Commands.slash("queue", "Get the current queue")
                .addOption(OptionType.INTEGER, "page", "Select a page (Default: 1)", false),
            Commands.slash("nowplaying", "Show the current song"),
            Commands.slash("disconnect", "Disconnect the bot from voice"),
            Commands.slash("pause", "Pause the bot"),
            Commands.slash("clear", "Clear the song queue"),
            Commands.slash("help", "Show a list of commands"),
            Commands.slash("announcement", "Compose an announcement message")
                .addOption(OptionType.ATTACHMENT, "file", "Announcement text in markdown", true),
            Commands.slash("apply", "Send an application to the raid team")
                .addOption(OptionType.STRING, "name", "Your character name (this works best if you give your EXACT name)", true)
                .addOption(OptionType.STRING, "server", "Which server is this character on? (put - instead of space)", true, true)
                .addOption(OptionType.STRING, "role", "Your role (Options: Healer, Tank, Ranged Damage, Melee Damage)", true, true)
                .addOption(OptionType.BOOLEAN, "raidtimes", "Will you be able to raid on Wednesdays and Sundays at 19:30 - 22:30 server time?", true),
            Commands.slash("roll", "roll a d100 for each entry")
                .addOption(OptionType.STRING, "entry1", "entry", true)
                .addOption(OptionType.STRING, "entry2", "entry", false)
                .addOption(OptionType.STRING, "entry3", "entry", false)
                .addOption(OptionType.STRING, "entry4", "entry", false)
                .addOption(OptionType.STRING, "entry5", "entry", false)
                .addOption(OptionType.STRING, "entry6", "entry", false)
                .addOption(OptionType.STRING, "entry7", "entry", false)
                .addOption(OptionType.STRING, "entry8", "entry", false)
                .addOption(OptionType.STRING, "entry9", "entry", false)
                .addOption(OptionType.STRING, "entry10", "entry", false)
            ).queue();
    }
}
