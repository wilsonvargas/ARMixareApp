
package org.ar.lib;

/**
 * An interface for ArState, so that it can be used in the library / plugin side, without knowing
 * the implementation.
 * @author A. Egal
 */
public interface ArStateInterface {

	boolean handleEvent(ArContextInterface ctx, String onPress);
	
}