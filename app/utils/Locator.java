/**
 *
 */
package utils;

import java.util.Iterator;
import java.util.Set;

import models.Node;

import org.petalslink.dsb.cxf.CXFHelper;
import org.petalslink.dsb.ws.api.ServiceInformation;

/**
 * @author chamerling
 *
 */
public class Locator {

	/**
	 * Try to find a business service which endpoint contains the given input
	 * string. Dummy way to lookup service until we have a reall governance
	 * tool...
	 *
	 * @param pattern
	 * @param node
	 * @return
	 */
	public static String getBusinessService(String pattern, Node node) {
		String result = null;
		if (node == null || pattern == null) {
			return null;
		}

		ServiceInformation information = CXFHelper.getClient(node.baseURL,
				ServiceInformation.class);

		try {
			Set<String> businessServices = information.getExposedWebServices();
			Iterator<String> iter = businessServices.iterator();
			boolean found = false;
			while (iter.hasNext() && !found) {
				String url = iter.next();
				if (url != null && url.contains(pattern)) {
					found = true;
					result = url;
				}
			}
		} catch (Exception e) {
		}

		return result;

	}

}
