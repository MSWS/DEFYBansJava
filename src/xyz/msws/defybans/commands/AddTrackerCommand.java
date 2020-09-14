package xyz.msws.defybans.commands;

import java.net.MalformedURLException;
import java.net.URL;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import xyz.msws.defybans.Client;
import xyz.msws.defybans.data.Callback;
import xyz.msws.defybans.data.ServerConfig;
import xyz.msws.defybans.data.punishment.Punishment.Type;
import xyz.msws.defybans.tracker.GuildTrackAssigner;
import xyz.msws.defybans.tracker.Tracker;

public class AddTrackerCommand extends AbstractCommand {

	private GuildTrackAssigner gta;

	public AddTrackerCommand(Client client, String name) {
		super(client, name);
		setAliases("addt");
		setDescription("Adds URL to track bans on.");
		setPermission(Permission.ADMINISTRATOR);
		if ((gta = client.getModule(GuildTrackAssigner.class)) == null)
			client.getCommandListener().unregisterCommand(this);
	}

	@Override
	public void execute(Message message, String[] args) {
		if (args.length != 1) {
			message.getChannel().sendMessage("You must specify a URL.").queue();
			return;
		}

		URL url;

		try {
			url = new URL(args[0]);
		} catch (MalformedURLException e) {
			message.getChannel().sendMessage("The URL provided was invalid.").queue();
			return;
		}

		Tracker tracker = gta.getFactory(message.getGuild().getIdLong()).createTracker(Type.BAN, url.toExternalForm());
		tracker.verify(new Callback<Boolean>() {
			@Override
			public void execute(Boolean call) {
				if (call) {
					message.getChannel().sendMessage("Successfully added URL to tracking.").queue();
					gta.getManager(message.getGuild()).addTracker(tracker, true);
					ServerConfig config = new ServerConfig(message.getGuild().getIdLong());
					config.addTracker(tracker);
					config.save();
					return;
				}
				message.getChannel().sendMessage("The specified URL is invalid.").queue();
			}
		});

	}

}
