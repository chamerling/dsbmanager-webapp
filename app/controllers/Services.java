/**
 *
 */
package controllers;

import java.io.ByteArrayInputStream;
import java.util.Random;

import models.Message;

import org.petalslink.dsb.api.MessageExchangeException;
import org.petalslink.dsb.monitoring.api.JAXBHelper;
import org.petalslink.dsb.monitoring.api.ReportBean;
import org.petalslink.dsb.monitoring.api.ReportListBean;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import play.libs.IO;
import play.libs.XML;
import play.libs.XPath;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http.StatusCode;
import utils.XMLHelper;

/**
 * Accept anything POX/SOAP/ store and publish messages...
 *
 * @author chamerling
 *
 */
public class Services extends Controller {

	public static Random r = new Random();

	@Before
	public static void setup() {
		// to be injected in ressources
		renderArgs.put("base", request.getBase());
		String location = request.getBase() + "/services/MonitoringService";
		renderArgs.put("location", location);
	}

	/**
	 * Accept anything, do not care about the message format...
	 *
	 */
	public static void any() {
		long date = System.currentTimeMillis();
		String body = IO.readContentAsString(request.body);
		if (body == null) {
			error(StatusCode.INTERNAL_ERROR,
					"Message is malformed (null body)...");
		}

		Document soapMessage = XML.getDocument(body);
		if (soapMessage == null) {
			error(StatusCode.INTERNAL_ERROR,
					"Message is malformed (not XML)...");
		}

		// parse the body and calculate required data...
		Node n = XPath.selectNode("//ReportListBean", soapMessage);
		if (n == null) {
			error(StatusCode.INTERNAL_ERROR,
					"Message is malformed (not a valid report)...");
		}

		// can be optimized...
		String reportList = XMLHelper.createStringFromDOMNode(n, true);
		if (reportList == null) {
			error(StatusCode.INTERNAL_ERROR,
					"Message is malformed (can not generate reportList)...");
		}

		ReportListBean reportListBean = null;
		try {
			reportListBean = JAXBHelper.unmarshall(new ByteArrayInputStream(
					reportList.getBytes()));
		} catch (MessageExchangeException e) {
			error(StatusCode.INTERNAL_ERROR, e.getMessage());
		}

		Message message = new Message();
		for (ReportBean report : reportListBean.getReports()) {
			if ("t1".equalsIgnoreCase(report.getType())) {
				message.t1 = report.getDate();
			} else if ("t2".equalsIgnoreCase(report.getType())) {
				message.t2 = report.getDate();
			} else if ("t3".equalsIgnoreCase(report.getType())) {
				message.t3 = report.getDate();
			} else if ("t4".equalsIgnoreCase(report.getType())) {
				message.t4 = report.getDate();
			}

			// monitoring api needs to be fixed since all informations are the
			// same for the target
			message.endpoint = report.getEndpoint();
			message.itf = report.getItf();
			message.service = report.getServiceName();
			message.operation = report.getOperation();
		}
		message.date = date;
		message.exception = false;
		WebSocket.liveStream.publish(message);

		render("Services/MonitoringService_Response.xml");
	}

	/**
	 * Just to give a WSDL which is completely false but which can be required
	 * by some clients.
	 */
	public static void wsdl() {
		render("Services/MonitoringService.wsdl");
	}

}
