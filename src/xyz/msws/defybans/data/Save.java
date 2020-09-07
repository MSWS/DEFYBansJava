package xyz.msws.defybans.data;

import java.util.Collection;

public interface Save {
	void save();

	void load();

	void queryPunishments(Callback<Collection<Punishment>> result);

	/**
	 * @deprecated
	 * @return
	 */
	Collection<Punishment> getPunishments();

	default void addPunishment(Punishment p) {
		addPunishment(p, false);
	}

	void addPunishment(Punishment p, boolean save);

}
