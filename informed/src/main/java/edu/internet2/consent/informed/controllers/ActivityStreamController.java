package edu.internet2.consent.informed.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.informed.cfg.InformedConfig;
import edu.internet2.consent.informed.model.ActivityStreamEntry;
import edu.internet2.consent.informed.util.InformedUtility;

@Path("activitystream")
public class ActivityStreamController {

	@SuppressWarnings("unused")
	private String caller="";
	@SuppressWarnings("unused")
	private final Log LOG = LogFactory.getLog(IIICController.class);
	
	// Response builder
	private Response buildResponse(Status code, String entity) {
		return Response.status(code).entity(entity).header("Access-Control-Allow-Origin","http://editor.swagger.io").header("Access-Control-Allow-methods","GET, POST, PUT, DELETE").header("Access-Control-Allow-Credentials","true").header("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept").type("application/json").build();
	}
	
	// No need for CORS here, since this isn't ever being presented via the OpenAPI interface
	
	// No need for DELETE or PUT or PATH here.  This is POST and GET only.
	
	@POST
	@Path("/")
	public Response postActivityStreamEntry(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("postActivityStreamEntry", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004");
		}
		
		// Deserialize the input
		
		ObjectMapper mapper = new ObjectMapper();
		ActivityStreamEntry entry = null;
		
		try {
			entry = mapper.readValue(entity, ActivityStreamEntry.class);
		} catch (JsonParseException e) {
			return InformedUtility.locError(400, "ERR0005");
		} catch (JsonMappingException e) {
			return InformedUtility.locError(400, "ERR0006");
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0007");
		}
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018");
		}
		
		// Override timestamp if needed
		if (entry.getTimestamp() == 0) {
			entry.setTimestamp(System.currentTimeMillis());
		}
		
		// Start a transaction
		Transaction tx = sess.beginTransaction();
		try {
			sess.save(entry);
			tx.commit();
			sess.close();
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException("Transaction rollback",e);
		} finally {
			if (sess.isOpen())
				sess.close();
		}
		
		// And return what we stored
		try {
			return buildResponse(Status.OK,entry.toJSON());
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0016");
		}
	}
	
	@GET
	@Path("/")
	public Response getActivityStreamEntries(@Context HttpServletRequest request,@Context HttpHeaders headers,String entity) {
		
		// Either return the entirity of the event list (which may be arbitrarily long) or the most recent N entries
		
		int n = 0;
		
		if (request.getParameter("recent") != null) {
			n = Integer.parseInt(request.getParameter("recent"));
		}
		
		String type = null;
		if (request.getParameter("type") != null) {
			type = request.getParameter("type");
		}
		
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getActivityStreamEntries", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004");
		}
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018");
		}
		Query<ActivityStreamEntry> retQuery = null;
		if (type == null) {
			retQuery = sess.createQuery("from ActivityStreamEntry order by timestamp desc",ActivityStreamEntry.class);
		} else {
			retQuery = sess.createQuery("from ActivityStreamEntry where type = :type order by timestamp desc",ActivityStreamEntry.class);
			retQuery.setParameter("type", type);
		}
		if (n != 0) {
			retQuery.setMaxResults(n);
		}
		
		List<ActivityStreamEntry> lase = retQuery.list();
		
		if (lase.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065");
		} else {
			try {
				ObjectMapper om = new ObjectMapper();
				return buildResponse(Status.OK,om.writeValueAsString(lase));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016");
			} finally {
				sess.close();
			}
		}
		
	}
}
