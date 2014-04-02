package net.oal.ets.planhoraire.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TrimestreCalendrierUniversitaire {
	static private DateFormat fmt = new SimpleDateFormat("dd-MM-yyyy");
	private String id;
	private Date dateDebutCours;
	private Date dateFinCours;
	private Date dateDebutExamen;
	private Date dateFinExamen;
	
	public TrimestreCalendrierUniversitaire() {
	}

	public Date getDateDebutCours() {
		return dateDebutCours;
	}

	public void setDateDebutCoursStr(String dateDebutCours) {
		try {
			this.dateDebutCours = fmt.parse(dateDebutCours);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setDateFinCoursStr(String dateFinCours) {
		try {
			this.dateFinCours = fmt.parse(dateFinCours);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setDateDebutCours(Date dateDebutCours) {
		this.dateDebutCours = dateDebutCours;
	}

	public Date getDateDebutExamen() {
		return dateDebutExamen;
	}

	public void setDateDebutExamen(Date dateDebutExamen) {
		this.dateDebutExamen = dateDebutExamen;
	}

	public Date getDateFinExamen() {
		return dateFinExamen;
	}

	public void setDateFinExamen(Date dateFin) {
		this.dateFinExamen = dateFin;
	}

	public Date getDateFinCours() {
		return dateFinCours;
	}

	public void setDateFinCours(Date dateFinCours) {
		this.dateFinCours = dateFinCours;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		CalendrierUniversitaire.getInstance().remove(this);
		this.id = id;
		CalendrierUniversitaire.getInstance().add(this);
	}
}
