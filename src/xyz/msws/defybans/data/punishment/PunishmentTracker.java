package xyz.msws.defybans.data.punishment;

import java.time.Duration;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.entities.TextChannel;
import xyz.msws.defybans.data.Callback;
import xyz.msws.defybans.data.Save;
import xyz.msws.defybans.data.punishment.Punishment.Key;

public class PunishmentTracker {
//	private Map<String, Set<Punishment>> punishments = new HashMap<>();
	private Set<Punishment> punishments = new HashSet<>();

	private Save data;
	private TextChannel channel;

	public PunishmentTracker(TextChannel channel, Save data) {
		this.data = data;
		this.channel = channel;
		data.load();

		data.queryPunishments(new Callback<Set<Punishment>>() {
			long time = System.currentTimeMillis();

			@Override
			public void execute(Set<Punishment> call) {
				call.forEach(p -> register(p));
				channel.sendMessageFormat("Successfully loaded %d punishments (took %s ms).", call.size(),
						System.currentTimeMillis() - time).delay(Duration.ofSeconds(10)).flatMap(m -> m.delete())
						.queue();
			}
		});
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

		this.channel.sendMessage(punish.createEmbed(old)).queue();
		punishments.remove(punish);
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

	public Set<Punishment> getPunishmentsRegex(EnumMap<Key, Object> filters) {
		Set<Punishment> result = new HashSet<>(punishments);

		for (Entry<Key, Object> entry : filters.entrySet()) {
			Iterator<Punishment> it = result.iterator();
			while (it.hasNext()) {
				Punishment p = it.next();
				String value = p.get(entry.getKey(), String.class);

				if (!Pattern.matches((String) entry.getValue(), value))
					it.remove();
			}
		}

		return result;
	}

	public Set<Punishment> getPunishments() {
		return punishments;
	}
}
