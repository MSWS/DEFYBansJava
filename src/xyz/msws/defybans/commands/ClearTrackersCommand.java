package xyz.msws.defybans.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import xyz.msws.defybans.Client;
import xyz.msws.defybans.data.Callback;
import xyz.msws.defybans.data.pageable.Pageable;
import xyz.msws.defybans.data.pageable.PageableText;
import xyz.msws.defybans.tracker.GuildTrackAssigner;

public class ClearTrackersCommand extends AbstractCommand {

	private GuildTrackAssigner gta;

	public ClearTrackersCommand(Client client, String name) {
		super(client, name);
		setPermission(Permission.ADMINISTRATOR);
		setAliases("cleartracks", "cts");
		setUsage("");
		if ((gta = client.getModule(GuildTrackAssigner.class)) == null)
			client.getCommandListener().unregisterCommand(this);
	}

	@Override
	public void execute(Message message, String[] args) {
		Callback<GuildMessageReactionAddEvent> confirm = new Callback<GuildMessageReactionAddEvent>() {
			@Override
			public void execute(GuildMessageReactionAddEvent call) {
				gta.getManager(message.getGuild()).clearTrackers();
				call.retrieveMessage().queue(
						m -> m.editMessage("Successfully cleared all trackers, add trackers with !addtracker").queue());
			}
		};
		Pageable<Message> page = new PageableText(client, "Please verify that you want to clear all trackers.")
				.bindTo(message.getAuthor());
		page.addCallback("✅", confirm);
		page.send(message.getTextChannel());
	}

}
