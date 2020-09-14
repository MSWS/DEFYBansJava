package xyz.msws.defybans.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import xyz.msws.defybans.Client;
import xyz.msws.defybans.data.pageable.PageableEmbed;
import xyz.msws.defybans.data.punishment.Punishment;
import xyz.msws.defybans.data.punishment.Punishment.Key;
import xyz.msws.defybans.data.punishment.PunishmentManager;
import xyz.msws.defybans.tracker.GuildTrackAssigner;

public class ListBansCommand extends AbstractCommand {
	private GuildTrackAssigner assigner;

	public ListBansCommand(Client client, String name) {
		super(client, name);

		if ((assigner = client.getModule(GuildTrackAssigner.class)) == null)
			client.getCommandListener().unregisterCommand(this);
		setAliases("lb", "getbans", "search");
		setDescription("List bans by criteria");
		setUsage("[id]: [regex]\\n...");
	}

	@Override
	public void execute(Message message, String[] args) {
		if (args.length < 2) {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setTitle("Example usage for ListBans");
			builder.setFooter("Requested by " + message.getAuthor().getAsTag());
			builder.setColor(Color.CYAN);
			List<String> desc = new ArrayList<>();
			String p = client.getPrefix();
			desc.add(p + "listbans Player: MSWS");
			desc.add(p + "listbans Player: MSWS\nReason: (?i)rdm");
			desc.add(p + "listbans Reason: (?i)(hack|cheat|wall)");
			builder.setDescription(String.join("\n", desc));

			message.getChannel().sendMessage(builder.build()).queue();
			return;
		}

		PunishmentManager tracker = assigner.getManager(message.getGuild());

		EnumMap<Key, Pattern> filters = new EnumMap<>(Key.class);
		List<String> unknown = new ArrayList<>();
		for (String msg : String.join(" ", args).split("\n")) {
			Key key = Key.fromString(msg.split(":")[0]);
			if (key == null) {
				unknown.add(msg);
				continue;
			}

			filters.put(key, Pattern.compile(msg.substring(msg.indexOf(":") + 2)));
		}

		List<Punishment> ps = new ArrayList<>(tracker.getPunishmentsRegex(filters));
		List<MessageEmbed> embeds = new ArrayList<>();
		for (int i = 0; i < ps.size(); i++) {
			MessageEmbed e = ps.get(i).createEmbed();
			e = new EmbedBuilder(e).setFooter(i + 1 + " of " + ps.size()).build();
			embeds.add(e);
		}

		if (!unknown.isEmpty())
			message.getChannel()
					.sendMessage("Unknown identifier: \n" + MarkdownSanitizer.escape(String.join("\n", unknown)))
					.queue();
		if (filters.isEmpty()) {
			message.getChannel().sendMessage("No valid filters were given.").queue();
			return;
		}

		if (embeds.isEmpty()) {
			message.getChannel().sendMessage(
					"No punishments matched the " + filters.size() + " filter" + (filters.size() == 1 ? "" : "s"))
					.queue();
			return;
		}
		new PageableEmbed(client, embeds).bindTo(message.getAuthor()).send(message.getTextChannel());
	}

}
