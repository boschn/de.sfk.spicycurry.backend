/**
 * 
 */
package de.sfk.spicycurry.data;

/**
 * @author boris.schneider
 *
 */
public class FeatureLoadedVisitor implements Visitor {

	/**
	 * visit a feature
	 */
	@Override
	public void visit(Feature f) {
		
		System.out.printf("\n Feature: '%s'- '%s' \n", f.getId(),f.getTitle());
		// visit all requirements
		for(Visited v: f.getRequirements()) 
			v.accept(this);
	}

	/**
	 * visit a requirement
	 */
	@Override
	public void visit(Requirement r) {
		
		System.out.printf("\t Requirement: '%s'- '%s'\n", r.getId(),r.getDescription());
		// visit all requirements
		for(Visited v: r.getRequirements()) 
			v.accept(this);

	}

}
