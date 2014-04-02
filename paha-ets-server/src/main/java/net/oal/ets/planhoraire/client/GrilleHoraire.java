package net.oal.ets.planhoraire.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;



public class GrilleHoraire implements IsSerializable {
	private Collection evenements = new ArrayList();

	public void addAll(Collection evts) {
		for(Iterator iter = evts.iterator(); iter.hasNext();) {
			EvenementIF evt = (EvenementIF) iter.next();
			this.add(evt);
		}
	}
	public void removeAll(Collection evts) {
		for(Iterator iter = evts.iterator(); iter.hasNext();) {
			EvenementIF evt = (EvenementIF) iter.next();
			this.remove(evt);
		}
	}

	public void add(EvenementIF evt) {
		this.evenements.add(evt);
	}
	public void remove(EvenementIF evt) {
		this.evenements.remove(evt);
	}

	public Collection getEvenementsInside(int jour, String heureDebut, String heureFin) {
		Collection liste = new ArrayList();
		for(Iterator iter = this.evenements.iterator(); iter.hasNext();) {
			EvenementIF evt = (EvenementIF) iter.next();
			if(evt.isInside(jour, heureDebut, heureFin)) {
				liste.add(evt);
			}
		}
		return liste;
	}
	public Collection getEvenementsInside(EvenementIF evtTest) {
		return this.getEvenementsInside(evtTest.getJour(), evtTest.getHeureDebut(), evtTest.getHeureFin());
	}
	public boolean canAddWithoutConflict(EvenementIF evt) {
		return this.getEvenementsInside(evt).isEmpty();
	}

	public GrilleHoraire copy() {
		GrilleHoraire copy = new GrilleHoraire();
		copy.addAll(this.evenements);
		return copy;
	}
	public Collection getEvenements() {
		return evenements;
	}
	public void setEvenements(Collection evenements) {
		this.evenements = evenements;
	}

	public Collection getGroupeCours() {
		Collection liste = new ArrayList();
		for(Iterator iter = this.evenements.iterator(); iter.hasNext();) {
			EvenementIF evt = (EvenementIF) iter.next();
			if(evt instanceof PeriodeCours) {
				GroupeCours groupe = ((PeriodeCours) evt).getGroupe();
				if(!liste.contains(groupe)) {
					liste.add(groupe);
				}
			}
		}
		return liste;
	}
}
