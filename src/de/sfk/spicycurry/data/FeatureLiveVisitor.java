/**
 * 
 */
package de.sfk.spicycurry.data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * @author boris.schneider
 *
 */
public class FeatureLiveVisitor implements Visitor {

	private Logger logger = LogManager.getLogger(FeatureLiveVisitor.class);
	
	/**
	 * visit a feature
	 */
	@Override
	public void visit(Feature f) {
		
		System.out.printf("\n Feature: '%s'- '%s' %s for %s \n \t %s", 
				f.getId(),
				f.getTitle(),
				f.getCustomerRequirementTitle(), 
				f.getCategory(), 
				f.getDescription());
		// visit all requirements
		for(Visitable v: f.loadSubRequirements()) 
			v.accept(this);
	}

	/**
	 * visit a requirement
	 */
	@Override
	public void visit(Requirement r) {
		
		System.out.printf("\t Requirement: '%s'- '%s'\n", r.getId(),r.getDescription());
		// visit all requirements
		for(Visitable v: r.loadSubRequirements()) 
			v.accept(this);
	}

}
