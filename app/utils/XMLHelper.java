/**
 *
 */
package utils;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.hibernate.transform.Transformers;
import org.w3c.dom.Node;

/**
 * @author chamerling
 *
 */
public class XMLHelper {

	public static final String createStringFromDOMNode(Node node,
			boolean omitDeclaration) {
		assert node != null;

		StringWriter out = null;
		try {
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");

			node.normalize();

			Source source = new DOMSource(node);
			out = new StringWriter();
			Result resultStream = new StreamResult(out);

			if (omitDeclaration) {
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
						"yes");
			} else {
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
						"no");
			}

			transformer.transform(source, resultStream);
		} catch (Exception e) {
		}

		if (out != null) {
			return out.toString();
		}

		return null;
	}

}
