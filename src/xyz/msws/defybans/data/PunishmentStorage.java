package xyz.msws.defybans.data;

import java.util.Set;

import xyz.msws.defybans.data.punishment.Punishment;

public interface PunishmentStorage extends Saveable {

	void queryPunishments(Callback<Set<Punishment>> result);

	/**
	 * @deprecated
	 * @return
	 */
	Set<Punishment> getPunishments();

	default void addPunishment(Punishment p) {
		addPunishment(p, false);
	}

	void deletePunishment(Punishment p);

	void addPunishment(Punishment p, boolean save);

}
