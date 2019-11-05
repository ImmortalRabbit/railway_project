package main;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.util.Pair;
import jdk.nashorn.internal.parser.JSONParser;
import main.graph.Graph;
import main.wrappers.Passenger;
import main.wrappers.Route;
import main.wrappers.RouteBuyTicket;
import main.wrappers.Ticket;
import org.glassfish.jersey.internal.util.Base64;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import static main.SqlUtils.*;
import static main.Utils.*;

@Path("/items")
public class RailwayService extends HttpServlet {
    Graph graph;
    Connection connection;
    DataOutputStream dout;
    DataInputStream din;
    boolean initalized;

    public RailwayService(Connection connection) throws IOException {
        this.graph = initalizeGraph(graph);
        this.initalized = false;
        this.connection = connection;
    }

    @GET
    @Path("initialize")
    public Response init(@Context ServletContext servletContext) {

        if (!this.initalized){
            Pair<DataInputStream, DataOutputStream> pair = initializeSocket(servletContext, this.dout, this.din);
            this.din = pair.getKey();
            this.dout = pair.getValue();
            this.initalized = true;
        }
        return Response.ok().build();
    }

    @GET
    @Path("{depart}/{dest}/{date}/{red}/{route}")
    public Response getData1(@PathParam("depart") String depart,
                             @PathParam("dest") String dest,
                             @PathParam("date") String datey,
                             @PathParam("red") String dateh,
                             @PathParam("route") int route) throws IOException {

        List<Route> params = new ArrayList();
        String result = findMapRoute(connection, route, datey, depart, dest, this.din, this.dout);
        Gson gson = new Gson();
        return Response.ok(gson.toJson(result)).build();
    }

    @GET
    @Path("{depart}/{dest}/{date}")
    public Response getData(@PathParam("depart") String depart,
                            @PathParam("dest") String dest,
                            @PathParam("date") String date) {

        List<Route> params = findRoute(depart, dest, date, connection);

        Gson gson = new Gson();
        return Response.ok(gson.toJson(params)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("registration")
    public Response postListItem(@FormParam("email") String email, @FormParam("password") String password, @FormParam("phone") String phone,
                                 @FormParam("firstName") String firstName, @FormParam("lastName") String lastName) {

        return register(connection, email, firstName, lastName, password, phone);

    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/secured/userProfile")
    public Response userProfile(@FormParam("authToken") String authToken) {

        return getUserProfile(connection, authToken);

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("buyTicket")
    public Response postNewTickets(String js) {
        Gson gson  = new Gson();
        RouteBuyTicket route = gson.fromJson(js, RouteBuyTicket.class);

        buyTicket(connection, route);

        return Response.ok().build();
    }

    @POST
    @Path("cancelTicket")
    public Response cancelTicket(@QueryParam("ticket_id") int ticket_id){
        System.out.println(ticket_id);
        deleteTicket(connection, ticket_id);
        return Response.ok().build();
    }

    @GET
    @Path("secured/login")
    @Produces("text/html")
    public Response redirect(@Context HttpServletRequest request, @Context HttpServletResponse response) throws IOException {
        return Response.status(Response.Status.ACCEPTED).build();
    }
}
