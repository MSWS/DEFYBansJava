package xyz.msws.defybans.commands;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import xyz.msws.defybans.Client;

public abstract class AbstractCommand {

	protected Client client;
	protected String name;
	protected Permission perm;
	protected String usage;

	protected List<String> aliases = new ArrayList<>();

	public AbstractCommand(Client client, String name) {
		this.client = client;
		this.name = name;
	}

	public abstract void execute(Message message, String[] args);

	public boolean checkPermission(Message message) {
		return checkPermission(message, true);
	}

	public boolean checkPermission(Message message, boolean verbose) {
		if (perm == null)
			return true;
		if (!message.getMember().hasPermission(perm)) {
			if (verbose) {
				message.getChannel()
						.sendMessageFormat("The **%s** command requires the **%s** permission.", name, perm.getName())
						.queue();
			}
			return false;
		}
		return true;
	}

	public String getName() {
		return name;
	}

	public String getUsage() {
		return this.usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public void setAliases(List<String> aliases) {
		this.aliases = aliases;
	}

	public List<String> getAliases() {
		return aliases;
	}

	public void setPermission(Permission permission) {
		this.perm = permission;
	}

	public Permission getPermission() {
		return perm;
	}

}
