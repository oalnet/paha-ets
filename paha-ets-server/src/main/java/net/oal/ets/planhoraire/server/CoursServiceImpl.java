package net.oal.ets.planhoraire.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import net.oal.ets.planhoraire.client.*;
import net.oal.ets.planhoraire.model.Horaire;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.*;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Logger;

public class CoursServiceImpl extends RemoteServiceServlet
        implements CoursService {
    private static final Logger logger = Logger.getLogger(CoursServiceImpl.class.getName());

    Gson gson = null;
    Map horaires = new HashMap();
    Map coursHoraires = new HashMap();
    DateFormat dFormat_ = null;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        gson = (new GsonBuilder()).create();
        dFormat_ = DateFormat.getDateTimeInstance();
        dFormat_.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("GMT-5:00"), Locale.CANADA_FRENCH));

        ServletContext servletContext = config.getServletContext();
        String pathContext = servletContext.getRealPath("/WEB-INF/");
        loadHoraire(pathContext);
    }

    public CoursServiceImpl() {
        super();
    }

    private void loadHoraire(String basedir) {
        try {
            File dir = new File(basedir, "horaires");
            if ((dir != null) && dir.exists() && dir.isDirectory()) {
                this.horaires.clear();
                File[] files = dir.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].getName().endsWith(".json")) {
                        Reader reader = new InputStreamReader(new FileInputStream(files[i]), "UTF-8");
                        Horaire horaire = gson.fromJson(reader, Horaire.class);
                        fixBiDirectional(horaire);
                        horaires.put(horaire.getId(), files[i]);
                        reader.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fixBiDirectional(Horaire horaire) {
        for(Cours cours : horaire.getListeCours()) {
            for(GroupeCours groupe : cours.getListeGroupe()) {
                groupe.setCours(cours);
                for(PeriodeCours periode : groupe.getListePeriodes()) {
                    periode.setGroupe(groupe);
                }
            }
        }
    }

    public Map getListeHoraireId() {
        Map liste = new HashMap();
        for (Iterator iter = horaires.keySet().iterator(); iter.hasNext(); ) {
            String id = (String) iter.next();
            File file = (File) horaires.get(id);
            liste.put(id, id + " (" + dFormat_.format(new Date(file.lastModified())) + ")");
        }
        return liste;
    }

    public Collection getListeCours(String horaireId) {
        Horaire horaire = new Horaire();
        if (!coursHoraires.containsKey(horaireId)) {
            File file = (File) horaires.get(horaireId);
            try {
                Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
                horaire = gson.fromJson(reader, Horaire.class);
                fixBiDirectional(horaire);
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return horaire.getListeCours();
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
