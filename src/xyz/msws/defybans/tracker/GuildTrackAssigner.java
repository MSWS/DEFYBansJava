package xyz.msws.defybans.tracker;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

import net.dv8tion.jda.api.Permission;
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

	private Map<Long, PunishmentManager> managers = new HashMap<>();
	private Map<Long, TrackerFactory> factories = new HashMap<>();

	@Override
	public void load() {
		for (Guild g : client.getJDA().getGuilds()) {
			PunishmentManager mgr = assignManager(g);
			if (mgr == null)
				continue;
			factories.put(g.getIdLong(), new TrackerFactory(mgr));
		}
	}

	public File getDataFile(long g) {
		File data = new File(System.getProperty("user.dir") + File.separator + g + ".txt");
		return data;
	}

	public void clearData(long g) {
		managers.remove(g);
	}

	public PunishmentManager assignManager(Guild g) {
		if (managers.containsKey(g.getIdLong()))
			return managers.get(g.getIdLong());
		File data = getDataFile(g.getIdLong());

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
				if (!c.canTalk())
					continue;
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
				msg.sendMessage(
						"Unable to find a proper channel to send messages to, set one with !setchannel [channel]")
						.queue();
			});
			return null;
		}

		if (!channel.canTalk()) {
			if (g.getSelfMember().hasPermission(Permission.NICKNAME_CHANGE))
				g.getSelfMember().modifyNickname("Disabled").queue();
			final TextChannel c = channel;
			g.retrieveOwner().queue(owner -> owner.getUser().openPrivateChannel().queue(msg -> {
				msg.sendMessageFormat("No permission to send messages in the %s channel.", c.getAsMention()).queue();
			}));
			return null;
		}

		PunishmentManager manager = new PunishmentManager(channel, save);
		managers.put(g.getIdLong(), manager);
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
		if (!managers.containsKey(g.getIdLong())) {
			assignManager(g);
		}

		return managers.get(g.getIdLong());
	}

}