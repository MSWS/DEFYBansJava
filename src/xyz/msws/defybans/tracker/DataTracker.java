package xyz.msws.defybans.tracker;

import java.io.File;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import xyz.msws.defybans.data.ServerConfig;

public class DataTracker extends ListenerAdapter {
	private GuildTrackAssigner assigner;

	public DataTracker(GuildTrackAssigner assigner) {
		this.assigner = assigner;
	}

	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		delete(event.getGuild().getIdLong());
	}

	@Override
	public void onUnavailableGuildLeave(UnavailableGuildLeaveEvent event) {
		delete(event.getGuildIdLong());
	}

	private void delete(long id) {
		assigner.clearData(id);
		File file = assigner.getDataFile(id);
		file.delete();
		ServerConfig config = new ServerConfig(id);
		config.getFile().delete();
	}

	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		assigner.assignManager(event.getGuild());
	}
}
