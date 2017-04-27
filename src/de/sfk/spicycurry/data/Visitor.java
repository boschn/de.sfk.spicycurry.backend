package de.sfk.spicycurry.data;
/**
 * Visitor interface 
 * 
 * @author boris.schneider
 *
 */
public interface Visitor {
	
	 /**
	  * visit a Feature
	  * @param f
	  */
	 public void visit(Feature f);    
	 
	 /**
	  * visit a requirement
	  * @param r
	  */
	 public void visit(Requirement r);

}
