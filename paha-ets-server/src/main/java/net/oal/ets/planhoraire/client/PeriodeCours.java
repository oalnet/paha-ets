package net.oal.ets.planhoraire.client;

import com.google.gwt.user.client.rpc.IsSerializable;




public class PeriodeCours extends Evenement implements IsSerializable {
	public transient static final int COURS = 0;
	public transient static final int LABORATOIRE = 1;
	public transient static final int TRAVAUX_PRATIQUES = 2;
	public transient static final int PROJET = 3;

	private int type = COURS;
	private GroupeCours groupe;
	private String local = "inconnu";

	public String getTypeName() {
		switch(type) {
			case COURS : return "Cours";
			case LABORATOIRE : return "Lab";
			case TRAVAUX_PRATIQUES : return "TP";
			case PROJET : return "Projet";
			default: return "inconnu";
		}
	}

	public String getDescription() {
		return groupe.toString()+" "+this.getTypeName();
	}

	public GroupeCours getGroupe() {
		return groupe;
	}

	public void setGroupe(GroupeCours groupe) {
//		if(this.groupe != null) {
//			this.groupe.remove(this);
//		}
		this.groupe = groupe;
//		if((this.groupe != null) && (!this.groupe.getListePeriodes().contains(this))) {
//			this.groupe.add(this);
//		}
	}

	public String getLocal() {
		return local;
	}

	public void setLocal(String local) {
		this.local = local;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public String toString() {
		return getJourName(this.getJour())+"\t"+this.getHeureDebut()+" - "+this.getHeureFin()+"\t"+this.getTypeName();
	}

	@Override
	public boolean equals(Object arg0) {
		if((arg0 instanceof PeriodeCours) && (arg0 != null)) {
			return this.getDescription().equals(((PeriodeCours)arg0).getDescription());
		} else
			return super.equals(arg0);
	}
}
