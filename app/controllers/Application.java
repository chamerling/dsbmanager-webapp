package controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

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
import org.petalslink.dsb.ws.api.ExposerService;
import org.petalslink.dsb.ws.api.PubSubMonitoringManager;
import org.petalslink.dsb.ws.api.PubSubMonitoringService;
import org.petalslink.dsb.ws.api.RegistryListenerService;
import org.petalslink.dsb.ws.api.RouterModule;
import org.petalslink.dsb.ws.api.RouterModuleService;
import org.petalslink.dsb.ws.api.SOAPServiceBinder;
import org.petalslink.dsb.ws.api.ServiceEndpoint;
import org.petalslink.dsb.ws.api.ServiceInformation;
import org.petalslink.dsb.ws.api.cron.CronJobBean;
import org.petalslink.dsb.ws.api.cron.CronJobService;
import org.petalslink.dsb.ws.api.jbi.ComponentInformationService;
import org.petalslink.dsb.ws.api.jbi.ServiceArtefactsInformationService;

import play.data.validation.URL;
import play.mvc.Before;
import play.mvc.Controller;
import beans.RegistryListener;

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

		renderArgs.put("base", request.getBase());
		String location = request.getBase() + "/services/MonitoringService";
		renderArgs.put("location", location);

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

	public static void registry() {
		RegistryListenerService s = CXFHelper.getClient(getURL(),
				RegistryListenerService.class);

		try {
			Set<RegistryListener> listeners = new HashSet<RegistryListener>();
			Set<String> tmp = s.getListeners();
			for (String name : tmp) {
				RegistryListener l = new RegistryListener();
				l.name = name;
				l.state = s.getState(name);
				listeners.add(l);
			}
			render(listeners);
		} catch (RuntimeException e) {
			flash.error(e.getMessage());
		}
		render();
	}

	public static void updateListener(String name, boolean state) {
		RegistryListenerService s = CXFHelper.getClient(getURL(),
				RegistryListenerService.class);

		try {
			s.setState(name, state);
		} catch (RuntimeException e) {
			flash.error(e.getMessage());
		}
		registry();
	}

	public static void router() {
		RouterModuleService routerModuleService = CXFHelper.getClient(getURL(),
				RouterModuleService.class);
		List<RouterModule> receivers = routerModuleService.getReceivers();
		List<RouterModule> senders = routerModuleService.getSenders();
		render(senders, receivers);
	}

	public static void updateSenderModule(String name, boolean state) {
		RouterModuleService s = CXFHelper.getClient(getURL(),
				RouterModuleService.class);
		try {
			s.setSenderState(name, state);
		} catch (RuntimeException e) {
			flash.error(e.getMessage());
		}
		router();
	}

	public static void updateReceiverModule(String name, boolean state) {
		RouterModuleService s = CXFHelper.getClient(getURL(),
				RouterModuleService.class);
		try {
			s.setReceiverState(name, state);
		} catch (RuntimeException e) {
			flash.error(e.getMessage());
		}
		router();
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

	public static void exposeNow() {
		ExposerService service = CXFHelper.getClient(getURL(),
				ExposerService.class);
		try {
			service.expose();
		} catch (Exception e) {
			flash.error(e.getMessage());
		}
		services();
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
			Set<String> sus = service.getSUForSA(saName);
			render(saName, sus, description);
		} catch (Exception e) {
			flash.error(e.getMessage());
		}
		render();
	}

	public static void jbisu(String saName, String su) {
		ServiceArtefactsInformationService service = CXFHelper.getClient(
				getURL(), ServiceArtefactsInformationService.class);
		try {
			String description = service.getSUDescription(saName, su);
			render(su, description);
		} catch (Exception e) {
			flash.error(e.getMessage());
		}
		render();
	}

	public static void jbicomponent(String name) {
		ServiceArtefactsInformationService service = CXFHelper.getClient(
				getURL(), ServiceArtefactsInformationService.class);

		ComponentInformationService componentService = CXFHelper.getClient(
				getURL(), ComponentInformationService.class);
		try {
			Set<String> sus = service.getSUForComponent(name);
			String description = "";// componentService.getComponentDescription(name);
			render(name, sus, description);
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

	public static void monitoring() {
		PubSubMonitoringManager manager = CXFHelper.getClient(getURL(),
				PubSubMonitoringManager.class);

		try {
			boolean state = manager.getState();
			QName topic = manager.getTopic();
			render(state, topic);
		} catch (Exception e) {
			flash.error(e.getMessage());
		}
		render();
	}

	public static void monitor(boolean state) {
		PubSubMonitoringManager manager = CXFHelper.getClient(getURL(),
				PubSubMonitoringManager.class);
		try {
			manager.setState(state);
		} catch (Exception e) {
			flash.error(e.getMessage());
		}
		monitoring();
	}

	public static void unsubcribeFromMonitoring() {
		flash.error("Not implemented");
		monitoring();
	}

	public static void subscribeToMonitoring() {

		PubSubMonitoringService pubSubMonitoringService = CXFHelper.getClient(
				getURL(), PubSubMonitoringService.class);

		try {
			String id = pubSubMonitoringService.subscribe(request.getBase()
					+ "/services/MonitoringService");
			flash.success("Subscription done, subscription ID is %s", id);
		} catch (Exception e) {
			flash.error("Can not send subscribe to DSB, cause '%s'!",
					e.getMessage());
		}
		monitoring();
	}

	/**
	 * Get the kernel jobs
	 */
	public static void jobs() {
		CronJobService jobsService = CXFHelper.getClient(getURL(), CronJobService.class);
		try {
			List<CronJobBean> jobs = jobsService.get();
			render(jobs);
		} catch (Exception e) {
			flash.error("Can not get jobs from the DSB, cause '%s'!",
					e.getMessage());
		}
		index();
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