/**
 * 
 */
package controllers;

import models.Message;

import org.w3c.dom.Document;

import com.google.gson.Gson;

import play.libs.F.EventStream;
import play.libs.XML;
import play.mvc.WebSocketController;
import play.utils.HTML;

/**
 * @author chamerling
 * 
 */
public class WebSocket extends WebSocketController {

	public static EventStream<Message> liveStream = new EventStream<Message>();

	public static void monitoring() {
		while (inbound.isOpen()) {
			Message message = await(liveStream.nextEvent());
			if (message != null) {
				String json = new Gson().toJson(message);
				outbound.send(json);
			}
		}
	}
}
