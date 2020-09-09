package xyz.msws.defybans.commands;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import xyz.msws.defybans.Client;

public class TestRegexCommand extends AbstractCommand {

	public TestRegexCommand(Client client, String name) {
		super(client, name);
		setDescription("Tests the regex parser");
		setPermission(Permission.MESSAGE_MANAGE);
		setUsage("[set/test] [Regex/Value]");
		setAliases("regex", "tr");
	}

	private Map<User, Pattern> patterns = new HashMap<>();

	@Override
	public void execute(Message message, String[] args) {
		if (args.length == 0) {
			message.getChannel().sendMessage("Please specify if would like to **set** or **test** and the value/regex.")
					.queue();
			return;
		}

		if (args.length == 1) {
			message.getChannel().sendMessage("Please specify a value.").queue();
			return;
		}

		Pattern patt;
		String value = String.join(" ", args);
		value = value.substring(value.indexOf(" ") + 1);

		switch (args[0].toLowerCase()) {
			case "set":

				try {
					patt = Pattern.compile(value);
				} catch (PatternSyntaxException e) {
					message.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setTitle("Error")
							.setDescription(e.getMessage()).build()).queue();
					return;
				}
				patterns.put(message.getAuthor(), patt);
				message.getChannel()
						.sendMessage(
								"Successfully set the regex to `" + MarkdownSanitizer.escape(patt.toString()) + "`")
						.queue();

				break;
			case "test":
				patt = patterns.get(message.getAuthor());
				if (patt == null) {
					message.getChannel().sendMessage("You have not set a regex! Set one with " + client.getPrefix()
							+ this.getName() + " set [regex]").queue();
					return;
				}

				message.getChannel()
						.sendMessageFormat("%s **DOES %s** match %s.", MarkdownSanitizer.escape(patt.toString()),
								patt.matcher(value).find() ? "" : "NOT", MarkdownSanitizer.escape(value))
						.queue();

				break;
			default:
				message.getChannel().sendMessageFormat("%s is not set/test", args[0]).queue();
				break;
		}

	}

}
