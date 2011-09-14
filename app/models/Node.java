/**
 * 
 */
package models;

import javax.persistence.Entity;

import play.db.jpa.Model;
import play.mvc.Scope.Session;

/**
 * @author chamerling
 * 
 */
@Entity
public class Node extends Model {

	public String name;

	public String baseURL;

	public boolean isSame(String id) {
		return id != null && Long.valueOf(id).equals(this.id);
	}

	public static Node getCurrentNode() {
		String nodeId = Session.current().get("node");
		if (nodeId == null) {
			return null;
		}
		Node node = Node.findById(Long.valueOf(nodeId));
		return node;
	}

	@Override
	public String toString() {
		return "Node [name=" + name + ", baseURL=" + baseURL + "]";
	}

}
