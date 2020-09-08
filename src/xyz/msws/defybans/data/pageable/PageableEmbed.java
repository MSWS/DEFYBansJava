package xyz.msws.defybans.data.pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import xyz.msws.defybans.Client;

public class PageableEmbed extends Pageable<MessageEmbed> {

	private PageableEmbed(Client client) {
		super(client);
		this.client = client;
		client.getJDA().addEventListener(this);
	}

	public PageableEmbed(Client client, List<MessageEmbed> pages) {
		this(client);
		this.pages = pages;
	}

	public PageableEmbed(Client client, MessageEmbed... msgs) {
		this(client);
		this.pages = new ArrayList<>();
		Collections.addAll(pages, msgs);
	}

	public PageableEmbed(Client client, String... pages) {
		this(client);
		this.pages = new ArrayList<>();
		for (String s : pages)
			this.pages.add(new EmbedBuilder().setDescription(s).build());
	}

	public void send(TextChannel channel, int page) {
		if (id != 0) {
			channel.editMessageById(id, pages.get(page)).queue();

			channel.retrieveMessageById(this.id).queue(m -> {
				if (this.page < 5) {
					m.removeReaction("⏪").queue();
					if (this.page <= 1) {
						m.removeReaction("⬅").queue();
						if (this.page == 0)
							m.removeReaction("◀").queue();
					}
				}

				if (this.page > pages.size() - 5) {
					m.removeReaction("⏩").queue();
					if (this.page >= pages.size() - 2) {
						m.removeReaction("➡").queue();
						if (this.page == pages.size() - 1)
							m.removeReaction("▶").queue();
					}
				}

				if (this.page > 0) {
					m.addReaction("◀").queue();
					if (this.page > 1) {
						m.addReaction("⬅").queue();
					}
					if (this.page > 5)
						m.addReaction("⏪").queue();
				}

				if (this.page < pages.size() - 1) {
					m.addReaction("▶").queue();
					if (this.page < pages.size() - 2) {
						m.addReaction("➡").queue();
						if (this.page < pages.size() - 5)
							m.addReaction("⏩").queue();
					}
				}

			});
			return;
		}

		this.page = page;
		channel.sendMessage(pages.get(page)).queue(m -> {
			this.id = m.getIdLong();
			if (this.page > 0) {
				m.addReaction("◀").queue();
				if (this.page > 1) {
					m.addReaction("⬅").queue();
				}
				if (this.page > 5)
					m.addReaction("⏪").queue();
			}

			m.addReaction("❌").queue();

			if (this.page < pages.size() - 1) {
				m.addReaction("▶").queue();
				if (this.page < pages.size() - 2) {
					m.addReaction("➡").queue();
					if (this.page < pages.size() - 5)
						m.addReaction("⏩").queue();
				}
			}
		});
	}

}
