package xyz.msws.defybans.commands;

import java.util.Arrays;

import net.dv8tion.jda.api.entities.Message;
import xyz.msws.defybans.DEFYBansClient;
import xyz.msws.defybans.data.pageable.Pageable;

public class BanStatsCommand extends AbstractCommand {

	public BanStatsCommand(DEFYBansClient client, String name) {
		super(client, name);
		setAliases(Arrays.asList("bs"));
	}

	@Override
	public void execute(Message message, String[] args) {
		if (args.length == 0) {
			Pageable page = new Pageable(client, "this", "is", "a", "test", "message");
			page.bindTo(message.getMember().getUser());
			page.send(message.getTextChannel());
		}
	}

}
