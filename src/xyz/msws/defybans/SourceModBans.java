package xyz.msws.defybans;

import java.util.ArrayList;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import xyz.msws.defybans.commands.BanStatsCommand;
import xyz.msws.defybans.commands.DeleteBanCommand;
import xyz.msws.defybans.commands.HelpCommand;
import xyz.msws.defybans.commands.InfoCommand;
import xyz.msws.defybans.commands.ListBansCommand;
import xyz.msws.defybans.commands.PingCommand;
import xyz.msws.defybans.commands.TestRegexCommand;
import xyz.msws.defybans.commands.UptimeCommand;
import xyz.msws.defybans.tracker.GuildTrackAssigner;

public class SourceModBans extends Client {

	public SourceModBans(String token) {
		super(token);
	}

	@Override
	public String getName() {
		return "SourceMod Bans";
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
			MessageAction.setDefaultMentions(new ArrayList<>());

			commands.registerCommand(new PingCommand(this, "ping"));
			commands.registerCommand(new BanStatsCommand(this, "banstats"));
			commands.registerCommand(new ListBansCommand(this, "listbans"));
			commands.registerCommand(new UptimeCommand(this, "uptime"));
			commands.registerCommand(new DeleteBanCommand(this, "deletebans"));
			commands.registerCommand(new InfoCommand(this, "info"));
			commands.registerCommand(new TestRegexCommand(this, "testregex"));
			commands.registerCommand(new HelpCommand(this, "help"));

		} catch (LoginException | InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getPrefix() {
		return "!";
	}

}
