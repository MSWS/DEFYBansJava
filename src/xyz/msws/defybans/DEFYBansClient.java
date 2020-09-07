package xyz.msws.defybans;

import java.io.File;
import java.io.IOException;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import xyz.msws.defybans.commands.PingCommand;
import xyz.msws.defybans.data.FileSave;
import xyz.msws.defybans.data.PunishmentTracker;
import xyz.msws.defybans.data.Save;
import xyz.msws.defybans.tracker.BanTracker;

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

		try {
			this.jda = JDABuilder.createDefault(token).build();
			jda.awaitReady();
			jda.addEventListener(commands);

			commands.registerCommand(new PingCommand(this, "ping"));

		} catch (LoginException | InterruptedException e) {
			e.printStackTrace();
		}

		for (Guild g : jda.getGuilds()) {
			File data = new File(System.getProperty("user.dir") + File.separator + g.getIdLong() + ".txt");

			if (!data.exists()) {
				try {
					data.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			Save save = new FileSave(data);

			TextChannel channel = null;
			for (TextChannel c : g.getTextChannels()) {
				if (c.getName().toLowerCase().contains("bans")) {
					channel = c;
					break;
				}
			}
			if (channel == null) {
				g.getSelfMember().modifyNickname("Disabled").queue();
				return;
			}

			PunishmentTracker tracker = new PunishmentTracker(channel, save);
			BanTracker timer = new BanTracker("https://bans.defyclan.com/index.php?p=banlist", tracker);
			timer.start();

		}

	}

	@Override
	public String getPrefix() {
		return "!";
	}

}
