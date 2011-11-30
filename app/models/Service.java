/**
 * 
 */
package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * @author chamerling
 *
 */
@Entity
public class Service extends Model {
	
	public String wsdlURL;
}
