package net.oal.ets.planhoraire.client;

import com.google.gwt.user.client.rpc.RemoteService;

import java.util.List;
import java.util.Map;

public interface CoursService extends RemoteService {
    public Map getListeHoraireId();

    public Horaire getHoraire(String horaireId);

    public Planificateur calculateGrilles(Planificateur planif);

    public Planificateur updateConflict(Planificateur planif, List listeCours);
}
