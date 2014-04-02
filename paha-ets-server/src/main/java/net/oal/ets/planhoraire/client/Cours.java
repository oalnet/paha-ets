package net.oal.ets.planhoraire.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


public class Cours implements Serializable {
	private String id;
	private String description;
	private int nbCredit;
	private Collection<GroupeCours> listeGroupe;

	public Cours() {
		this.id = "------";
		this.description = "inconnue";
		this.nbCredit = 0;
		this.listeGroupe = new ArrayList<GroupeCours>();
	}

	/* (non-Javadoc)
	 * @see net.oal.ets.planhoraire.model.CoursIF#getDescription()
	 */
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see net.oal.ets.planhoraire.model.CoursIF#getId()
	 */
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see net.oal.ets.planhoraire.model.CoursIF#getNbCredit()
	 */
	public int getNbCredit() {
		return nbCredit;
	}
	public void setNbCredit(int nbCredit) {
		this.nbCredit = nbCredit;
	}

	public void add(GroupeCours groupe) {
		if(!this.listeGroupe.contains(groupe))
			this.listeGroupe.add(groupe);
	}
	public void remove(GroupeCours groupe) {
		this.listeGroupe.remove(groupe);
	}

	/* (non-Javadoc)
	 * @see net.oal.ets.planhoraire.model.CoursIF#toString()
	 */
	public String toString() {
		return id;
	}
	public Collection<GroupeCours> getListeGroupe() {
		return listeGroupe;
	}
	public void setListeGroupe(Collection<GroupeCours> listeGroupe) {
		this.listeGroupe = listeGroupe;
	}

	public Collection getEvenementsInside(int jour, String heureDebut, String heureFin) {
		Collection liste = new ArrayList();
		for(Iterator iter = this.listeGroupe.iterator(); iter.hasNext();) {
			GroupeCours grp = (GroupeCours) iter.next();
			liste.addAll(grp.getEvenementsInside(jour, heureDebut, heureFin));
		}
		return liste;
	}
	public Collection getEvenementsInside(EvenementIF evtTest) {
		return this.getEvenementsInside(evtTest.getJour(), evtTest.getHeureDebut(), evtTest.getHeureFin());
	}

	@Override
	public boolean equals(Object arg0) {
		if((arg0 instanceof Cours) && (arg0 != null)) {
			return this.getId().equals(((Cours)arg0).getId());
		} else
			return super.equals(arg0);
	}
}
