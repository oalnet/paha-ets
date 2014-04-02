package net.oal.ets.planhoraire.client;

import java.util.Comparator;

public class CoursComparator implements Comparator {
	public CoursComparator() {
		
	}

	public int compare(Object arg0, Object arg1) {
		if((arg0 instanceof Cours) && (arg0 instanceof Cours)) {
			Cours cours0 = (Cours) arg0;
			Cours cours1 = (Cours) arg1;
			return cours0.getId().compareTo(cours1.getId()); 
		} else {
			throw new ClassCastException();
		}
	}

}
