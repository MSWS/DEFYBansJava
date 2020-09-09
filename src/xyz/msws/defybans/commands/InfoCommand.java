package xyz.msws.defybans.commands;

import java.awt.Color;
import java.io.File;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import xyz.msws.defybans.Client;
import xyz.msws.defybans.utils.TimeParser;

public class InfoCommand extends AbstractCommand {

	public InfoCommand(Client client, String name) {
		super(client, name);
		setAliases("stats", "author", "who");
	}

	@Override
	public void execute(Message message, String[] args) {
		File known = new File("main.jar");

		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("SourceMod Ban Info", "https://github.com/msws");
		builder.setColor(Color.ORANGE);
		builder.addField("Creator", "MSWS#9499", true);
		builder.addField("Servers", client.getJDA().getGuilds().size() + "", true);
		builder.addField("Server Time", TimeParser.getDateDescription(System.currentTimeMillis()), true);

		if (known.exists()) {
			long last = known.lastModified();
			builder.addField("Last Updated", TimeParser.getDateDescription(last), true);
		}
		message.getChannel().sendMessage(builder.build()).queue();
	}

}
