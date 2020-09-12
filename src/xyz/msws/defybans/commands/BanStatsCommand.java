package xyz.msws.defybans.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import xyz.msws.defybans.SourceModBans;
import xyz.msws.defybans.data.pageable.PageableEmbed;
import xyz.msws.defybans.data.punishment.Punishment;
import xyz.msws.defybans.data.punishment.Punishment.Key;
import xyz.msws.defybans.data.punishment.PunishmentTracker;
import xyz.msws.defybans.tracker.GuildTrackAssigner;
import xyz.msws.defybans.utils.PunishmentUtils;
import xyz.msws.defybans.utils.TimeParser;
import xyz.msws.defybans.utils.TimeParser.TimeUnit;

public class BanStatsCommand extends AbstractCommand {

	private GuildTrackAssigner assigner;

	public BanStatsCommand(SourceModBans client, String name) {
		super(client, name);
		if ((assigner = client.getModule(GuildTrackAssigner.class)) == null)
			client.getCommandListener().unregisterCommand(this);
		setAliases("bs");
		setDescription("Views specific punishment statistics");
		setUsage("<identifier/admin>");
	}

	@Override
	public void execute(Message message, String[] args) {
		PunishmentTracker tracker = assigner.getTracker(message.getGuild());
		Set<Punishment> ps = tracker.getPunishments();
		Set<String> admins = new HashSet<>();
		ps.forEach(p -> admins.add(p.get(Key.ADMIN, String.class).toLowerCase()));

		if (args.length == 0) {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setTitle("Punishment Statistics for " + message.getGuild().getName());
			builder.setAuthor("MSWS", "https://github.com/MSWS", "https://i.imgur.com/weIiWIz.png");
			builder.setColor(new Color(255, 0, 0));
			builder.appendDescription("**Punishments Within**:\n");
			builder.setFooter(
					"Requested by " + message.getAuthor().getAsTag() + "\nSourceMod Bans Tracker created by MSWS");

			for (long sec : new long[] { TimeUnit.YEARS.getSeconds(), TimeUnit.MONTHS.getSeconds(),
					TimeUnit.WEEKS.getSeconds(), TimeUnit.DAYS.getSeconds() * 3, TimeUnit.DAYS.getSeconds() }) {
				Set<Punishment> copy = new HashSet<>(ps);
				Iterator<Punishment> it = copy.iterator();
				while (it.hasNext()) {
					Punishment p = it.next();
					if (p.getDate() < System.currentTimeMillis() - sec * 1000)
						it.remove();
				}

				builder.appendDescription(TimeParser.getDurationDescription(sec, 1) + ": " + copy.size() + "\n");
			}

			Map<String, Integer> reasons = new HashMap<>();

			for (Punishment p : ps) {
				String adm = p.get(Key.ADMIN, String.class), reason = p.get(Key.REASON, String.class);
				admins.add(adm);
				reasons.put(reason, reasons.getOrDefault(reason, 0) + 1);
			}

			builder.appendDescription("\n**Admin Punishment Count**\n");
			builder.appendDescription(PunishmentUtils.rank(ps, Key.ADMIN, 5));

			builder.appendDescription("\n\n**Most Common Reasons**\n");
			builder.appendDescription(PunishmentUtils.rank(ps, Key.REASON, 5));

			builder.appendDescription(String.format("\n\n**Tracking URLs** (**%d**):\n", tracker.getTrackers().size()));
			tracker.getTrackers().forEach(t -> builder.appendDescription(MarkdownSanitizer.escape(t.getURL()) + "\n"));

			builder.addField("Total Punishments", ps.size() + "", true);
			builder.addField("Total Admins", admins.size() + "", true);
			builder.addField("Unique Reasons",
					String.format("%d/%d (%.2f%%)", reasons.size(), tracker.getPunishments().size(),
							((double) reasons.keySet().size() / (double) tracker.getPunishments().size()) * 100),
					true);

			message.getChannel().sendMessage(builder.build()).queue();
		}

		if (args.length >= 1) {
			String id = String.join(" ", args);
			if (admins.contains(id.toLowerCase())) {
				List<Punishment> values = tracker.getPunishments().stream()
						.filter(p -> p.get(Key.ADMIN, String.class).equalsIgnoreCase(id))
						.sorted(new Comparator<Punishment>() {
							public int compare(Punishment o1, Punishment o2) {
								return o1.getDate() == o2.getDate() ? 0 : o1.getDate() > o2.getDate() ? -1 : 1;
							};
						}).collect(Collectors.toList());

				EmbedBuilder builder = new EmbedBuilder();
				builder.setTitle("Punishment Statistics for " + id);
				builder.setFooter("Requested by " + message.getAuthor().getAsTag());
				builder.setColor(Color.BLUE);

				builder.appendDescription("**Most Common Reasons**\n");
				builder.appendDescription(PunishmentUtils.rank(values, Key.REASON));
				builder.appendDescription("\n\n**Most Common Players**\n");
				builder.appendDescription(PunishmentUtils.rank(values, Key.USERNAME));
				builder.appendDescription("\n\n**Most Common Durations**\n");
				builder.appendDescription(PunishmentUtils.rank(values, Key.DURATION));

				builder.addField("Total Bans", values.size() + "", true);

				long seconds = 0;
				for (Punishment p : values)
					seconds += p.getDuration();
				seconds /= values.size();
				builder.addField("Average Ban Duration", TimeParser.getDurationDescription(seconds), true);
				builder.addField("First Ban", values.get(values.size() - 1).get(Key.DATE, String.class), true);
				builder.addField("Last Ban", values.get(0).get(Key.DATE, String.class), true);
				message.getChannel().sendMessage(builder.build()).delay(1, java.util.concurrent.TimeUnit.MINUTES)
						.map(Message::delete).queue();
				return;
			}

			Key key = Key.fromString(id);
			if (key == null) {
				EmbedBuilder builder = new EmbedBuilder();
				builder.setTitle("Unknown identifier");
				builder.setColor(Color.RED);

				List<MessageEmbed> pages = new ArrayList<>();
				int i = 1; // Set to one to avoid 0 % 7 = 0
				for (Key k : Key.values()) {
					builder.appendDescription(k.getId() + "\n");
					if (i % 7 == 0) {
						pages.add(builder.build());
						builder = new EmbedBuilder();
						i = 1;
					}
					i++;
				}
				if (!builder.isEmpty())
					pages.add(builder.build());
				for (String adm : admins) {
					builder.appendDescription(adm + "\n");
					if (i % 7 == 0) {
						pages.add(builder.build());
						builder = new EmbedBuilder();
						builder.setColor(Color.RED);
						i = 1;
					}
					i++;
				}
				if (!builder.isEmpty())
					pages.add(builder.build());

				new PageableEmbed(client, pages).bindTo(message.getAuthor()).send(message.getTextChannel());
				return;
			}

			EmbedBuilder builder = new EmbedBuilder();
			builder.setTitle("Punishment Statistics by " + key.getId());
			builder.setFooter("Requested by " + message.getAuthor().getAsTag());
			builder.setColor(Color.GREEN);

			List<MessageEmbed> pages = new ArrayList<>();
			int i = 1;
			for (String line : PunishmentUtils.rankList(ps, key)) {
				builder.appendDescription(line + "\n");
				if (i % 10 == 0) {
					pages.add(builder.build());
					builder = new EmbedBuilder();
					builder.setColor(Color.GREEN);
					builder.setFooter("Requested by " + message.getAuthor().getAsTag());
					i = 1;
				}
				i++;
			}
			if (!builder.isEmpty())
				pages.add(builder.build());

			new PageableEmbed(client, pages).bindTo(message.getAuthor()).send(message.getTextChannel());
		}
	}

}
