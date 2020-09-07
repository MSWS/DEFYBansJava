package xyz.msws.defybans.data;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.dv8tion.jda.api.entities.TextChannel;
import xyz.msws.defybans.data.Punishment.Key;

public class PunishmentTracker {
	private Map<String, List<Punishment>> punishments = new HashMap<>();

	private Save data;
	private TextChannel channel;

	public PunishmentTracker(TextChannel channel, Save data) {
		this.data = data;
		this.channel = channel;
		data.load();

		data.queryPunishments(new Callback<Collection<Punishment>>() {
			long time = System.currentTimeMillis();

			@Override
			public void execute(Collection<Punishment> call) {
				call.forEach(p -> register(p));
				channel.sendMessageFormat("Successfully loaded %d punishments (took %s ms).", call.size(),
						System.currentTimeMillis() - time).delay(Duration.ofSeconds(10)).flatMap(m -> m.delete())
						.queue();
			}
		});
	}

	private void register(Punishment punish) {
		List<Punishment> ps = punishments.getOrDefault(punish.get(Key.STEAMID), new ArrayList<>());
		ps.add(punish);
		punishments.put(punish.get(Key.STEAMID, String.class), ps);
	}

	public void addPunishment(Punishment punish) {
		List<Punishment> ps = punishments.getOrDefault(punish.get(Key.STEAMID), new ArrayList<>());

		Punishment old = null;
		for (Punishment p : ps) {
			if (p.isSimilar(punish)) {
				old = p;
				break;
			}
		}

		if (old != null && old.equals(punish)) // Same duplicate punishment
			return;

		this.channel.sendMessage(punish.createEmbed(old)).queue();
		ps.remove(punish);
		ps.add(punish);
		punishments.put(punish.get(Key.STEAMID, String.class), ps);
		data.addPunishment(punish, true);
	}
}
