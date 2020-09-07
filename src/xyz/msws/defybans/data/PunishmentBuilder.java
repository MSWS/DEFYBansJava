package xyz.msws.defybans.data;

import java.util.EnumMap;
import java.util.EnumSet;

import xyz.msws.defybans.data.Punishment.Key;

public class PunishmentBuilder {
	private EnumMap<Key, Object> data;

	public PunishmentBuilder(Punishment.Type type) {
		this.data = new EnumMap<>(Key.class);
		data.put(Key.TYPE, type.toString());
	}

	public PunishmentBuilder player(String name) {
		data.put(Key.USERNAME, name);
		return this;
	}

	public PunishmentBuilder steam(String steam) {
		data.put(Key.STEAMID, steam);
		return this;
	}

	public PunishmentBuilder admin(String admin) {
		data.put(Key.ADMIN, admin);
		return this;
	}

	public PunishmentBuilder reason(String reason) {
		data.put(Key.REASON, reason);
		return this;
	}

	public PunishmentBuilder set(Key key, String value) {
		data.put(key, value);
		return this;
	}

	public Punishment build() {
		return new Punishment(data);
	}

	private EnumSet<Key> required = EnumSet.of(Key.USERNAME, Key.ADMIN, Key.DATE, Key.TYPE);

	public boolean canBuild() {
		return data.keySet().containsAll(required);
	}
}