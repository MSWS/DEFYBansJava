package xyz.msws.defybans.tracker;

import org.json.JSONObject;

import xyz.msws.defybans.data.punishment.PunishmentManager;
import xyz.msws.defybans.data.punishment.Punishment.Type;

public class TrackerFactory {

	private PunishmentManager manager;

	public TrackerFactory(PunishmentManager manager) {
		this.manager = manager;
	}

	public Tracker createTracker(JSONObject obj) {
		if (!obj.has("url") || !obj.has("type"))
			throw new IllegalArgumentException(obj.toString() + " does not contain url/type");
		Type type = obj.getEnum(Type.class, "type");
		return createTracker(type, obj.getString("url"));
	}

	public Tracker createTracker(Type type, String URL) {
		switch (type) {
			case BAN:
				return new BanTracker(URL, manager);
			default:
				return null;
		}
	}
}
