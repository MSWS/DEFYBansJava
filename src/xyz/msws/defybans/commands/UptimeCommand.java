package xyz.msws.defybans.commands;

import net.dv8tion.jda.api.entities.Message;
import xyz.msws.defybans.Client;
import xyz.msws.defybans.utils.TimeParser;

public class UptimeCommand extends AbstractCommand {

	private long start;

	public UptimeCommand(Client client, String name) {
		super(client, name);
		start = System.currentTimeMillis();
		setAliases("lifetime");
		setDescription("Get how long the bot has been up");
		setUsage("");
	}

	@Override
	public void execute(Message message, String[] args) {
		message.getChannel().sendMessage(
				"Current uptime: " + TimeParser.getDurationDescription((System.currentTimeMillis() - start) / 1000))
				.queue();
	}

}
