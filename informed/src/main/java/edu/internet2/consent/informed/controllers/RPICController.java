/*
 * Copyright 2015 - 2019 Duke University
 
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License Version 2 as published by
    the Free Software Foundation.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License Version 2
    along with this program.  If not, see <https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt>.

 */
package edu.internet2.consent.informed.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.informed.cfg.InformedConfig;
import edu.internet2.consent.informed.model.ReturnedRPRequiredInfoItemList;
import edu.internet2.consent.informed.model.LogCriticality;
import edu.internet2.consent.informed.model.ReturnedRPMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRPOptionalInfoItemList;
import edu.internet2.consent.informed.util.InformedUtility;
import edu.internet2.consent.informed.util.OMSingleton;

@Path("/rpic")
public class RPICController {

	@SuppressWarnings("unused")
	private String caller="";
	//@SuppressWarnings("unused")
	//private final Log LOG = LogFactory.getLog(RHICController.class);
	
	// Response builder
	private Response buildResponse(Status code, String entity) {
		return Response.status(code).entity(entity).header("Access-Control-Allow-Origin","http://editor.swagger.io").header("Access-Control-Allow-methods","GET, POST, PUT, DELETE").header("Access-Control-Allow-Credentials","true").header("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept").type("application/json").encoding("UTF-8").build();
	}
	
	@GET
	@Path("/healthcheck")
	public Response healthCheck(@Context HttpServletRequest request, @Context HttpHeaders headers) {
		// We do a simple check against the database to verify that we have 
		// DB access, and then return based on that either 200 or 500.
		
		boolean healthy = false;  // unhealthy until proven otherwise
		
		Session sess = InformedUtility.getHibernateSession();
		
		if (sess == null) {
			return buildResponse(Status.INTERNAL_SERVER_ERROR,"No Session");
		}
		
		long c = 0;
		
		try {
			@SuppressWarnings("rawtypes")
			Query q =  sess.createSQLQuery("select 1 from dual");
			c =  ((Integer) q.uniqueResult()).longValue();
			if (c == 1) {
				healthy = true;
			}
		} catch (Exception e) {
			// ignore
			return buildResponse(Status.INTERNAL_SERVER_ERROR,"Exception: " + e.getMessage());
		} finally {
			if (sess != null) 
				sess.close();
		}
		
		if (healthy) 
			return buildResponse(Status.OK,"");
		else
			return buildResponse(Status.INTERNAL_SERVER_ERROR,"Check returned " + c);
		
	}
	
