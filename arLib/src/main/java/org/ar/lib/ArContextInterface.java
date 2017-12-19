
package org.ar.lib;

/**
 * An interface for ArContext, so that it can be used in the libary / Plugin
 * side, without knowing the implementation.
 * 
 * @author A. Egal
 */
public interface ArContextInterface {

	void loadArViewWebPage(String url) throws Exception;

	public void abrirLugar(int idLugarTuristico);

}