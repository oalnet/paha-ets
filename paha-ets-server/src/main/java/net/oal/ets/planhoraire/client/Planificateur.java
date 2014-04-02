package net.oal.ets.planhoraire.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Planificateur implements IsSerializable {
	private List coursChoisis = new ArrayList();
	private Collection indisponibilites = new ArrayList();
	private List coursAplanifier = new ArrayList();
	private boolean recalcul = true;
	private Collection grilles = new ArrayList();
	private Collection coursSansConflits = new ArrayList();

	public void add(Cours cours) {
		if(cours != null) {
			this.coursChoisis.add(cours);
			this.recalcul = true;
		}
	}
	public void remove(Cours cours) {
		if(cours != null) {
			this.coursChoisis.remove(cours);
			this.recalcul = true;
		}
	}

	public void add(Indisponibilite indisponibilite) {
		if(indisponibilite != null) {
			this.indisponibilites.add(indisponibilite);
			this.recalcul = true;
		}
	}
	public void remove(Indisponibilite indisponibilite) {
		Indisponibilite toRemove = indisponibilite;
		if(!this.indisponibilites.contains(toRemove)) {
			for(Iterator iter = this.indisponibilites.iterator(); iter.hasNext();) {
				EvenementIF indis = (EvenementIF) iter.next();
				if(((Indisponibilite) indis).equals(toRemove)) {
					toRemove = (Indisponibilite) indis;
				}
			}
		}
		if(toRemove != null) {
			this.indisponibilites.remove(toRemove);
			this.recalcul = true;
		}
	}

	public Collection getGrilles() {
		Collection copy = new ArrayList();
		this.calculateGrilles();
		copy.addAll(this.grilles);
		return copy;
	}
	
	public void calculateGrilles() {
		if(this.recalcul) {
			this.coursAplanifier.clear();
			this.coursAplanifier.addAll(this.coursChoisis);
			this.grilles.clear();
			GrilleHoraire grille = new GrilleHoraire();
			grille.addAll(this.indisponibilites);
			this.calculateGrilles(grille);
		}
	}
	protected void calculateGrilles(GrilleHoraire grille) {
		if(!this.coursAplanifier.isEmpty()) {
			Cours cours = (Cours) this.coursAplanifier.get(this.coursAplanifier.size()-1);
			this.coursAplanifier.remove(cours);
			for(Iterator iter = cours.getListeGroupe().iterator(); iter.hasNext();) {
				GroupeCours groupe = (GroupeCours) iter.next();
				boolean canAdd = true;
				Collection evts = new ArrayList();
				for(Iterator iter2 = groupe.getListePeriodes().iterator(); iter2.hasNext();) {
					PeriodeCours periode = (PeriodeCours) iter2.next();
					evts.add(periode);
					if(!grille.canAddWithoutConflict(periode)) {
						canAdd = false;
						break;
					}
				}
				if(canAdd) {
					grille.addAll(evts);
					this.calculateGrilles(grille);
					grille.removeAll(evts);
				}
			}
			this.coursAplanifier.add(cours);
		} else {
			this.grilles.add(grille.copy());
		}
	}
	public List getCoursChoisis() {
		return coursChoisis;
	}
	public void setCoursChoisis(List coursChoisis) {
		this.coursChoisis = coursChoisis;
	}

	public int getNbGrille() {
		return this.getGrilles().size();
	}
	public int getNbCoursChoisis() {
		return this.coursChoisis.size();
	}
	public int getNbCredit() {
		int creditTotal = 0;
		for(Iterator iter = this.coursChoisis.iterator(); iter.hasNext();) {
			Cours cours = (Cours) iter.next();
			creditTotal += cours.getNbCredit();
		}
		return creditTotal;
	}
	
	public boolean canAddWithoutConflict(EvenementIF evt) {
		for(Iterator iter = this.grilles.iterator(); iter.hasNext();) {
			GrilleHoraire grille = (GrilleHoraire) iter.next();
			if(grille.canAddWithoutConflict(evt)) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean canAddWithoutConflict(GroupeCours groupe) {
		for(Iterator iter = groupe.getListePeriodes().iterator(); iter.hasNext();) {
			PeriodeCours periode = (PeriodeCours) iter.next();
			if(!this.canAddWithoutConflict(periode)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean canAddWithoutConflict(Cours cours) {
		for(Iterator iter = cours.getListeGroupe().iterator(); iter.hasNext();) {
			GroupeCours groupe = (GroupeCours) iter.next();
			if(this.canAddWithoutConflict(groupe)) {
				return true;
			}
		}
		return false;
	}

	public void updateConflict(List listeCours) {
		this.coursSansConflits.clear();
		for(Iterator iter = listeCours.iterator(); iter.hasNext();) {
			Cours cours = (Cours) iter.next();
			if(this.getCoursChoisis().contains(cours)) {
				continue;
			} else if(!this.canAddWithoutConflict(cours)) {
				continue;
			}
			coursSansConflits.add(cours);
		}
	}

	public Collection getIndisponibilitesInside(int jour, String heureDebut, String heureFin) {
		Collection liste = new ArrayList();
		for(Iterator iter = this.indisponibilites.iterator(); iter.hasNext();) {
			EvenementIF evt = (EvenementIF) iter.next();
			if(evt.isInside(jour, heureDebut, heureFin)) {
				liste.add(evt);
			}
		}
		return liste;
	}

	public Collection getCoursSansConflits() {
		return coursSansConflits;
	}
	public void setCoursSansConflits(Collection coursSansConflits) {
		this.coursSansConflits = coursSansConflits;
	}
}
