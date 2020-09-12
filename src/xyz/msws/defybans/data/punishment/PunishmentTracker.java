package xyz.msws.defybans.data.punishment;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.entities.TextChannel;
import xyz.msws.defybans.data.Callback;
import xyz.msws.defybans.data.PunishmentStorage;
import xyz.msws.defybans.data.punishment.Punishment.Key;
import xyz.msws.defybans.tracker.Tracker;
import xyz.msws.defybans.utils.TimeParser;

public class PunishmentTracker {
	private Set<Punishment> punishments = new HashSet<>();

	private List<Tracker> trackers = new ArrayList<>();

	private PunishmentStorage data;
	private TextChannel channel;

	public PunishmentTracker(TextChannel channel, PunishmentStorage data) {
		this.data = data;
		this.channel = channel;
		data.load();

		data.queryPunishments(new Callback<Set<Punishment>>() {
			@Override
			public void execute(Set<Punishment> call) {
				call.forEach(p -> register(p));
				channel.getManager().setTopic(String.format("Last updated %s Total Bans: %d",
						TimeParser.getDateDescription(System.currentTimeMillis()), call.size())).queue();
			}
		});
	}

	public void addTracker(Tracker tracker) {
		if (!tracker.running())
			tracker.start();
		trackers.add(tracker);
	}

	public List<Tracker> getTrackers() {
		return trackers;
	}

	private void register(Punishment punish) {
		punishments.add(punish);
	}

	public void addPunishment(Punishment punish) {
		Punishment old = null;
		for (Punishment p : punishments) {
			if (p.isSimilar(punish)) {
				old = p;
				break;
			}
		}

		if (old != null && old.equals(punish)) // Same duplicate punishment
			return;

		this.channel.getManager().setTopic(String.format("Last Updated %s Total Bans: %d",
				TimeParser.getDateDescription(System.currentTimeMillis()), punishments.size())).queue();

		this.channel.sendMessage(punish.createEmbed(old)).queue();
		punishments.remove(old);
		data.deletePunishment(old);
		punishments.add(punish);
		data.addPunishment(punish, true);
	}

	public void delete(Punishment punish, boolean save) {
		punishments.remove(punish);
		if (save)
			data.save();
	}

	public void delete(Punishment punish) {
		delete(punish, false);
	}

	public Set<Punishment> getPunishments(EnumMap<Key, Object> filters) {
		Set<Punishment> result = new HashSet<>(punishments);

		for (Entry<Key, Object> entry : filters.entrySet()) {
			Iterator<Punishment> it = result.iterator();
			while (it.hasNext()) {
				Punishment p = it.next();
				if (!entry.getValue().equals(p.getData().get(entry.getKey())))
					it.remove();
			}
		}

		return result;
	}

	public Set<Punishment> getPunishmentsRegex(EnumMap<Key, Pattern> filters) {
		Set<Punishment> result = new HashSet<>(punishments);

		for (Entry<Key, Pattern> entry : filters.entrySet()) {
			Iterator<Punishment> it = result.iterator();
			while (it.hasNext()) {
				Punishment p = it.next();
				if (!p.getData().containsKey(entry.getKey())) {
					it.remove();
					continue;
				}
				String value = p.get(entry.getKey(), String.class);
				if (!entry.getValue().matcher(value).find())
					it.remove();
			}
		}

		return result;
	}

	public Set<Punishment> getPunishments() {
		return punishments;
	}
}
