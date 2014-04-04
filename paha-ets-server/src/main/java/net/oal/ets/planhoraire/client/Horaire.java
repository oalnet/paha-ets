package net.oal.ets.planhoraire.client;

import com.google.gwt.user.client.rpc.IsSerializable;
import net.oal.ets.planhoraire.client.Cours;
import net.oal.ets.planhoraire.client.CoursComparator;
import net.oal.ets.planhoraire.client.GroupeCours;

import java.util.*;

public class Horaire implements IsSerializable {
    private String session;
    private String concentration;

	private String description;
	private String url;
	private String date;
	private Map<String, Cours> listeCours;

	public Horaire() {
		this.session = "---";
        this.concentration = "---";
		this.description = "inconnue";
		this.listeCours = new HashMap<String, Cours>();
	}

	public String getId() {
		return session.replaceAll(" ", "_")+"-"+concentration;
	}

    public String getSession() {
        return session;
    }
    public void setSession(String session) {
        this.session = session;
    }

    public String getConcentration() {
        return concentration;
    }
    public void setConcentration(String concentration) {
        this.concentration = concentration;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
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
		return getId()+" ("+this.getDate()+")";
	}

	public Collection<Cours> getListeCours() {
		List<Cours> l = new ArrayList<Cours>();
		l.addAll(listeCours.values());
		Collections.sort(l, new CoursComparator());
		return l;
	}

	public Map<String, Cours> getMapCours() { return this.listeCours; }
	public void setMapCours(Map<String, Cours> listeCours) { this.listeCours = listeCours; }
}
