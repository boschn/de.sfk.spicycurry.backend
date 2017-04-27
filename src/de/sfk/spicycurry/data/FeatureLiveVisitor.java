/**
 * 
 */
package de.sfk.spicycurry.data;

/**
 * @author boris.schneider
 *
 */
public class FeatureLiveVisitor implements Visitor {

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
		for(Visited v: f.loadRequirements()) 
			v.accept(this);
	}

	/**
	 * visit a requirement
	 */
	@Override
	public void visit(Requirement r) {
		
		System.out.printf("\t Requirement: '%s'- '%s'\n", r.getId(),r.getDescription());
		// visit all requirements
		for(Visited v: r.loadRequirements()) 
			v.accept(this);
	}

}
