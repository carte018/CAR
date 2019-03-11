/* DEPRECATED */
/*package edu.internet2.consent.icm.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.icm.util.IcmUtility;
import edu.internet2.consent.incon.model.InfoItemInformedContent;
import edu.internet2.consent.incon.model.RelyingPartyInformedContent;

@Path("informed-content")
public class InformedContentController {

	private Response buildResponse(Status code, String entity) {
		return Response.status(code).entity(entity).header("Access-Control-Allow-Origin", "http://editor.swagger.io").header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH").header("Access-Control-Allow-Credentials", "true").header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept").type("application/json").build();
	}
	
	// Set of handler methods for GET/POST (retrieve/create) informed content for relying parties and/or infoitems
	// This is *very* rudimentary and solely for the purpose of enabling testing and demos
	// TODO:  This CANNOT go to production as-is
	
	@GET
	@Path("/rpic")
	@Produces({"application/json"})
	public Response getRPIC(@Context HttpServletRequest request, @Context HttpHeaders headers) {
		// HIBERNATE
		Session sess = IcmUtility.getHibernateSession();
		if (sess == null) {
			return IcmUtility.locError(500, "ERR0018");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("from RelyingPartyInformedContent where rpType is not null");
		if (request.getParameter("rpType") != null && ! request.getParameter("rpType").equals("")) {
			sb.append(" and rpType = :rptype");
			if (request.getParameter("rpValue") != null && ! request.getParameter("rpValue").equals("")) {
				sb.append(" and rpValue = :rpvalue");
			}
		}
		Query getQuery = sess.createQuery(sb.toString());
		if (request.getParameter("rpType") != null && ! request.getParameter("rpType").equals("")) {
			getQuery.setString("rptype", request.getParameter("rpType"));
			if (request.getParameter("rpValue") != null && ! request.getParameter("rpValue").equals("")) {
				getQuery.setString("rpvalue",request.getParameter("rpValue"));
			}
		}
		@SuppressWarnings("unchecked")
		List<RelyingPartyInformedContent> rl = (List<RelyingPartyInformedContent>)getQuery.list();
		if (rl.isEmpty()) {
			sess.close();
			return IcmUtility.locError(404, "ERR0063");
		}
		sess.close();
		
		try {
			ObjectMapper om = new ObjectMapper();
			return buildResponse(Status.OK,om.writeValueAsString(rl));
		} catch (JsonProcessingException e) {
			return IcmUtility.locError(500, "ERR0016");
		}
	}
	@POST
	@Path("/rpic")
	@Consumes({"application/json"})
	@Produces({"application/json"})
	public Response postRPIC(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		// Very simplistic create operation for the RP informed content endpoint.
		// Given a JSON object with RP IC in it, store it in the database.  No checking, no nothing else.
		// Just store it for later use.
		
		// Map the input
		RelyingPartyInformedContent rpic = null;
		try {
			ObjectMapper om = new ObjectMapper();
			rpic = om.readValue(entity, RelyingPartyInformedContent.class);
		} catch (JsonParseException e) {
			return IcmUtility.locError(400, "ERR0005");
		} catch (JsonMappingException e) {
			return IcmUtility.locError(400, "ERR0006");
		} catch (Exception e) {
			return IcmUtility.locError(400, "ERR0007");
		}
		
		// HIBERNATE

		Session sess = IcmUtility.getHibernateSession();
		if (sess == null) {
			return IcmUtility.locError(500, "ERR0018");
		}
		
		// Perform a blind delete in case there's already something there (we replace it quietly)
		Transaction tx = sess.beginTransaction();
		Query delQuery = sess.createQuery("from RelyingPartyInformedContent where rptype = :rptype and rpvalue = :rpvalue");
		delQuery.setString("rptype", rpic.getRpType());
		delQuery.setString("rpvalue", rpic.getRpValue());
		@SuppressWarnings("unchecked")
		List<RelyingPartyInformedContent> lr = delQuery.list();
		if (! lr.isEmpty()) {
			sess.delete(lr.get(0));
		}
		tx.commit();
		sess.close();
		Session sess2 = IcmUtility.getHibernateSession();
		if (sess2 == null) {
			return IcmUtility.locError(500, "ERR0018");
		}
		Transaction tx2 = sess2.beginTransaction();
		sess2.save(rpic);
		tx2.commit();
		sess2.close();
		
		return buildResponse(Status.OK,"Success");
	}
	
	
	@GET
	@Path("/iiic")
	@Produces({"application/json"})
	public Response getIIIC(@Context HttpServletRequest request, @Context HttpHeaders headers) {
		// HIBERNATE
		Session sess = IcmUtility.getHibernateSession();
		if (sess == null) {
			return IcmUtility.locError(500, "ERR0018");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("from InfoItemInformedContent where infoName is not null");
		if (request.getParameter("infoName") != null && ! request.getParameter("infoName").equals("")) {
			sb.append(" and infoName = :infoname");
			if (request.getParameter("infoValue") != null && ! request.getParameter("infoValue").equals("")) {
				sb.append(" and infoValue = :infovalue");
			}
		}
		Query getQuery = sess.createQuery(sb.toString());
		if (request.getParameter("infoName") != null && ! request.getParameter("infoName").equals("")) {
			getQuery.setString("infoname", request.getParameter("infoName"));
			if (request.getParameter("infoValue") != null && ! request.getParameter("infoValue").equals("")) {
				getQuery.setString("infovalue",request.getParameter("infoValue"));
			}
		}
		@SuppressWarnings("unchecked")
		List<InfoItemInformedContent> rl = (List<InfoItemInformedContent>)getQuery.list();
		if (rl.isEmpty()) {
			sess.close();
			return IcmUtility.locError(404, "ERR0063");
		}
		sess.close();
		
		try {
			ObjectMapper om = new ObjectMapper();
			return buildResponse(Status.OK,om.writeValueAsString(rl));
		} catch (JsonProcessingException e) {
			return IcmUtility.locError(500, "ERR0016");
		}
	}
	@POST
	@Path("/iiic")
	@Consumes({"application/json"})
	@Produces({"application/json"})
	public Response postIIIC(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		// Very simplistic create operation for the RP informed content endpoint.
		// Given a JSON object with RP IC in it, store it in the database.  No checking, no nothing else.
		// Just store it for later use.
		
		// Map the input
		InfoItemInformedContent iiic = null;
		try {
			ObjectMapper om = new ObjectMapper();
			iiic = om.readValue(entity, InfoItemInformedContent.class);
		} catch (JsonParseException e) {
			return IcmUtility.locError(400, "ERR0005");
		} catch (JsonMappingException e) {
			return IcmUtility.locError(400, "ERR0006");
		} catch (Exception e) {
			return IcmUtility.locError(500, "ERR0007");
		}
		
		// HIBERNATE

		Session sess = IcmUtility.getHibernateSession();
		if (sess == null) {
			return IcmUtility.locError(500, "ERR0018");
		}
		
		// Perform a blind delete in case there's already something there (we replace it quietly)
		Transaction tx = sess.beginTransaction();
		Query delQuery = sess.createQuery("from InfoItemInformedContent where infoName = :infoname and infoValue = :infovalue");
		delQuery.setString("infoname", iiic.getInfoName());
		delQuery.setString("infovalue", iiic.getInfoValue());
		@SuppressWarnings("unchecked")
		List<InfoItemInformedContent> lr = delQuery.list();
		if (! lr.isEmpty()) {
			sess.delete(lr.get(0));
		}
		tx.commit();
		sess.close();
		Session sess2 = IcmUtility.getHibernateSession();
		if (sess2 == null) {
			return IcmUtility.locError(500, "ERR0018");
		}
		Transaction tx2 = sess2.beginTransaction();
		sess2.save(iiic);
		tx2.commit();
		sess2.close();
		
		return buildResponse(Status.OK,"Success");
	} 
}*/
