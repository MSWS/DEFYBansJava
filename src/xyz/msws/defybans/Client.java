package xyz.msws.defybans;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import xyz.msws.defybans.commands.CommandListener;
import xyz.msws.defybans.module.Module;

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
	protected List<Module> modules = new ArrayList<>();

	public Client(String token) {
		this.token = token;
		commands = new CommandListener(this);
	}

	public void loadModules() {
		modules.forEach(Module::load);
	}

	public <T extends Module> T getModule(Class<T> c) {
		for (Module m : modules) {
			if (m.getClass().isAssignableFrom(c))
				return c.cast(m);
		}
		return null;
	}

	public abstract void start();

	public JDA getJDA() {
		return jda;
	}

	/**
	 * Initiates a shutdown
	 */
	public void shutdown() {
		modules.forEach(Module::unload);
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

	public CommandListener getCommandListener() {
		return commands;
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
