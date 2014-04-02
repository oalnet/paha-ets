package net.oal.ets.planhoraire.model;

import com.google.gwt.user.client.rpc.IsSerializable;
import net.oal.ets.planhoraire.client.Cours;
import net.oal.ets.planhoraire.client.CoursComparator;
import net.oal.ets.planhoraire.client.GroupeCours;

import java.util.*;

public class Horaire implements IsSerializable {
	private String id;
	private String description;
	private String url;
	private String date;
	private Map<String, Cours> listeCours;

	public Horaire() {
		this.id = "---";
		this.description = "inconnue";
		this.listeCours = new HashMap<String, Cours>();
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
//		GestionnaireHoraire.getInstance().remove(this);
		this.id = id;
//		GestionnaireHoraire.getInstance().add(this);
	}

	public void addAll(Collection<Cours> listeCours) {
		for(Iterator iter = listeCours.iterator(); iter.hasNext();) {
			Cours cours = (Cours) iter.next();
			this.add(cours);
		}
	}
	public void add(Cours cours) {
		Cours c = this.getCours(cours.getId());
		if(c != null) {
			for(Iterator iter = cours.getListeGroupe().iterator(); iter.hasNext();) {
				GroupeCours groupe = (GroupeCours) iter.next();
				if(!c.getListeGroupe().contains(groupe)) {
					c.add(groupe);
				}
			}
		}
		this.listeCours.put(cours.getId(), cours);
	}
	public Cours getCours(String id) {
		return (Cours) this.listeCours.get(id);
	}
	public void remove(Cours cours) {
		this.listeCours.remove(cours);
	}

	public boolean contains(String coursId) {
		return this.listeCours.containsKey(coursId);
	}

	public String toString() {
		return id+" ("+this.getDate()+")";
	}

	public Collection<Cours> getListeCours() {
		List<Cours> l = new ArrayList<Cours>();
		l.addAll(listeCours.values());
		Collections.sort(l, new CoursComparator());
		return l;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public Map<String, Cours> getMapCours() { return this.listeCours; }
	public void setMapCours(Map<String, Cours> listeCours) { this.listeCours = listeCours; }
}
