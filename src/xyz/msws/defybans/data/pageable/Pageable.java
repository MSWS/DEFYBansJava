package xyz.msws.defybans.data.pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import xyz.msws.defybans.Client;

public class Pageable extends ListenerAdapter {

	private User member;
//	private Client client;

	private List<Message> pages;

	private int page = 0;
	private long id;

	public Pageable(Client client, List<Message> pages) {
		client.getJDA().addEventListener(this);
		this.pages = pages;
	}

	public Pageable(Client client, Message... msgs) {
		client.getJDA().addEventListener(this);
		this.pages = new ArrayList<>();
		Collections.addAll(pages, msgs);
	}

	public Pageable(Client client, String... pages) {
		client.getJDA().addEventListener(this);
		this.pages = new ArrayList<>();
		for (String s : pages)
			addPage(s);
	}

	public void bindTo(User member) {
		this.member = member;
	}

	public void addPage(Message message) {
		pages.add(message);
	}

	public void addPage(String text) {
		Message msg = new MessageBuilder(text).build();
		pages.add(msg);
	}

	public void send(TextChannel channel) {
		send(channel, page);
	}

	public void send(TextChannel channel, int page) {
		if (this.id != 0) {
			channel.editMessageById(id, pages.get(page)).queue();
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
				if (this.page > 20)
					m.addReaction("⏪").queue();
			}

			if (this.page < pages.size() - 1) {
				m.addReaction("▶").queue();
				if (this.page < pages.size() - 2) {
					m.addReaction("➡").queue();
					if (this.page < pages.size() - 20)
						m.addReaction("⏩").queue();
				}
			}

//			for (char c : "⏪⬅◀▶➡⏩".toCharArray())
//				m.addReaction(c + "").queue();
		});
	}

	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		ReactionEmote react = event.getReactionEmote();
		if (member != null && event.getMember().getIdLong() != member.getIdLong()) {
			if (react.isEmote()) {
				event.retrieveMessage().map(msg -> msg.removeReaction(react.getEmote(), event.getMember().getUser()))
						.queue();
			} else {
				event.retrieveMessage().map(msg -> msg.removeReaction(react.getEmoji(), event.getMember().getUser()))
						.queue();
			}
			return;
		}

		if (!react.isEmoji()) {
			event.retrieveMessage().map(msg -> msg.removeReaction(react.getEmote(), event.getMember().getUser()))
					.queue();
			return;
		}
//		event.retrieveMessage().map(msg -> msg.removeReaction(react.getEmoji(), event.getMember().getUser())).queue();
		event.retrieveMessage().map(msg -> msg.removeReaction(react.getAsReactionCode(), event.getUser()));
//		event.retrieveMessage().

		switch (react.getEmoji()) {
			case "▶":
				this.page = Math.min(page + 1, pages.size() - 1);
				break;
			case "➡":
				this.page += (double) pages.size() / (double) 10;
				this.page = Math.min(page, pages.size() - 1);
				break;
			case "⏩":
				this.page = pages.size() - 1;
				break;
			case "◀":
				this.page = Math.max(page - 1, 0);
				break;
			case "⬅":
				this.page -= pages.size() / 10;
				this.page = Math.max(page, 0);
				break;
			case "⏪":
				this.page = 0;
				break;
			default:
				System.out.println("Unknown emoji: " + react.getEmoji());
				return;
		}
		send(event.getChannel());
	}

}
