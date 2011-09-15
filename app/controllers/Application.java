package controllers;

import java.util.List;
import java.util.Set;

import models.Node;

import org.ow2.petals.kernel.ws.api.EndpointService;
import org.ow2.petals.kernel.ws.api.JBIArtefactsService;
import org.ow2.petals.kernel.ws.api.TopologyService;
import org.ow2.petals.kernel.ws.api.to.Component;
import org.ow2.petals.kernel.ws.api.to.ContainerInformation;
import org.ow2.petals.kernel.ws.api.to.Endpoint;
import org.ow2.petals.kernel.ws.api.to.ServiceAssembly;
import org.ow2.petals.kernel.ws.api.to.SharedLib;
import org.petalslink.dsb.cxf.CXFHelper;
import org.petalslink.dsb.ws.api.DSBInformationService;
import org.petalslink.dsb.ws.api.DSBWebServiceException;
import org.petalslink.dsb.ws.api.SOAPServiceBinder;
import org.petalslink.dsb.ws.api.ServiceEndpoint;
import org.petalslink.dsb.ws.api.ServiceInformation;
import org.petalslink.dsb.ws.api.jbi.ServiceArtefactsInformationService;

import play.data.validation.URL;
import play.mvc.Before;
import play.mvc.Controller;

public class Application extends Controller {

	@Before
	/**
	 * Put some data on all requests...
	 */
	public static void init() {
		Node n = Node.getCurrentNode();
		if (n != null) {
			renderArgs.put("currentNode", n);
		}
	}

	public static void index() {
		render();
	}

	public static void connect() {
		List<Node> nodes = Node.all().fetch();
		render(nodes);
	}

	public static void nodeConnect(Long id) {
		Node n = Node.findById(id);
		if (n != null) {
			session.put("node", n.id);
			flash.success("Connected to node %s", n.name);
		} else {
			flash.error("No such node");
		}
		connect();
	}

	public static void nodeDisconnect(Long id) {
		session.remove("node");
		flash.success("Disconnected...");
		connect();
	}

	public static void coreservices() {
		DSBInformationService s = CXFHelper.getClient(getURL(),
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

	public static void services() {
		SOAPServiceBinder binder = CXFHelper.getClient(getURL(),
				SOAPServiceBinder.class);

		ServiceInformation information = CXFHelper.getClient(getURL(),
				ServiceInformation.class);

		try {
			Set<String> services = binder.getWebServices();
			Set<String> exposed = information.getExposedWebServices();
			render(services, exposed);
		} catch (Exception e) {
			flash.error(e.getMessage());
		}
		render();
	}

	public static void endpoints() {
		EndpointService client = CXFHelper.getClient(getURL(),
				EndpointService.class);
		try {
			List<Endpoint> endpoints = client.getEndpoints();
			render(endpoints);
		} catch (Exception e) {
			flash.error(e.getMessage());
		}
		render();
	}

	public static void postBind(@URL String wsdlURL) {
		SOAPServiceBinder binder = CXFHelper.getClient(getURL(),
				SOAPServiceBinder.class);
		try {
			List<ServiceEndpoint> bound = binder.bindWebService(wsdlURL);
			StringBuffer endpoints = new StringBuffer();
			int i = 0;
			for (ServiceEndpoint serviceEndpoint : bound) {
				i++;
				endpoints.append("[Name : ");
				endpoints.append(serviceEndpoint.getEndpoint());
				endpoints.append(", Service : " + serviceEndpoint.getService());
				endpoints.append(", Interface : " + serviceEndpoint.getItf()
						+ "]");
				if (i < bound.size()) {
					endpoints.append(", ");
				}
			}
			flash.success("Service %s bound as endpoint(s) %s", wsdlURL,
					endpoints);
		} catch (Exception e) {
			flash.error(e.getMessage());
		}
		services();
	}

	public static void jbi() {
		JBIArtefactsService service = CXFHelper.getClient(getURL(),
				JBIArtefactsService.class);
		try {
			List<Component> components = service.getComponents();
			List<ServiceAssembly> sas = service.getServiceAssemblies();
			List<SharedLib> sls = service.getSharedLibraries();
			render(components, sas, sls);
		} catch (Exception e) {
			flash.error(e.getMessage());
		}
		render();
	}

	public static void jbisa(String saName) {
		ServiceArtefactsInformationService service = CXFHelper.getClient(
				getURL(), ServiceArtefactsInformationService.class);
		try {
			String description = service.getSADescription(saName);
			render(saName, description);
		} catch (Exception e) {
			flash.error(e.getMessage());
		}
		render();
	}

	public static void topology() {
		TopologyService topologyService = CXFHelper.getClient(getURL(),
				TopologyService.class);
		try {
			List<ContainerInformation> containers = topologyService
					.getAllContainerInformation();
			render(containers);
		} catch (Exception e) {
			flash.error(e.getMessage());
		}
	}
	
	private static String getURL() {
		if (session.get("node") == null) {
			flash.error("Not connected, please select a node");
			connect();
		}
		Node n = Node.findById(Long.parseLong(session.get("node")));
		if (n == null) {
			session.remove("node");
			flash.error("Node not found, please select a node");
			connect();
		}
		return n.baseURL;
	}

}