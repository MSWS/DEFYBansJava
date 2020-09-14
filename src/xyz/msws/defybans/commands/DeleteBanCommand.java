package xyz.msws.defybans.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import xyz.msws.defybans.Client;
import xyz.msws.defybans.data.Callback;
import xyz.msws.defybans.data.pageable.Pageable;
import xyz.msws.defybans.data.pageable.PageableEmbed;
import xyz.msws.defybans.data.punishment.Punishment;
import xyz.msws.defybans.data.punishment.Punishment.Key;
import xyz.msws.defybans.data.punishment.PunishmentManager;
import xyz.msws.defybans.tracker.GuildTrackAssigner;

public class DeleteBanCommand extends AbstractCommand {
	private GuildTrackAssigner assigner;

	public DeleteBanCommand(Client client, String name) {
		super(client, name);
		if ((assigner = client.getModule(GuildTrackAssigner.class)) == null)
			client.getCommandListener().unregisterCommand(this);
		setAliases("db", "delete");
		setPermission(Permission.KICK_MEMBERS);
		setDescription("Deletes specific punishments");
		setUsage("[Hash/IDs] <Values>");
	}

	@Override
	public void execute(Message message, String[] args) {

		if (args.length == 0) {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setTitle("Example usage for DeleteBans");
			builder.setFooter("Requested by " + message.getAuthor().getAsTag());
			builder.setColor(Color.CYAN);
			List<String> desc = new ArrayList<>();
			String p = client.getPrefix();
			desc.add(p + "deleteban [Hash]");
			desc.add(p + "deleteban Player: MSWS");
			desc.add(p + "deleteban Player: MSWS\nReason: (?i)rdm");
			desc.add(p + "deleteban Reason: (?i)(hack|cheat|wall)");
			builder.setDescription(String.join("\n", desc));

			message.getChannel().sendMessage(builder.build()).queue();
			return;
		}

		PunishmentManager tracker = assigner.getManager(message.getGuild());

		if (args.length == 1) {
			if (!NumberUtils.isNumber(args[0])) {
				message.getChannel().sendMessage("Invalid punishment hash, hashes can only be integers.").queue();
				return;
			}

			int hash;

			try {
				hash = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				message.getChannel().sendMessage("An error occured when parsing the specified hash.").queue();
				return;
			}

			Punishment punish = null;

			for (Punishment p : tracker.getPunishments()) {
				if (p.hashCode() == hash) {
					punish = p;
					break;
				}
			}

			if (punish == null) {
				message.getChannel().sendMessageFormat("A punishment could not be found with the %d hash.", hash)
						.queue();
				return;
			}

			tracker.delete(punish, true);
			message.getChannel().sendMessage("Punishment successfully deleted.");
			return;
		}

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
			message.getChannel().sendMessage("Unknown regex: " + MarkdownSanitizer.escape(String.join("\n", unknown)))
					.queue();

		if (embeds.isEmpty()) {
			message.getChannel().sendMessage(
					"No punishments matched the " + filters.size() + " filter" + (filters.size() == 1 ? "" : "s"))
					.queue();
			return;
		}

		Pageable<MessageEmbed> pe = new PageableEmbed(client, embeds).bindTo(message.getAuthor());
		Callback<GuildMessageReactionAddEvent> confirm = new Callback<GuildMessageReactionAddEvent>() {
			@Override
			public void execute(GuildMessageReactionAddEvent call) {
				ps.forEach(p -> tracker.delete(p));
			}
		};
		pe.addCallback("✅", confirm);
		pe.send(message.getTextChannel());
	}

}
