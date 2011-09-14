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
public class Node extends Model {
	
	String baseURL;

}
