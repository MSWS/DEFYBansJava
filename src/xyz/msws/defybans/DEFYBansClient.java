package xyz.msws.defybans;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import xyz.msws.defybans.commands.BanStatsCommand;
import xyz.msws.defybans.commands.ListBansCommand;
import xyz.msws.defybans.commands.PingCommand;
import xyz.msws.defybans.tracker.GuildTrackAssigner;

public class DEFYBansClient extends Client {

	public DEFYBansClient(String token) {
		super(token);
	}

	@Override
	public String getName() {
		return "DEFYBans";
	}

	@Override
	public void start() {
		modules.add(new GuildTrackAssigner(this));

		try {
			this.jda = JDABuilder.createDefault(token).build();
			jda.awaitReady();
			loadModules();

			jda.addEventListener(commands);
			jda.getPresence().setActivity(Activity.watching("SourceMod Bans"));

			commands.registerCommand(new PingCommand(this, "ping"));
			commands.registerCommand(new BanStatsCommand(this, "banstats"));
			commands.registerCommand(new ListBansCommand(this, "listbans"));

		} catch (LoginException | InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getPrefix() {
		return "!";
	}

}
