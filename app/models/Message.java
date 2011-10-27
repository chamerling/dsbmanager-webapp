/**
 *
 */
package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Lob;

import play.Play;
import play.db.jpa.Model;

/**
 * @author chamerling
 *
 */
@Entity
public class Message extends Model {

	public static long MAX = 10;

	public long date;

	public String service;

	public String operation;

	public String endpoint;

	public String itf;

	//public long responseTime;

	public boolean exception;

	public long t1, t2, t3, t4;

	public synchronized static void push(Message message) {
		if (message == null) {
			return;
		}
		long count = count();
		if (count >= getMaxSize()) {
			// remove oldest message
			// FIXME : to be optimized
			List<Message> list = Message.find("order by date asc").fetch(1);
			for (Message message2 : list) {
				message2.delete();
			}
		}
		message.save();
	}

	public static long getMaxSize() {
		long result = MAX;
		Object o = Play.configuration.get("app.message.size");
		if (o != null) {
			try {
				result = Long.parseLong(o.toString());
			} catch (NumberFormatException e) {
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Message [date=");
		builder.append(date);
		builder.append(", service=");
		builder.append(service);
		builder.append(", exception=");
		builder.append(exception);
		builder.append("]");
		return builder.toString();
	}

}
