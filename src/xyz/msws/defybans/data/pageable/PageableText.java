package xyz.msws.defybans.data.pageable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
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

	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		ReactionEmote react = event.getReactionEmote();
		if (event.getUserIdLong() == client.getJDA().getSelfUser().getIdLong())
			return;
		if (member != null && event.getMember().getIdLong() != member.getIdLong()) {
			if (react.isEmote()) {
				event.retrieveMessage().queue(msg -> msg.removeReaction(react.getEmoji(), event.getUser()).queue());
			} else {
				event.retrieveMessage().queue(msg -> msg.removeReaction(react.getEmoji(), event.getUser()).queue());
			}
			return;
		}

		if (!react.isEmoji()) {
			event.retrieveMessage().queue(msg -> msg.removeReaction(react.getEmoji(), event.getUser()).queue());
			return;
		}
		event.retrieveMessage().queue(msg -> msg.removeReaction(react.getEmoji(), event.getUser()).queue());

		switch (react.getEmoji()) {
			case "▶":
				this.page = Math.min(page + 1, pages.size() - 1);
				break;
			case "➡":
				this.page += (double) pages.size() / 5.0;
				this.page = Math.min(page, pages.size() - 1);
				break;
			case "⏩":
				this.page = pages.size() - 1;
				break;
			case "◀":
				this.page = Math.max(page - 1, 0);
				break;
			case "⬅":
				this.page -= (double) pages.size() / 5.0;
				this.page = Math.max(page, 0);
				break;
			case "⏪":
				this.page = 0;
				break;
			case "❌":
				event.retrieveMessage().queue(m -> m.delete().queue());
				return;
			default:
				System.out.println("Unknown emoji: " + react.getEmoji());
				return;
		}
		send(event.getChannel());
	}

}
