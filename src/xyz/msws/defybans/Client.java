package xyz.msws.defybans;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import xyz.msws.defybans.commands.CommandListener;

/**
 * Represents a discord client
 * 
 * @author imodm
 *
 */
public abstract class Client {
	protected String prefix;
	protected String token;
	protected JDA jda;
	protected CommandListener commands;

	public Client(String token) {
		this.token = token;
		commands = new CommandListener(this);
	}

	public abstract void start();

	public JDA getJDA() {
		return jda;
	}

	/**
	 * Initiates a shutdown
	 */
	public void shutdown() {
		jda.shutdown();
	}

	/**
	 * Returns true if the client is still connected
	 * 
	 * @return
	 */
	public boolean isOnline() {
		return jda.getStatus() == Status.CONNECTED;
	}

	/**
	 * Returns the client's name
	 * 
	 * @return
	 */
	public abstract String getName();

	/**
	 * Returns the client's prefix
	 */
	public abstract String getPrefix();

}
