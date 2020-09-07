package xyz.msws.defybans.commands;

import net.dv8tion.jda.api.entities.Message;
import xyz.msws.defybans.Client;

public class PingCommand extends AbstractCommand {

	public PingCommand(Client client, String name) {
		super(client, name);
	}

	@Override
	public void execute(Message message, String[] args) {
		long time = System.currentTimeMillis();
		message.getChannel().sendMessage("Pong!")
				.flatMap(msg -> msg.editMessageFormat("Pong! (%d ms)", System.currentTimeMillis() - time)).queue();
	}

}
