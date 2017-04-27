/**
 * 
 */
package de.sfk.spicycurry.data;

import java.util.*;
import java.io.Serializable;
import javax.persistence.*;
/**
 * @author boris.schneider
 *
 */

@Entity(name="Feature")
@Inheritance(strategy=InheritanceType.JOINED)
public class Feature extends Requirement implements Serializable{

	@Transient
	private static final long serialVersionUID = 1L;
	
	/**
	 * constructor
	 */
	public Feature() {
		super();
	}
	public Feature(String id) {
		super(id);
		
	}

	/**
	 * add sub requirement and add the feature to it
	 * @param req
	 */
	public void addRequirement(Requirement req) {
		super.addRequirement(req);
		req.addFeature(this);
	}
	/**
	 * visitor-pattern visit by accept 
	 */
	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
	
	/**
	 * return true if feature
	 * @return
	 */
	@Basic
	public boolean isFeature() {
		return true;
	}
}
