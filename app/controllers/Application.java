package controllers;

import java.util.List;
import java.util.Set;

import org.ow2.petals.kernel.ws.api.EndpointService;
import org.ow2.petals.kernel.ws.api.PEtALSWebServiceException;
import org.ow2.petals.kernel.ws.api.to.Endpoint;
import org.petalslink.dsb.cxf.CXFHelper;
import org.petalslink.dsb.ws.api.DSBInformationService;
import org.petalslink.dsb.ws.api.DSBWebServiceException;
import org.petalslink.dsb.ws.api.SOAPServiceBinder;

import play.mvc.Controller;

public class Application extends Controller {

	public static final String url = "http://localhost:7600/petals/ws";

	public static void index() {
		render();
	}

	public static void services() {
		DSBInformationService s = CXFHelper.getClient(url,
				DSBInformationService.class);
		try {
			List<String> services = s.getWebServices();
			render(services);
		} catch (RuntimeException e) {
			flash.error(e.getMessage());
		} catch (DSBWebServiceException e) {
			flash.error(e.getMessage());
		}
		render();
	}

	public static void bind() {
		SOAPServiceBinder binder = CXFHelper.getClient(url,
				SOAPServiceBinder.class);
		try {
			Set<String> services = binder.getWebServices();
			render(services);
		} catch (Exception e) {
			flash.error(e.getMessage());
		}
		render();
	}

	public static void endpoints() {
		EndpointService client = CXFHelper
				.getClient(url, EndpointService.class);
		try {
			List<Endpoint> endpoints = client.getEndpoints();
			render(endpoints);
		} catch (Exception e) {
			flash.error(e.getMessage());
		}
		render();
	}

	public static void postBind() {
		bind();
	}

}