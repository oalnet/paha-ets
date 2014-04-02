package net.oal.ets.planhoraire.client;

public class Indisponibilite extends Evenement {

	public boolean equals(Object arg0) {
		if(arg0 instanceof Indisponibilite) {
			Indisponibilite indis = (Indisponibilite) arg0;
			boolean flag = (this.getJour() == indis.getJour());
			flag &= (this.getHeureDebut().equals(indis.getHeureDebut()));
			flag &= (this.getHeureFin().equals(indis.getHeureFin()));
			return flag;
		}
		return super.equals(arg0);
	}

	public String getDescription() {
		return "Indisponible";
	}
}
