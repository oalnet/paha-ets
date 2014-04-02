package net.oal.ets.planhoraire.client;

import com.google.gwt.user.client.rpc.IsSerializable;


public class Evenement implements EvenementIF, IsSerializable {
	private int jour = 1;
	private String heureDebut = null;
	private String heureFin = null;

	private static String[] jourName = {"Sam", "Dim", "Lun", "Mar", "Mer", "Jeu", "Ven"};
	public Evenement() {
		
	}

	public static String getJourName(int jour) {
		if(jour >= 0) {
			return jourName[jour % jourName.length];
		} else {
			return "inconnu ("+jour+")";
		}
	}

	public String getDescription() {
		return "desc";
	}

	public int getJour() {
		return jour;
	}

	public void setJour(int jour) {
		this.jour = jour;
	}
	
	public String getHeureDebut() {
		return heureDebut;
	}

	public void setHeureDebut(String heureDebut) {
		this.heureDebut = heureDebut;
	}

	public String getHeureFin() {
		return heureFin;
	}

	public void setHeureFin(String heureFin) {
		this.heureFin = heureFin;
	}

	public boolean isInside(int jour, String heureDebut, String heureFin) {
		if(this.jour == jour) {
			if((this.heureDebut == null) || (this.heureFin == null)) {
				System.out.println("Heure debut : "+this.heureDebut+" / "+this.heureFin+" - "+heureDebut+" / "+heureFin+".");
			}
			if((heureDebut == null) || (heureFin == null)) {
				return true;
			}
			if((this.heureDebut.compareTo(heureDebut) >= 0) && (this.heureDebut.compareTo(heureFin) < 0)) {
				return true;
			}
			if((this.heureFin.compareTo(heureDebut) > 0) && (this.heureFin.compareTo(heureFin) <= 0)) {
				return true;
			}
			if((this.heureFin != null) && (this.heureDebut.compareTo(heureDebut) <= 0) && (this.heureFin.compareTo(heureFin) >= 0)) {
				return true;
			}
		}
		return false;
	}
}
