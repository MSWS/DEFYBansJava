package xyz.msws.defybans.tracker;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import xyz.msws.defybans.Client;
import xyz.msws.defybans.data.FileStorage;
import xyz.msws.defybans.data.PunishmentStorage;
import xyz.msws.defybans.data.punishment.PunishmentTracker;
import xyz.msws.defybans.module.Module;

public class GuildTrackAssigner extends Module {

	public GuildTrackAssigner(Client client) {
		super(client);
	}

	private Map<Long, PunishmentTracker> trackers = new HashMap<>();

	@Override
	public void load() {
		trackers = new HashMap<>();

		for (Guild g : client.getJDA().getGuilds()) {
			assignTracker(g);
		}
	}

	public void assignTracker(Guild g) {
		File data = new File(System.getProperty("user.dir") + File.separator + g.getIdLong() + ".txt");

		if (!data.exists()) {
			try {
				data.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		PunishmentStorage save = new FileStorage(data);

		TextChannel channel = null;
		for (TextChannel c : g.getTextChannels()) {
			if (c.getName().toLowerCase().contains("bans")) {
				channel = c;
				break;
			}
		}
		if (channel == null) {
			g.getSelfMember().modifyNickname("Disabled").queue();
			g.getOwner().getUser().openPrivateChannel().queue(msg -> {
				msg.sendMessage("");
			});
			return;
		}

		PunishmentTracker tracker = new PunishmentTracker(channel, save);
		trackers.put(g.getIdLong(), tracker);
		tracker.addTracker(new BanTracker("https://bans.defyclan.com/index.php?p=banlist", tracker));
	}

	@Override
	public void unload() {

	}

	public PunishmentTracker getTracker(Guild g) {
		if (!trackers.containsKey(g.getIdLong())) {
			assignTracker(g);
		}

		return trackers.get(g.getIdLong());
	}

}
