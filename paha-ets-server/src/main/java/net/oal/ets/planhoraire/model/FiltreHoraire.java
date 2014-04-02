package net.oal.ets.planhoraire.model;

import net.oal.ets.planhoraire.client.Cours;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class FiltreHoraire {
	private final static int TAILLE_INDEX = 3;
	private Horaire horaire = null;
	private Collection filtreActif;
	private boolean viewConflicts = true;
	
	public boolean isViewConflicts() {
		return viewConflicts;
	}

	public void setViewConflicts(boolean viewConflicts) {
		this.viewConflicts = viewConflicts;
	}

	public FiltreHoraire() {
		filtreActif = new ArrayList();
	}

	public Collection getListeFiltres() {
		Collection liste = new ArrayList();
		for(Iterator iter = horaire.getListeCours().iterator(); iter.hasNext();) {
			Cours cours = (Cours) iter.next();
			String racine = cours.getId().substring(0, TAILLE_INDEX);
			if(!liste.contains(racine)) {
				liste.add(racine);
			}
		}
		return liste;
	}

	public Collection getListeCours() {
		if(filtreActif.isEmpty()) {
			return horaire.getListeCours();
		}
		Collection liste = new ArrayList();
		for(Iterator iter = horaire.getListeCours().iterator(); iter.hasNext();) {
			Cours cours = (Cours) iter.next();
			String racine = cours.getId().substring(0, TAILLE_INDEX);
			if(filtreActif.contains(racine)) {
				liste.add(cours);
			}
		}
		return liste;
	}

	public Horaire getHoraire() {
		return this.horaire;
	}

	public void setHoraire(Horaire horaire) {
		this.horaire = horaire;
	}

	public Collection getFiltreActif() {
		return filtreActif;
	}
}
