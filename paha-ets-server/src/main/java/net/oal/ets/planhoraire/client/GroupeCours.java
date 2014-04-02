package net.oal.ets.planhoraire.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class GroupeCours implements IsSerializable {
	private Cours cours;
	private String id;
	private Collection<PeriodeCours> listePeriodes = new ArrayList<PeriodeCours>();
	
	public String toString() {
		return cours.toString()+"-"+id;
	}

	public Cours getCours() {
		return cours;
	}

	public void setCours(Cours cours) {
//		if(this.cours != null) {
//			this.cours.remove(this);
//		}
		this.cours = cours;
//		if((this.cours != null) && !this.cours.getListeGroupe().contains(this)) {
//			this.cours.add(this);
//		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Collection<PeriodeCours> getListePeriodes() {
		return listePeriodes;
	}

	public void setListePeriodes(Collection<PeriodeCours> listePeriodes) {
		this.listePeriodes = listePeriodes;
	}
	
	public void add(PeriodeCours periode) {
		if(!this.listePeriodes.contains(periode))
			this.listePeriodes.add(periode);
	}
	public void remove(PeriodeCours periode) {
		this.listePeriodes.remove(periode);
	}

	public Collection getEvenementsInside(int jour, String heureDebut, String heureFin) {
		Collection liste = new ArrayList();
		for(Iterator iter = this.listePeriodes.iterator(); iter.hasNext();) {
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

	public boolean equals(Object obj) {
		if(obj instanceof GroupeCours) {
			GroupeCours groupe = (GroupeCours) obj;
			if(groupe.toString().equals(this.toString())) {
				return true;
			}
		}
		return super.equals(obj);
	}
}
