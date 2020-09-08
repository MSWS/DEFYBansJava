package xyz.msws.defybans.data.pageable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import xyz.msws.defybans.Client;

public abstract class Pageable<T> extends ListenerAdapter implements List<T> {
	protected List<T> pages;
	protected int page;
	protected Client client;
	protected User member;
	protected long id;

	public Pageable(Client client) {
		this.client = client;
		client.getJDA().addEventListener(this);
	}

	public void bindTo(User member) {
		this.member = member;
	}

	public User getBoundTo() {
		return member;
	}

	public abstract void send(TextChannel channel, int page);

	public void send(TextChannel channel) {
		send(channel, this.page);
	}

	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		ReactionEmote react = event.getReactionEmote();
		if (event.getUserIdLong() == client.getJDA().getSelfUser().getIdLong())
			return;
		if (event.getMessageIdLong() != this.id)
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

	public int getPage() {
		return page;
	}

	public boolean setPage(int page) {
		if (page < 0 || page >= pages.size())
			return false;
		this.page = page;
		return true;
	}

	@Override
	public int size() {
		return pages.size();
	}

	@Override
	public boolean isEmpty() {
		return pages.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return pages.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return pages.iterator();
	}

	@Override
	public Object[] toArray() {
		return pages.toArray();
	}

	@Override
	public <Ot> Ot[] toArray(Ot[] a) {
		return pages.toArray(a);
	}

	@Override
	public boolean add(T e) {
		return pages.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return pages.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return pages.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return pages.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		return pages.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return pages.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return pages.retainAll(c);
	}

	@Override
	public void clear() {
		pages.clear();
	}

	@Override
	public T get(int index) {
		return pages.get(index);
	}

	@Override
	public T set(int index, T element) {
		return pages.set(index, element);
	}

	@Override
	public void add(int index, T element) {
		pages.add(index, element);
	}

	@Override
	public T remove(int index) {
		return pages.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return pages.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return pages.lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		return pages.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return pages.listIterator(index);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return pages.subList(fromIndex, toIndex);
	}

}
