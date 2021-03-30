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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
import edu.internet2.consent.informed.model.LogCriticality;
import edu.internet2.consent.informed.model.ReturnedInfoItemMetaInformation;
import edu.internet2.consent.informed.model.ReturnedValueMetaInformation;
import edu.internet2.consent.informed.model.ScopeMapping;
import edu.internet2.consent.informed.util.InformedUtility;
import edu.internet2.consent.informed.util.OMSingleton;

@Path("/iiic")
public class IIICController {

	@SuppressWarnings("unused")
	private String caller="";
	@SuppressWarnings("unused")
	private final Log LOG = LogFactory.getLog(IIICController.class);
	
	// Response builder
	private Response buildResponse(Status code, String entity) {
		return Response.status(code).entity(entity).header("Access-Control-Allow-Origin","http://editor.swagger.io").header("Access-Control-Allow-methods","GET, POST, PUT, DELETE").header("Access-Control-Allow-Credentials","true").header("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept").type("application/json").build();
	}
	
	// CORS
	@OPTIONS
	@Path("/")
	public Response optionsRoot(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	
	@OPTIONS
	@Path("/iimetainformation/{rhtype}/{rhvalue}/{iitype}/{iivalue}")
	public Response optionsRHII(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	
	@OPTIONS
	@Path("/valuemetainformation/{iiname}/{iivalue}")
	public Response optionsVMI(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
			return buildResponse(Status.OK,"");
	}
	
	@OPTIONS
	@Path("/scopemapping/{rhtype}/{rhvalue}/{scopename}")
	public Response optionsSM(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	
	@DELETE
	@Path("/iimetainformation/{rhtype}/{rhvalue}/{iitype}/{iivalue}")
	public Response deleteIIMetaInformation(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein, @PathParam("iitype") String iitype, @PathParam("iivalue") String iivalue) {
		
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("deleteIIMetaInformation", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004", LogCriticality.error);
		}
		
		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);

		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}

		// Get a transaction
		Transaction tx = sess.beginTransaction();
		try {
		Query<ReturnedInfoItemMetaInformation> getQuery = sess.createQuery("from ReturnedInfoItemMetaInformation where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid and iiidentifier.iitype = :iitype and iiidentifier.iiid = :iiid and state = :state",ReturnedInfoItemMetaInformation.class);
		getQuery.setParameter("rhtype", rhtype);
		getQuery.setParameter("rhid", rhvalue);
		getQuery.setParameter("iitype", iitype);
		getQuery.setParameter("iiid", iivalue);
		getQuery.setParameter("state", "active");  // only retrieve the active version if one exists
		
		List<ReturnedInfoItemMetaInformation> riimi = getQuery.list();
		
		if (! riimi.isEmpty()) {
			// sess.delete(riimi.get(0));  // Now we instead deactivate 
			riimi.get(0).setState("inactive");
			tx.commit();
			sess.close();
		} else {
			//nothing to do
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
	
	// Administratively retrieve all value metainformation, regardless of its referent
	@GET
	@Path("/valueinformation/")
	public Response getAllValueMetainformation(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getValueMetaInformation", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018", LogCriticality.error);
		}

		Query<ReturnedValueMetaInformation> retQuery = sess.createQuery("from ReturnedValueMetaInformation",ReturnedValueMetaInformation.class);

		
		List<ReturnedValueMetaInformation> rvmi = retQuery.list();
		
		if (rvmi.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				//ObjectMapper mapper = new ObjectMapper();
				ObjectMapper mapper = OMSingleton.getInstance().getOm();
				
				return buildResponse(Status.OK,mapper.writeValueAsString(rvmi));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	
	// Administratively retrieve all info item metainformation, regardless of its referent
	@GET
	@Path("/iimetainformation/")
	public Response getAllIIMetainformation(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getInfoItemMetaInformation", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}

		Query<ReturnedInfoItemMetaInformation> retQuery = sess.createQuery("from ReturnedInfoItemMetaInformation where state=:state",ReturnedInfoItemMetaInformation.class);
		retQuery.setParameter("state","active");  // only retrieve active entries
		
		List<ReturnedInfoItemMetaInformation> riimi = retQuery.list();
		
		if (riimi.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				//ObjectMapper mapper = new ObjectMapper();
				ObjectMapper mapper = OMSingleton.getInstance().getOm();
				
				return buildResponse(Status.OK,mapper.writeValueAsString(riimi));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	
	@GET 
	@Path("/valuemetainformation/{iiname}/{iivalue}")
	public Response getValueMetaInformation(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("iiname") String iiname, @PathParam("iivalue") String iivaluein) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getInfoItemMetaInformation", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}

		// Unescape
		String iivalue = InformedUtility.idUnEscape(iivaluein);
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}

		Query<ReturnedValueMetaInformation> retQuery = sess.createQuery("from ReturnedValueMetaInformation where infoitemname = :iiname and infoitemvalue = :iivalue and state = :state",ReturnedValueMetaInformation.class);
		retQuery.setParameter("iiname",iiname);
		retQuery.setParameter("iivalue",iivalue);
		retQuery.setParameter("state", "active");  // only get active entries
		
		List<ReturnedValueMetaInformation> rvmi = retQuery.list();
		
		if (rvmi.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				return buildResponse(Status.OK,rvmi.get(0).toJSON());
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	
	@GET
	@Path("/iimetainformation/{rhtype}/{rhvalue}/{iitype}/{iivalue}")
	public Response getIIMetaInformation(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein, @PathParam("iitype") String iitype, @PathParam("iivalue") String iivalue) {
		
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getInfoItemMetaInformation", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}

		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}

		Query<ReturnedInfoItemMetaInformation> retQuery = sess.createQuery("from ReturnedInfoItemMetaInformation where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid and iiidentifier.iitype = :iitype and iiidentifier.iiid = :iiid and state = :state",ReturnedInfoItemMetaInformation.class);
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhid", rhvalue);
		retQuery.setParameter("iitype", iitype);
		retQuery.setParameter("iiid",iivalue);
		retQuery.setParameter("state", "active");  // Only retrieve active entries
		
		List<ReturnedInfoItemMetaInformation> riimi = retQuery.list();
		
		if (riimi.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				return buildResponse(Status.OK,riimi.get(0).toJSON());
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	@GET
	@Path("/scopemapping/{rhtype}/{rhvalue}/{scopename}")
	public Response getScopeMapping(@Context HttpServletRequest request, @Context HttpHeaders headers, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein, @PathParam("scopename") String scopename) {
		// Retrieve a ScopeMapping object by scopename
		
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getScopeMapping", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0004", LogCriticality.error);
		}
		
		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}

		Query<ScopeMapping> scopequery = sess.createQuery("from ScopeMapping where state = :state and scopename = :scopename and rhtype = :rhtype and rhvalue = :rhvalue",ScopeMapping.class);
		
		if (request.getParameter("state") != null) {
			scopequery.setParameter("state", request.getParameter("state"));
		} else {
			scopequery.setParameter("state","active");
		}
		scopequery.setParameter("rhtype", rhtype);
		scopequery.setParameter("rhvalue", rhvalue);
		scopequery.setParameter("scopename", scopename);
		
		List<ScopeMapping> sml = scopequery.list();
		
		if (sml.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				return buildResponse(Status.OK,sml.get(0).toJSON());
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	
	@PUT
	@Path("/scopemapping/{rhtype}/{rhvalue}/{scopename}")
	public Response putScopeMapping(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein, @PathParam("scopename") String scopename) {
		
		// Init
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("putScopeMapping",request,headers,entity);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0004", LogCriticality.error);
		}
		
		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);
		
		// Deserialize
		
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		
		ScopeMapping smin = null;
		
		try {
			smin = mapper.readValue(entity, ScopeMapping.class);
		} catch (JsonParseException e) {
			return InformedUtility.locError(400, "ERR0005",LogCriticality.info);
		} catch (JsonMappingException e) {
			return InformedUtility.locError(400, "ERR0006",LogCriticality.info);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0007",LogCriticality.error);
		}
		
		// now we are authorized and we have a valid object
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}
		
		// Start a transaction
		Transaction tx = sess.beginTransaction();
		
		// Check to see if we can retrieve the entry
		Query<ScopeMapping> retQuery = sess.createQuery("from ScopeMapping where rhtype = :rhtype and rhvalue = :rhvalue and scopename = :scopename and state = 'active'",ScopeMapping.class);
		
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhvalue", rhvalue);
		retQuery.setParameter("scopename", scopename);  // we want the active version if there are multiple in the database
		
		List<ScopeMapping> retList = retQuery.list();
		
		ScopeMapping tosave = null;
		try {
		if (retList.isEmpty()) {
			// just store the object we have
			// First, though, make sure we have values for the state parameters
			smin.setVersion(1);  // This becomes version one if there's not one already in place
			smin.setState("active");  // This becomes active regardless
			smin.setUpdated(System.currentTimeMillis()); // creation time set to now
			sess.save(smin);
			tx.commit();
			sess.close();
			tosave = smin;
		} else {
			// update retList[0] with smin data and save it
			retList.get(0).setState("inactive"); // make the current one inactive
			smin.setVersion(retList.get(0).getVersion() + 1);
			smin.setState("active");;
			smin.setUpdated(System.currentTimeMillis());
			sess.save(smin);
			tosave = smin;
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

		
		// And return what we stored
		try {
			return buildResponse(Status.OK,tosave.toJSON());
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0016",LogCriticality.error);
		}
	}
	
	@PUT
	@Path("/valuemetainformation/{iiname}/{iivalue}")
	public Response putValueMetaInformation(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("iiname") String iiname, @PathParam("iivalue") String iivaluein) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("putValueMetaInformation", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}
		
		// Unescape
		String iivalue = InformedUtility.idUnEscape(iivaluein);
		
		// Deserialize the input
		
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		
		ReturnedValueMetaInformation rvmi = null;
		
		try {
			rvmi = mapper.readValue(entity, ReturnedValueMetaInformation.class);
		} catch (JsonParseException e) {
			return InformedUtility.locError(400, "ERR0005",LogCriticality.info);
		} catch (JsonMappingException e) {
			return InformedUtility.locError(400, "ERR0006",LogCriticality.info);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0007",LogCriticality.error);
		}
		
		// now we are authorized and we have a valid object
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}
		
		// Start a transaction
		Transaction tx = sess.beginTransaction();
		
		// Check to see if we can retrieve the entry
		Query<ReturnedValueMetaInformation> retQuery = sess.createQuery("from ReturnedValueMetaInformation where infoitemname = :iiname and infoitemvalue = :iivalue and state = :state",ReturnedValueMetaInformation.class);
		retQuery.setParameter("iiname", iiname);
		retQuery.setParameter("iivalue", iivalue);
		retQuery.setParameter("state", "active");  // we want the active version if there are multiple in the database
		
		List<ReturnedValueMetaInformation> retList = retQuery.list();
		
		ReturnedValueMetaInformation tosave = null;
		try {
		if (retList.isEmpty()) {
			// just store the object we have
			// First, though, make sure we have values for the state parameters
			rvmi.setVersion(1);  // This becomes version one if there's not one already in place
			rvmi.setState("active");  // This becomes active regardless
			rvmi.setUpdated(System.currentTimeMillis()); // creation time set to now
			sess.save(rvmi);
			tx.commit();
			sess.close();
			tosave = rvmi;
		} else {
			// update retList[0] with ritl data and save it
			retList.get(0).setState("inactive"); // make the current one inactive
			rvmi.setVersion(retList.get(0).getVersion());
			rvmi.setState("active");;
			rvmi.setUpdated(System.currentTimeMillis());
			sess.save(rvmi);
			tosave = rvmi;
			sess.close();
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
			return InformedUtility.locError(500, "ERR0016",LogCriticality.error);
		}
	}
	
	@PUT
	@Path("/iimetainformation/{rhtype}/{rhvalue}/{iitype}/{iivalue}")
	public Response putIIMetaInformation(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein, @PathParam("iitype") String iitype, @PathParam("iivalue") String iivalue) {
		
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("putIIMetaInformation", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}
		
		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);
		
		// Deserialize the input
		
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		
		ReturnedInfoItemMetaInformation riimi = null;
		
		try {
			riimi = mapper.readValue(entity, ReturnedInfoItemMetaInformation.class);
		} catch (JsonParseException e) {
			return InformedUtility.locError(400, "ERR0005",LogCriticality.info);
		} catch (JsonMappingException e) {
			return InformedUtility.locError(400, "ERR0006",LogCriticality.info);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0007",LogCriticality.error);
		}
		
		// now we are authorized and we have a valid object
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}
		
		// Start a transaction
		Transaction tx = sess.beginTransaction();
		
		// Check to see if we can retrieve the entry
		Query<ReturnedInfoItemMetaInformation> retQuery = sess.createQuery("from ReturnedInfoItemMetaInformation where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid and iiidentifier.iitype = :iitype and iiidentifier.iiid = :iiid and state = :state",ReturnedInfoItemMetaInformation.class);
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhid", rhvalue);
		retQuery.setParameter("iitype", iitype);
		retQuery.setParameter("iiid", iivalue);
		retQuery.setParameter("state", "active");  // only retrieve if active
		
		List<ReturnedInfoItemMetaInformation> retList = retQuery.list();
		
		ReturnedInfoItemMetaInformation tosave = null;
		try {
		if (retList.isEmpty()) {
			// just store the object we have
			// with current status information
			riimi.setVersion(1); // start with version 1
			riimi.setState("active"); // start with active
			riimi.setUpdated(System.currentTimeMillis());
			sess.save(riimi);
			tx.commit();
			sess.close();
			tosave = riimi;
		} else {
			// update retList[0] with ritl data and save it
			// Rather, now we update retList[0] with new status information and create a whole new object
			retList.get(0).setState("inactive");  // this version is inactive now
			riimi.setVersion(retList.get(0).getVersion() + 1);
			riimi.setUpdated(System.currentTimeMillis());
			riimi.setState("active");  // new one is now active
			sess.save(riimi);
			tosave = riimi;  // now we're saving riimi as active
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

		
		// And return what we stored
		try {
			return buildResponse(Status.OK,tosave.toJSON());
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0016",LogCriticality.error);
		}
	}
}

