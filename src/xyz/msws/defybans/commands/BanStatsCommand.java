package xyz.msws.defybans.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import xyz.msws.defybans.DEFYBansClient;
import xyz.msws.defybans.data.pageable.PageableEmbed;
import xyz.msws.defybans.data.punishment.Punishment;
import xyz.msws.defybans.data.punishment.Punishment.Key;
import xyz.msws.defybans.data.punishment.PunishmentTracker;
import xyz.msws.defybans.tracker.GuildTrackAssigner;
import xyz.msws.defybans.utils.TimeParser;
import xyz.msws.defybans.utils.TimeParser.TimeUnit;

public class BanStatsCommand extends AbstractCommand {

	private GuildTrackAssigner assigner;

	public BanStatsCommand(DEFYBansClient client, String name) {
		super(client, name);
		if ((assigner = client.getModule(GuildTrackAssigner.class)) == null)
			client.getCommandListener().unregisterCommand(this);
		setAliases(Arrays.asList("bs"));
	}

	@Override
	public void execute(Message message, String[] args) {
		PunishmentTracker tracker = assigner.getTracker(message.getGuild());

		if (args.length == 0) {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setTitle("Ban Statistics for " + message.getGuild().getName());
			builder.setAuthor("MSWS", "https://github.com/MSWS", "https://i.imgur.com/weIiWIz.png");
			builder.setColor(new Color(255, 0, 0));
			builder.appendDescription("**Bans Within**:\n");
			builder.setFooter("SourceMod Bans Tracker created by MSWS");
			Set<Punishment> ps = tracker.getPunishments();

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

			Map<String, Integer> admins = new HashMap<>(), reasons = new HashMap<>();

			for (Punishment p : ps) {
				String adm = p.get(Key.ADMIN, String.class), reason = p.get(Key.REASON, String.class);
				admins.put(adm, admins.getOrDefault(adm, 0) + 1);
				reasons.put(reason, reasons.getOrDefault(reason, 0) + 1);
			}

			admins = sortByValue(admins);
			reasons = sortByValue(reasons);

			builder.appendDescription("\n**Admin Ban Count**\n");
			int i = 0;
			for (Entry<String, Integer> entry : admins.entrySet()) {
				builder.appendDescription(MarkdownSanitizer.escape(entry.getKey()) + ": " + entry.getValue() + "\n");
				i++;
				if (i > 4)
					break;
			}

			builder.appendDescription("\n**Popular Reasons**\n");
			i = 0;
			for (Entry<String, Integer> entry : reasons.entrySet()) {
				builder.appendDescription(MarkdownSanitizer.escape(entry.getKey()) + ": " + entry.getValue() + "\n");
				i++;
				if (i > 4)
					break;
			}

			builder.addField("Total Bans", ps.size() + "", true);
			builder.addField("Total Admins", admins.size() + "", true);
			builder.addField("Unique Reasons",
					String.format("%d/%d (%.2f%%)", reasons.size(), tracker.getPunishments().size(),
							((double) reasons.keySet().size() / (double) tracker.getPunishments().size()) * 100),
					true);

			message.getChannel().sendMessage(builder.build()).queue();
		}

		if (args.length >= 1) {
			Key key = Key.fromString(String.join(" ", args));
			if (key == null) {
				EmbedBuilder builder = new EmbedBuilder();
				builder.appendDescription("Unknown identifier, valid identifiers:\n");

				List<MessageEmbed> pages = new ArrayList<>();
				int i = 1;
				for (Key k : Key.values()) {
					builder.appendDescription(k.getId() + "\n");
					if (i % 7 == 0) {
						pages.add(builder.build());
						builder = new EmbedBuilder();
						i = 1;
					}
					i++;
				}

				new PageableEmbed(client, pages).send(message.getTextChannel());
			}
		}

	}

	private <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
		list.sort(Entry.comparingByValue());

		Map<K, V> result = new LinkedHashMap<>();
		for (Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}

}
