package net.oal.ets.planhoraire.server;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.oal.ets.planhoraire.client.Horaire;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class ImportServlet extends HttpServlet {
    private Gson gson = null;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        gson = (new GsonBuilder()).create();
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();

//        if(user != null) {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

            String horaireJson = req.getParameter("horaire");
            Horaire horaire = gson.fromJson(horaireJson, Horaire.class);
            Date date = new Date();

            Entity entity = new Entity("Horaire", horaire.getId());
            entity.setProperty("user", user);
            entity.setProperty("date", date);
            entity.setProperty("session", horaire.getSession());
            entity.setProperty("concentration", horaire.getConcentration());
            entity.setUnindexedProperty("horaire", new Text(horaireJson));

            datastore.put(entity);
//        }

        resp.sendRedirect("/importhoraire.jsp");
    }
}