package xyz.msws.defybans.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import xyz.msws.defybans.Client;
import xyz.msws.defybans.data.ServerConfig;

public class SetChannelCommand extends AbstractCommand {

	public SetChannelCommand(Client client, String name) {
		super(client, name);
		setUsage("[channel]");
		setAliases("sc");
		setPermission(Permission.BAN_MEMBERS);
		setDescription("Sets the channel that punishments are logged to.");
	}

	@Override
	public void execute(Message message, String[] args) {
		if (args.length != 1) {
			message.getChannel().sendMessage("Please specify a channel to log punishments to.").queue();
			return;
		}

		TextChannel channel = null;

		if (message.getMentionedChannels().size() == 1) {
			channel = message.getMentionedChannels().get(0);
		} else {
			for (GuildChannel c : message.getGuild().getChannels()) {
				if (c.getType() != ChannelType.TEXT)
					continue;
				if (c.getName().equalsIgnoreCase(args[0])) {
					channel = (TextChannel) c;
					break;
				}
			}
		}

		if (channel == null) {
			message.getChannel().sendMessage("Unknown channel specified.").queue();
			return;
		}

		ServerConfig config = new ServerConfig(message.getGuild().getIdLong());
		config.setChannelID(channel.getIdLong());
		config.save();
		message.getChannel().sendMessageFormat("Successfully set the log channel to %s", channel.getAsMention())
				.queue();
	}

}
