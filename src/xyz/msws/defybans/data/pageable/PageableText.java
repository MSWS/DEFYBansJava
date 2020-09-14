package xyz.msws.defybans.data.pageable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import xyz.msws.defybans.Client;

public class PageableText extends Pageable<Message> {

	private PageableText(Client client) {
		super(client);
	}

	public PageableText(Client client, Collection<Message> pages) {
		this(client);
		this.pages = new ArrayList<>(pages);
	}

	public PageableText(Client client, Message... msgs) {
		this(client);
		this.pages = new ArrayList<>();
		Collections.addAll(pages, msgs);
	}

	public PageableText(Client client, String... pages) {
		this(client);
		this.pages = new ArrayList<>();
		for (String s : pages)
			this.pages.add(new MessageBuilder(s).build());
	}

	public void send(TextChannel channel, int page) {
		if (this.id != 0) {
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
			for (String s : confirms.keySet())
				m.addReaction(s).queue();
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
	}
}
