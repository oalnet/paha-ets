/**
 * 
 */
package net.oal.ets.planhoraire.client;

import com.google.gwt.user.client.rpc.IsSerializable;


public interface EvenementIF extends IsSerializable {
	public String getDescription();
	public int getJour();
	public String getHeureDebut();
	public String getHeureFin();
	public boolean isInside(int jour, String heureDeubt, String heureFin);
}