/**
 * 
 */
package de.sfk.spicycurry;

import de.sfk.spicycurry.data.EclipseLinkPersistor;
import de.sfk.spicycurry.data.IPersistor;
import de.sfk.spicycurry.server.*;

/**
 * @author boris.schneider
 *
 */
public class Globals {

		public static final IPersistor Persistor = new EclipseLinkPersistor("H2LOCAL");
		public static IDBServer DBServer = null;
		public static CurryServer CurryServer = null;
}
