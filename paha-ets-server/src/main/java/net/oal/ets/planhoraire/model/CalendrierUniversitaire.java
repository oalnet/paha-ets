package net.oal.ets.planhoraire.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class CalendrierUniversitaire {
	private static CalendrierUniversitaire _instance = null;
	private Map trimestres;
	
	public static CalendrierUniversitaire getInstance() {
		if(_instance == null) {
			_instance = new CalendrierUniversitaire();
		}
		return _instance;
	}
	
	public CalendrierUniversitaire() {
		trimestres = new HashMap();
	}

	public void add(TrimestreCalendrierUniversitaire trimestre) {
		this.trimestres.put(trimestre.getId(), trimestre);
	}
	public void remove(TrimestreCalendrierUniversitaire trimestre) {
		this.trimestres.remove(trimestre);
	}
	public Collection listeTrimestres() {
		Collection copy = new LinkedList();
		copy.addAll(this.trimestres.values());
		return copy;
	}
	public Map getTrimestres() {
		return trimestres;
	}
}
