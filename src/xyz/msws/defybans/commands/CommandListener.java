package xyz.msws.defybans.commands;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import xyz.msws.defybans.Client;

public class CommandListener extends ListenerAdapter {

	private List<AbstractCommand> commands = new ArrayList<>();
	private Client client;

	public CommandListener(Client client) {
		this.client = client;
	}

	public boolean registerCommand(AbstractCommand command) {
		return commands.add(command);
	}

	public boolean isCommandRegistered(AbstractCommand command) {
		return commands.contains(command);
	}

	public AbstractCommand getCommand(String name) {
		return commands.stream().filter(cmd -> cmd.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().getIdLong() == client.getJDA().getSelfUser().getIdLong())
			return;
		Message message = event.getMessage();

		if (!message.getContentDisplay().toLowerCase().startsWith(client.getPrefix().toLowerCase())
				&& !message.getContentDisplay().startsWith("@" + client.getJDA().getSelfUser().getName()))
			return;

		String msg = message.getContentDisplay()
				.substring(message.getContentDisplay().toLowerCase().startsWith(client.getPrefix().toLowerCase())
						? client.getPrefix().length()
						: client.getJDA().getSelfUser().getName().length() + 2);

		for (AbstractCommand cmd : commands) {
			if (cmd.getName().equalsIgnoreCase(msg.split(" ")[0])
					|| cmd.getAliases().contains(msg.split(" ")[0].toLowerCase())) {
				if (!cmd.checkPermission(message))
					break;
				cmd.execute(message, msg.contains(" ") ? msg.substring(msg.indexOf(" ")).split(" ") : new String[0]);
				break;
			}
		}
	}
}
