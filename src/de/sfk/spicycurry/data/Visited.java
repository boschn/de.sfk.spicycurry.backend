/**
 * 
 */
package de.sfk.spicycurry.data;

/**
 * Visited interface for elements visitable
 * 
 * @author boris.schneider
 *
 */
public interface Visited {
	public void accept( Visitor v ); // dispatch the visitor
}