	// CORS
	@OPTIONS
	@Path("/")
	public Response optionsRoot(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	
	@OPTIONS
	@Path("/iilist/{rhtype}/{rhvalue}/{rptype}/{rpvalue}")
	public Response optionsIilistRP(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	
	@OPTIONS
	@Path("/metainformation/{rhtype}/{rhvalue}/{rptype}/{rpvalue}")
	public Response optionsMetaInformationRP(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	
	// Resource handlers for optional info item lists in RH/RP
	@DELETE
	@Path("/optionaliilist/{rhtype}/{rhvalue}/{rptype}/{rpvalue}")
	public Response deleteOptionalIilistRP(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein, @PathParam("rptype") String rptype, @PathParam("rpvalue") String rpvaluein) {
		
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("deleteOptionalInfoItemList", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004");
		}
		
		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);
		String rpvalue = InformedUtility.idUnEscape(rpvaluein);

		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018");
		}

		// Get a transaction
		Transaction tx = sess.beginTransaction();
		try {
		Query<ReturnedRPOptionalInfoItemList> getQuery = sess.createQuery("from ReturnedRPOptionalInfoItemList where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid and rpidentifier.rptype = :rptype and rpidentifier.rpid = :rpid and state = :state",ReturnedRPOptionalInfoItemList.class);
		getQuery.setParameter("rhtype", rhtype);
		getQuery.setParameter("rhid", rhvalue);
		getQuery.setParameter("rptype", rptype);
		getQuery.setParameter("rpid", rpvalue);
		getQuery.setParameter("state","active");  // only operate on active entry
		
		List<ReturnedRPOptionalInfoItemList> rril = getQuery.list();
		
		if (! rril.isEmpty()) {
			rril.get(0).setState("inactive");
			tx.commit();
			sess.close();
			try {
				InformedUtility.locLog("ERR0070","Deleted optional II list for " + rptype + "," + rpvalue);
			} catch (Exception e) {
				// ignore -- best effort logging
			}
		} else {
			tx.commit();
			sess.close();
		}
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException("Transaction rollback",e);
		} finally {
			if (sess.isOpen())
				sess.close();
		}
		return buildResponse(Status.NO_CONTENT,"");
		
	}
	@GET
	@Path("/optionaliilist/")
	public Response getIiListsAll(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		// Get all the optional II lists for all RPs across all the RHs

		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getOptionalInfoItemList", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004");
		}


		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018");
		}

		Query<ReturnedRPOptionalInfoItemList> retQuery = sess.createQuery("from ReturnedRPOptionalInfoItemList where state = :state",ReturnedRPOptionalInfoItemList.class);
		retQuery.setParameter("state", "active");  // only active entry
	
		
		List<ReturnedRPOptionalInfoItemList> rril = retQuery.list();
		
		if (rril.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065", LogCriticality.info);
		} else {
			try {
				//ObjectMapper mapper = new ObjectMapper();
				ObjectMapper mapper = OMSingleton.getInstance().getOm();
				return buildResponse(Status.OK,mapper.writeValueAsString(rril));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016");
			} finally {
				sess.close();
			}
		}
	}
	@GET
	@Path("/optionaliilist/{rhtype}/{rhvalue}")
	public Response getIilistsRH(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein) {
		// Get all the optional II lists for RPs slaved to specified RH
		
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getOptionalInfoItemList", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004");
		}

		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018");
		}

		Query<ReturnedRPOptionalInfoItemList> retQuery = sess.createQuery("from ReturnedRPOptionalInfoItemList where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid and state = :state",ReturnedRPOptionalInfoItemList.class);
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhid", rhvalue);
		retQuery.setParameter("state", "active");  // only active entry
	
		
		List<ReturnedRPOptionalInfoItemList> rril = retQuery.list();
		
		if (rril.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065", LogCriticality.info);
		} else {
			try {
				//ObjectMapper mapper = new ObjectMapper();
				ObjectMapper mapper = OMSingleton.getInstance().getOm();
				return buildResponse(Status.OK,mapper.writeValueAsString(rril));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016");
			} finally {
				sess.close();
			}
		}
	}
	@GET
	@Path("/optionaliilist/{rhtype}/{rhvalue}/{rptype}/{rpvalue}")
	public Response getIilistRP(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein, @PathParam("rptype") String rptype, @PathParam("rpvalue") String rpvaluein) {
		
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getOptionalInfoItemList", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004");
		}

		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);
		String rpvalue = InformedUtility.idUnEscape(rpvaluein);
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018");
		}

		Query<ReturnedRPOptionalInfoItemList> retQuery = sess.createQuery("from ReturnedRPOptionalInfoItemList where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid and rpidentifier.rptype = :rptype and rpidentifier.rpid = :rpid and state = :state",ReturnedRPOptionalInfoItemList.class);
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhid", rhvalue);
		retQuery.setParameter("rptype",rptype);
		retQuery.setParameter("rpid", rpvalue);
		retQuery.setParameter("state", "active");  // only active
		
		List<ReturnedRPOptionalInfoItemList> rril = retQuery.list();
		
		if (rril.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065", LogCriticality.info);
		} else {
			try {
				return buildResponse(Status.OK,rril.get(0).toJSON());
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016");
			} finally {
				sess.close();
			}
		}
	}
	
	@PUT
	@Path("/optionaliilist/{rhtype}/{rhvalue}/{rptype}/{rpvalue}")
	public Response putOptionalIilistRP(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein, @PathParam("rptype") String rptype, @PathParam("rpvalue") String rpvaluein) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("putOptionalInfoItemList", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004");
		}
		
		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);
		String rpvalue = InformedUtility.idUnEscape(rpvaluein);
		
		// Deserialize the input
		
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		ReturnedRPOptionalInfoItemList roil = null;
		
		try {
			roil = mapper.readValue(entity, ReturnedRPOptionalInfoItemList.class);
		} catch (JsonParseException e) {
			return InformedUtility.locError(400, "ERR0005",LogCriticality.info);
		} catch (JsonMappingException e) {
			return InformedUtility.locError(400, "ERR0006",LogCriticality.info);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0007");
		}
		
		// now we are authorized and we have a valid object
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018");
		}
		
		// Start a transaction
		Transaction tx = sess.beginTransaction();
		
		// Check to see if we can retrieve the entry
		Query<ReturnedRPOptionalInfoItemList> retQuery = sess.createQuery("from ReturnedRPOptionalInfoItemList where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid and rpidentifier.rptype = :rptype and rpidentifier.rpid = :rpid and state = :state",ReturnedRPOptionalInfoItemList.class);
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhid", rhvalue);
		retQuery.setParameter("rptype", rptype);
		retQuery.setParameter("rpid", rpvalue);
		retQuery.setParameter("state", "active");  // only active
		
		List<ReturnedRPOptionalInfoItemList> retList = retQuery.list();
		
		ReturnedRPOptionalInfoItemList tosave = null;
		try {
		if (retList.isEmpty()) {
			// just store the object we have
			// with updated tourist information
			roil.setState("active");
			roil.setUpdated(System.currentTimeMillis());
			roil.setVersion(1); // always start at 1
			sess.save(roil);
			tx.commit();
			sess.close();
			tosave = roil;
			try {
				InformedUtility.locLog("ERR0070","Created optional II list for RP " + rptype + "," + rpvalue);
			} catch (Exception e) {
				// ignore - best effort logging
			}
		} else {
			// update retList[0] with ritl data and save it
			// retList.get(0).setOptionallist(roil.getOptionallist());  // versioning now
			retList.get(0).setState("inactive");
			roil.setState("active");;
			roil.setUpdated(System.currentTimeMillis());
			roil.setVersion(retList.get(0).getVersion() + 1);
			tosave = roil;
			sess.save(roil);
			//tosave = retList.get(0);
			tx.commit();
			sess.close();
			try {
				InformedUtility.locLog("ERR0070","Updated optional II list for RP " + rptype + "," + rpvalue + " to version " + roil.getVersion());
			} catch (Exception e) {
				// ignore - best effort logging
			}
		} 
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException("Transaction rollback",e);
		} finally {
			if (sess.isOpen())
				sess.close();
		}
		
		// And return what we stored
		try {
			try {
				InformedUtility.locDebug("ERR0070","New optional II list: " + tosave.toJSON());
			} catch (Exception e) {
				// ignore -- best effort logging
			}
			return buildResponse(Status.OK,tosave.toJSON());
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0016");
		}
	}
	
	// Resource handlers for required info item lists -- required versus optional info items for RPs in RHs
	@DELETE
	@Path("/requirediilist/{rhtype}/{rhvalue}/{rptype}/{rpvalue}")
	public Response deleteRequiredIilistRP(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein, @PathParam("rptype") String rptype, @PathParam("rpvalue") String rpvaluein) {
		
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("deleteRequiredInfoItemList", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004");
		}
		
		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);
		String rpvalue = InformedUtility.idUnEscape(rpvaluein);

		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018");
		}

		// Get a transaction
		Transaction tx = sess.beginTransaction();
		try {
		Query<ReturnedRPRequiredInfoItemList> getQuery = sess.createQuery("from ReturnedRPRequiredInfoItemList where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid and rpidentifier.rptype = :rptype and rpidentifier.rpid = :rpid and state = :state",ReturnedRPRequiredInfoItemList.class);
		getQuery.setParameter("rhtype", rhtype);
		getQuery.setParameter("rhid", rhvalue);
		getQuery.setParameter("rptype", rptype);
		getQuery.setParameter("rpid", rpvalue);
		getQuery.setParameter("state", "active");
		
		List<ReturnedRPRequiredInfoItemList> rril = getQuery.list();
		
		if (! rril.isEmpty()) {
			// sess.delete(rril.get(0));  // now with versioning
			rril.get(0).setState("inactive");
			tx.commit();
			sess.close();
			try {
				InformedUtility.locLog("ERR0070","Deleted required II list for " + rptype + "," + rpvalue);
			} catch (Exception e) {
				// ignore -- best effort logging
			}
		} else {
			tx.commit();
			sess.close();
		}
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException("Transaction rollback",e);
		} finally {
			if (sess.isOpen())
				sess.close();
		}
		return buildResponse(Status.NO_CONTENT,"");
		
	}
	@GET
	@Path("/requirediilist/")
	public Response regRequiredIilistAll(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getRequiredInfoItemList", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004");
		}

		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018");
		}

		Query<ReturnedRPRequiredInfoItemList> retQuery = sess.createQuery("from ReturnedRPRequiredInfoItemList where state = :state",ReturnedRPRequiredInfoItemList.class);
		retQuery.setParameter("state", "active");  // active only
		
		List<ReturnedRPRequiredInfoItemList> rril = retQuery.list();
		
		if (rril.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065", LogCriticality.info);
		} else {
			try {
				//ObjectMapper mapper = new ObjectMapper();
				ObjectMapper mapper = OMSingleton.getInstance().getOm();
				return buildResponse(Status.OK, mapper.writeValueAsString(rril));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016");
			} finally {
				sess.close();
			}
		}
	}
	@GET
	@Path("/requirediilist/{rhtype}/{rhvalue}")
	public Response getRequiredIilistRH(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getRequiredInfoItemList", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004");
		}

		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018");
		}

		Query<ReturnedRPRequiredInfoItemList> retQuery = sess.createQuery("from ReturnedRPRequiredInfoItemList where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid and state = :state",ReturnedRPRequiredInfoItemList.class);
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhid", rhvalue);
		retQuery.setParameter("state", "active");  // only active

		
		List<ReturnedRPRequiredInfoItemList> rril = retQuery.list();
		
		if (rril.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				//ObjectMapper mapper = new ObjectMapper();
				ObjectMapper mapper = OMSingleton.getInstance().getOm();
				return buildResponse(Status.OK,mapper.writeValueAsString(rril));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016");
			} finally {
				sess.close();
			}
		}
	}
	@GET
	@Path("/requirediilist/{rhtype}/{rhvalue}/{rptype}/{rpvalue}")
	public Response getRequiredIilistRP(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein, @PathParam("rptype") String rptype, @PathParam("rpvalue") String rpvaluein) {
		
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getRequiredInfoItemList", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004");
		}

		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);
		String rpvalue = InformedUtility.idUnEscape(rpvaluein);
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018");
		}

		Query<ReturnedRPRequiredInfoItemList> retQuery = sess.createQuery("from ReturnedRPRequiredInfoItemList where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid and rpidentifier.rptype = :rptype and rpidentifier.rpid = :rpid and state = :state",ReturnedRPRequiredInfoItemList.class);
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhid", rhvalue);
		retQuery.setParameter("rptype",rptype);
		retQuery.setParameter("rpid", rpvalue);
		retQuery.setParameter("state", "active");  // only active
		
		List<ReturnedRPRequiredInfoItemList> rril = retQuery.list();
		
		if (rril.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				return buildResponse(Status.OK,rril.get(0).toJSON());
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016");
			} finally {
				sess.close();
			}
		}
	}
	
	@PUT
	@Path("/requirediilist/{rhtype}/{rhvalue}/{rptype}/{rpvalue}")
	public Response putRequiredIilistRP(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein, @PathParam("rptype") String rptype, @PathParam("rpvalue") String rpvaluein) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("putRequiredInfoItemList", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004");
		}
		
		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);
		String rpvalue = InformedUtility.idUnEscape(rpvaluein);
		
		// Deserialize the input
		
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		
		ReturnedRPRequiredInfoItemList rril = null;
		
		try {
			rril = mapper.readValue(entity, ReturnedRPRequiredInfoItemList.class);
		} catch (JsonParseException e) {
			return InformedUtility.locError(400, "ERR0005", LogCriticality.info);
		} catch (JsonMappingException e) {
			return InformedUtility.locError(400, "ERR0006", LogCriticality.info);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0007");
		}
		
		// now we are authorized and we have a valid object
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018");
		}
		
		// Start a transaction
		Transaction tx = sess.beginTransaction();
		
		// Check to see if we can retrieve the entry
		Query<ReturnedRPRequiredInfoItemList> retQuery = sess.createQuery("from ReturnedRPRequiredInfoItemList where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid and rpidentifier.rptype = :rptype and rpidentifier.rpid = :rpid and state = :state",ReturnedRPRequiredInfoItemList.class);
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhid", rhvalue);
		retQuery.setParameter("rptype", rptype);
		retQuery.setParameter("rpid", rpvalue);
		retQuery.setParameter("state", "active");  // only retrieve active entry
		
		List<ReturnedRPRequiredInfoItemList> retList = retQuery.list();
		
		ReturnedRPRequiredInfoItemList tosave = null;
		try {
		if (retList.isEmpty()) {
			// just store the object we have
			// with additional tourist info
			rril.setVersion(1);
			rril.setState("active");
			rril.setUpdated(System.currentTimeMillis());
			sess.save(rril);
			tx.commit();
			sess.close();
			tosave = rril;
			try {
				InformedUtility.locLog("ERR0070","Created required II list for " + rptype + "," + rpvalue);
			} catch (Exception e) {
				// ignore -- best effort logging
			}
		} else {
			// update retList[0] with ritl data and save it
			// retList.get(0).setRequiredlist(rril.getRequiredlist());  // now we are versioned instead
			retList.get(0).setState("inactive");
			rril.setVersion(retList.get(0).getVersion() + 1);
			rril.setState("active");
			rril.setUpdated(System.currentTimeMillis());
			sess.save(rril);
			tosave = rril;
			tx.commit();
			sess.close();
			try {
				InformedUtility.locLog("ERR0070","Updated required II list for " + rptype + "," + rpvalue + " to version " + rril.getVersion());
			} catch (Exception e) {
				// ignore -- best effort logging
			}
		}
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException("Transaction rollback",e);
		} finally {
			if (sess.isOpen())
				sess.close();
		}
		
		
		// And return what we stored
		try {
			return buildResponse(Status.OK,tosave.toJSON());
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0016");
		}
	}
	
	// Resource handlers for metainformation
	
	@DELETE
	@Path("/metainformation/{rhtype}/{rhvalue}/{rptype}/{rpvalue}")
	public Response deleteMetaInformationRP(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein, @PathParam("rptype") String rptype, @PathParam("rpvalue") String rpvaluein) {
		
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("deleteMetaInformation", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004");
		}
		
		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);
		String rpvalue = InformedUtility.idUnEscape(rpvaluein);

		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018");
		}

		// Get a transaction
		Transaction tx = sess.beginTransaction();
		try {
		Query<ReturnedRPMetaInformation> getQuery = sess.createQuery("from ReturnedRPMetaInformation where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid and rpidentifier.rptype = :rptype and rpidentifier.rpid = :rpid and state = :state",ReturnedRPMetaInformation.class);
		getQuery.setParameter("rhtype", rhtype);
		getQuery.setParameter("rhid", rhvalue);
		getQuery.setParameter("rptype", rptype);
		getQuery.setParameter("rpid", rpvalue);
		getQuery.setParameter("state", "active");  // only relevant for active entry
		
		List<ReturnedRPMetaInformation> rrmi = getQuery.list();
		
		if (! rrmi.isEmpty()) {
		//	sess.delete(rrmi.get(0)); // now versioned
			rrmi.get(0).setState("inactive");
			tx.commit();
			try {
				InformedUtility.locLog("ERR0070","Deleted metainformation for RP " + rptype + "," + rpvalue);
			} catch (Exception e) {
				// ignore -- best effort logging
			}
		} else {
			tx.commit();
		}
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException("Transaction rollback",e);
		} finally {
			if (sess.isOpen())
				sess.close();
		}
		
		return buildResponse(Status.NO_CONTENT,"");
		
	}
	
	@GET
	@Path("/metainformation/")
	public Response GetMetaInformationAll(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getMetaInformation", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004");
		}
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018");
		}

		Query<ReturnedRPMetaInformation> retQuery = sess.createQuery("from ReturnedRPMetaInformation where state = :state",ReturnedRPMetaInformation.class);
		retQuery.setParameter("state","active");  // only active
		
		List<ReturnedRPMetaInformation> rrmi = retQuery.list();
		
		if (rrmi.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				//ObjectMapper mapper = new ObjectMapper();
				ObjectMapper mapper = OMSingleton.getInstance().getOm();
				
				return buildResponse(Status.OK,mapper.writeValueAsString(rrmi));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016");
			} finally {
				sess.close();
			}
		}
	}
	
	@GET 
	@Path("/metainformation/{rhtype}/{rhvalue}")
	public Response getMetaInformationRH(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getMetaInformation", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004");
		}
		
		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);
		

		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018");
		}

		Query<ReturnedRPMetaInformation> retQuery = sess.createQuery("from ReturnedRPMetaInformation where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid and state = :state",ReturnedRPMetaInformation.class);
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhid", rhvalue);
		retQuery.setParameter("state", "active");  // only retrieve actives

		
		List<ReturnedRPMetaInformation> rrmi = retQuery.list();
		
		if (rrmi.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065", LogCriticality.info);
		} else {
			try {
				//ObjectMapper mapper = new ObjectMapper();
				ObjectMapper mapper = OMSingleton.getInstance().getOm();
				
				return buildResponse(Status.OK,mapper.writeValueAsString(rrmi));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016");
			} finally {
				sess.close();
			}
		}
	}
	@GET
	@Produces("application/json;charset=UTF-8")
	@Path("/metainformation/{rhtype}/{rhvalue}/{rptype}/{rpvalue}")
	public Response getMetaInformationRP(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein, @PathParam("rptype") String rptype, @PathParam("rpvalue") String rpvaluein) {
		
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getMetaInformation", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004");
		}
		
		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);
		String rpvalue = InformedUtility.idUnEscape(rpvaluein);
		

		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018");
		}

		Query<ReturnedRPMetaInformation> retQuery = sess.createQuery("from ReturnedRPMetaInformation where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid and rpidentifier.rptype = :rptype and rpidentifier.rpid = :rpid and state = :state",ReturnedRPMetaInformation.class);
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhid", rhvalue);
		retQuery.setParameter("rptype",rptype);
		retQuery.setParameter("rpid", rpvalue);
		retQuery.setParameter("state", "active");  // only active
		
		List<ReturnedRPMetaInformation> rrmi = retQuery.list();
		
		if (rrmi.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				ReturnedRPMetaInformation ri = rrmi.get(0);
				
				return buildResponse(Status.OK,ri.toJSON());
			} catch (Exception e) {
				try {
					InformedUtility.locDebug("ERR0069",LogCriticality.debug,rrmi.get(0).toJSON());
				} catch (Exception w) {
					throw new RuntimeException(w);
				}
				return InformedUtility.locError(500,"ERR0016");
			} finally {
				sess.close();
			}
		}
	}
	
	@PUT
	@Consumes("application/json;charset=UTF-8")
	@Produces("application/json;charset=UTF-8")
	@Path("/metainformation/{rhtype}/{rhvalue}/{rptype}/{rpvalue}")
	public Response putMetaInformationRP(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein, @PathParam("rptype") String rptype, @PathParam("rpvalue") String rpvaluein) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("putMetaInformation", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004");
		}
	
		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);
		String rpvalue = InformedUtility.idUnEscape(rpvaluein);
		
		// Deserialize the input
		
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		
		ReturnedRPMetaInformation rrmi = null;
		
		try {
			rrmi = mapper.readValue(entity, ReturnedRPMetaInformation.class);
			
		} catch (JsonParseException e) {
			return InformedUtility.locError(400, "ERR0005",LogCriticality.info);
		} catch (JsonMappingException e) {
			return InformedUtility.locError(400, "ERR0006",LogCriticality.info);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0007");
		}
		
		// now we are authorized and we have a valid object
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018");
		}
		
		// Start a transaction
		Transaction tx = sess.beginTransaction();
		
		// Check to see if we can retrieve the entry
		Query<ReturnedRPMetaInformation> retQuery = sess.createQuery("from ReturnedRPMetaInformation where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid and rpidentifier.rptype = :rptype and rpidentifier.rpid = :rpid and state = :state",ReturnedRPMetaInformation.class);
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhid", rhvalue);
		retQuery.setParameter("rptype", rptype);
		retQuery.setParameter("rpid", rpvalue);
		retQuery.setParameter("state", "active");  // only active

		List<ReturnedRPMetaInformation> retList = retQuery.list();
		
		ReturnedRPMetaInformation tosave = null;
		try {
		if (retList.isEmpty()) {
			// just store the object we have
			// with additional tourist info
			rrmi.setVersion(1);;
			rrmi.setState("active");
			rrmi.setUpdated(System.currentTimeMillis());
			sess.save(rrmi);
			tx.commit();
			tosave = rrmi;
			try {
				InformedUtility.locLog("ERR0070","Created RP metainformation for " + rptype + "," + rpvalue);
				InformedUtility.locDebug("ERR0070","New RP metainformation: " + rrmi.toJSON());
			} catch (Exception e) {
				// ignore -- best effort logging
			}
		} else {

			// Now versioned
			retList.get(0).setState("inactive");
			rrmi.setVersion(retList.get(0).getVersion() + 1);
			rrmi.setState("active");
			rrmi.setUpdated(System.currentTimeMillis());
			sess.save(rrmi);
			tosave = rrmi;
			tx.commit();
			try {
				InformedUtility.locLog("ERR0070","Updated RP metainformation for " + rptype + "," + rpvalue + " to version " + rrmi.getVersion());
			} catch (Exception e) {
				// ignore -- best effort logging
			}
		}
		try {
			
			return buildResponse(Status.OK,tosave.toJSON());
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0016");
		}
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException("Transaction rollback",e);
		} finally {
			if (sess.isOpen())
				sess.close();
		}
	}
}

