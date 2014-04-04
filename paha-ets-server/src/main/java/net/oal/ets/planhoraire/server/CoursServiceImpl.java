package net.oal.ets.planhoraire.server;

import com.google.appengine.api.datastore.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import net.oal.ets.planhoraire.client.*;
import net.oal.ets.planhoraire.client.Horaire;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Logger;

public class CoursServiceImpl extends RemoteServiceServlet implements CoursService {
    Gson gson = null;
    DateFormat dFormat_ = null;
    private Map concentrationMap = new HashMap();


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        gson = (new GsonBuilder()).create();
        dFormat_ = DateFormat.getDateTimeInstance();
        dFormat_.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("America/Montreal"), Locale.CANADA_FRENCH));

        concentrationMap.put("SEG", "Enseignements généraux");
        concentrationMap.put("CTN", "Génie de la construction");
        concentrationMap.put("ELE", "Génie électrique");
        concentrationMap.put("LOG", "Génie logiciel");
        concentrationMap.put("MEC", "Génie mécanique");
        concentrationMap.put("GOL", "Génie des opérations et de la logistique");
        concentrationMap.put("GPA", "Génie de la production automatisée");
        concentrationMap.put("GTI", "Génie des technologies de l'information");
        concentrationMap.put("SUP", "Cycles supérieurs");
        concentrationMap.put("CUR", "Cursus");
    }

    public CoursServiceImpl() {
        super();
    }

    private void fixBiDirectionalAndMapImpl(Horaire horaire) {
        for (Cours cours : horaire.getListeCours()) {
            for (GroupeCours groupe : cours.getListeGroupe()) {
                groupe.setCours(cours);
                for (PeriodeCours periode : groupe.getListePeriodes()) {
                    periode.setGroupe(groupe);
                }
                groupe.setListePeriodes(new LinkedList<PeriodeCours>(groupe.getListePeriodes()));
            }
            cours.setListeGroupe(new LinkedList<GroupeCours>(cours.getListeGroupe()));
        }
        Map coursMap = new HashMap<String, Cours>();
        coursMap.putAll(horaire.getMapCours());
        horaire.setMapCours(coursMap);
    }

    public Map getListeHoraireId() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query q = new Query("Horaire");
        PreparedQuery pq = datastore.prepare(q);

        Map liste = new HashMap();
        for (Entity result : pq.asIterable()) {
            String session = (String) result.getProperty("session");
            String concentration = (String) result.getProperty("concentration");
            Date date = (Date) result.getProperty("date");

            Map map2 = (Map) liste.get(session);
            if (map2 == null) {
                map2 = new HashMap();
                liste.put(session, map2);
            }

            map2.put(concentration + " - "+ concentrationMap.get(concentration) + " (" + dFormat_.format(date) + ")", result.getKey().getName());
        }

        return liste;
    }

    public Horaire getHoraire(String horaireId) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Horaire horaire;
        try {
            Key key = KeyFactory.createKey("Horaire", horaireId);

            Entity entity = datastore.get(key);
            Text horaireJSON = (Text) entity.getProperty("horaire");
            horaire = gson.fromJson(horaireJSON.getValue(), Horaire.class);
            fixBiDirectionalAndMapImpl(horaire);

        } catch (EntityNotFoundException ex) {
            horaire = new Horaire();

        }

        return horaire;
    }

    public Planificateur calculateGrilles(Planificateur planif) {
        planif.calculateGrilles();
        return planif;
    }

    public Planificateur updateConflict(Planificateur planif, List listeCours) {
        planif.updateConflict(listeCours);
        return planif;
    }
}
