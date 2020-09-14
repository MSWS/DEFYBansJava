package xyz.msws.defybans.tracker;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import xyz.msws.defybans.Client;
import xyz.msws.defybans.data.FileStorage;
import xyz.msws.defybans.data.PunishmentStorage;
import xyz.msws.defybans.data.ServerConfig;
import xyz.msws.defybans.data.punishment.PunishmentManager;
import xyz.msws.defybans.module.Module;

public class GuildTrackAssigner extends Module {

	public GuildTrackAssigner(Client client) {
		super(client);
	}

	private Map<Long, PunishmentManager> trackers = new HashMap<>();
	private Map<Long, TrackerFactory> factories = new HashMap<>();

	@Override
	public void load() {
		for (Guild g : client.getJDA().getGuilds())
			factories.put(g.getIdLong(), new TrackerFactory(assignTracker(g)));
	}

	public PunishmentManager assignTracker(Guild g) {
		File data = new File(System.getProperty("user.dir") + File.separator + g.getIdLong() + ".txt");

		if (!data.exists()) {
			try {
				data.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		PunishmentStorage save = new FileStorage(data);
		ServerConfig config = new ServerConfig(g.getIdLong());

		TextChannel channel = g.getTextChannelById(config.getChannelID());
		if (channel == null)
			for (TextChannel c : g.getTextChannels()) {
				if (c.getName().toLowerCase().contains("bans")) {
					channel = c;
					config.setChannelID(channel.getIdLong());
					config.save();
					break;
				}
			}
		if (channel == null) {
			g.getSelfMember().modifyNickname("Disabled").queue();
			g.getOwner().getUser().openPrivateChannel().queue(msg -> {
				msg.sendMessage("");
			});
			return null;
		}

		PunishmentManager manager = new PunishmentManager(channel, save);
		trackers.put(g.getIdLong(), manager);
		factories.put(g.getIdLong(), new TrackerFactory(manager));

		JSONArray ts = config.getTrackerInfo();
		if (ts == null || ts.isEmpty())
			return manager;
		for (int i = 0; i < ts.length(); i++) {
			manager.addTracker(factories.get(g.getIdLong()).createTracker(ts.getJSONObject(i)), false);
		}

		return manager;
	}

	public TrackerFactory getFactory(long guild) {
		return factories.get(guild);
	}

	@Override
	public void unload() {

	}

	public PunishmentManager getManager(Guild g) {
		if (!trackers.containsKey(g.getIdLong())) {
			assignTracker(g);
		}

		return trackers.get(g.getIdLong());
	}

}