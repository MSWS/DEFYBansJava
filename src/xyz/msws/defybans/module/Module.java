package xyz.msws.defybans.module;

import xyz.msws.defybans.Client;

public abstract class Module {
	protected Client client;

	public Module(Client client) {
		this.client = client;
	}

	public abstract void load();

	public abstract void unload();

}
