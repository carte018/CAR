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
import edu.internet2.consent.informed.model.ReturnedInfoTypeList;
import edu.internet2.consent.informed.model.ReturnedRHInfoItemList;
import edu.internet2.consent.informed.model.ReturnedRHMetaInformation;
import edu.internet2.consent.informed.model.ReturnedRHRPList;
import edu.internet2.consent.informed.util.InformedUtility;
import edu.internet2.consent.informed.util.OMSingleton;

// Controller for /rhic resources
// These are resources that are unique to one or more resource holders, irrespective
// of relying party or user.


@Path("/rhic")
public class RHICController {

	@SuppressWarnings("unused")
	private String caller="";
	@SuppressWarnings("unused")
	private final Log LOG = LogFactory.getLog(RHICController.class);
	
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
	@Path("/infotypes")
	public Response optionsInfotypes(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	@OPTIONS
	@Path("/infotypes/{rhtype}")
	public Response optionsInfotypeRhtype(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	@OPTIONS
	@Path("/infotypes/{rhtype}/{rhvalue}")
	public Response optionsInfotypeRH(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	
	
	@OPTIONS
	@Path("metainformation/{rhtype}/{rhvalue}")
	public Response optionsMetaInformationRH(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	
	// Resources related to the list of info types available from a given RH
	// For a SAML IDP, for example, this may typically be a one-long list containing a type descriptor
	// for an "attribute".
	//
	// Support for GET, PUT, and DELETE (no POST)
	
	// DELETE simply removes an entry -- more properly, set the state so that the entry does not exist
	
	@DELETE
	@Path("/infotypes/{rhtype}/{rhvalue}")
	public Response deleteInfotypes(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein) {
		// Init
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("deleteInfotypes", request, headers, null);
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
		
		// Perform the delete operation in a transaction
		Transaction tx = sess.beginTransaction();
		try {
		Query<ReturnedInfoTypeList> delQuery = sess.createQuery("from ReturnedInfoTypeList where rhtype = :rhtype and rhvalue = :rhvalue",ReturnedInfoTypeList.class);
		delQuery.setParameter("rhtype", rhtype);
		delQuery.setParameter("rhvalue", rhvalue);
		
		// delQuery.executeUpdate();  // ignore failures -- this always succeeds, even if it fails
		List<ReturnedInfoTypeList> retlist = delQuery.list();
		if (! retlist.isEmpty()) {
			sess.delete(retlist.get(0)); // Not versioned
			tx.commit();
			sess.close();
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
	// Get all the entries
	@GET
	@Path("/infotypes/")
	public Response getAllInfotypes(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		
		// Init
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getInfotypes", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}
		
		// Get the entries (if there are any)
		Query<ReturnedInfoTypeList> retQuery = sess.createQuery("from ReturnedInfoTypeList",ReturnedInfoTypeList.class);
		
		List<ReturnedInfoTypeList> retlist = retQuery.list();
		
		if (retlist.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				//ObjectMapper mapper = new ObjectMapper();
				ObjectMapper mapper = OMSingleton.getInstance().getOm();
				return buildResponse(Status.OK,mapper.writeValueAsString(retlist));
			} catch (Exception e) {
				return InformedUtility.locError(500, "ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
				 
		}
	}
	// Get all entries with a given rhtype
	@GET
	@Path("/infotypes/{rhtype}")
	public Response getInfotypesByRHType(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype) {
		
		// Init
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getInfotypes", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004", LogCriticality.error);
		}
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018", LogCriticality.error);
		}
		
		// Get the entries (if there are any)
		Query<ReturnedInfoTypeList> retQuery = sess.createQuery("from ReturnedInfoTypeList where rhtype = :rhtype",ReturnedInfoTypeList.class);
		retQuery.setParameter("rhtype", rhtype);
		
		List<ReturnedInfoTypeList> retlist = retQuery.list();
		
		// if none are returned, we 404, otherwise, we return the list
		if (retlist.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065", LogCriticality.info);
		} else {
			try {
				//ObjectMapper mapper = new ObjectMapper();
				ObjectMapper mapper = OMSingleton.getInstance().getOm();
				return buildResponse(Status.OK,mapper.writeValueAsString(retlist));
			} catch (Exception e) {
				return InformedUtility.locError(500, "ERR0016", LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	// Get an entry by full name
	@GET 
	@Path("/infotypes/{rhtype}/{rhvalue}")
	public Response getInfotypesByRH(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein) {
		
		// Init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getInfotypes", request, headers, null);
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
		
		// Retrieve the requested object if it exists
		Query<ReturnedInfoTypeList> retQuery = sess.createQuery("from ReturnedInfoTypeList where rhtype = :rhtype and rhvalue = :rhvalue",ReturnedInfoTypeList.class);
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhvalue", rhvalue);
		
		List<ReturnedInfoTypeList> retlist = retQuery.list();
		
		// Two possibilities:  The object exists or it doesn't.  404 if it doesn't, else return the object
		
		if (retlist.isEmpty()) {
			// return a 404
			sess.close();
			return InformedUtility.locError(404, "ERR0065", LogCriticality.info);
		} else {
			// return the returned object (we assume we cannot have duplicate returns
			try {
				return buildResponse(Status.OK,retlist.get(0).toJSON());
			} catch (Exception e) {
				return InformedUtility.locError(500, "ERR0016", LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	
	// Create/replace an entry
	@PUT
	@Path("/infotypes/{rhtype}/{rhvalue}")
	public Response putInfotypes(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein) {

		// Run the init process
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("putInfotypes", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004", LogCriticality.error);
		}
		
		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);
		
		// Deserialize the input
		
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		
		ReturnedInfoTypeList ritl = null;
		
		try {
			ritl = mapper.readValue(entity, ReturnedInfoTypeList.class);
		} catch (JsonParseException e) {
			return InformedUtility.locError(400, "ERR0005", LogCriticality.info);
		} catch (JsonMappingException e) {
			return InformedUtility.locError(400, "ERR0006", LogCriticality.info);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0007", LogCriticality.error);
		}
		
		// now we are authorized and we have a valid object
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}
		
		// Start a transaction
		Transaction tx = sess.beginTransaction();
		
		// See if we can get an object out of the store already
		// If we can, we copy the input object to it and re-store it
		// If we cannot, we simply store the input object directly.
		// PUT is create or replace.
		
		Query<ReturnedInfoTypeList> retQuery = sess.createQuery("from ReturnedInfoTypeList where rhtype = :rhtype and rhvalue = :rhvalue",ReturnedInfoTypeList.class);
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhvalue", rhvalue);
		
		List<ReturnedInfoTypeList> retlist = retQuery.list();
		
		ReturnedInfoTypeList tosave = null;
		try {
		if (retlist.isEmpty()) {
			// No object in the store already, so just save input
			sess.save(ritl);
			tx.commit();
			sess.close();
			tosave = ritl;
		} else {
			// We have one already -- replace it
			retlist.get(0).setInfotypes(ritl.getInfotypes());
			tosave = retlist.get(0);
			// save unnecessary
			tx.commit();
			sess.close();
		} 
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException("Transaction rollback",e);
		} finally {
		// Either way, complete the transaction
			if (sess != null && sess.isOpen())
				sess.close();
		}
		// And return the input as the response
		try {
			return buildResponse(Status.OK,tosave.toJSON());
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0016",LogCriticality.error);
		}
	}
	
	// Resource methods related to meta-information for resource holders.  We support multiple GET endpoints
	// for individual and collected responses, PUT (for create/replace operations) and DELETE (for removal)
	
	@DELETE
	@Path("/metainformation/{rhtype}/{rhvalue}")
	public Response deleteMeatainformationRH(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("deleteMetainformation", request, headers, null);
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
		
		// Start a transaction
		
		Transaction tx = sess.beginTransaction();
		try {
		Query<ReturnedRHMetaInformation> getQuery = sess.createQuery("from ReturnedRHMetaInformation where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid and state = :state",ReturnedRHMetaInformation.class);
		getQuery.setParameter("rhtype", rhtype);
		getQuery.setParameter("rhid", rhvalue);
		getQuery.setParameter("state","active");  // only the active entry
		
		List<ReturnedRHMetaInformation> lri = getQuery.list();
		
		if (! lri.isEmpty()) {
			// sess.delete(lri.get(0));  // not anymore
			lri.get(0).setState("inactive");
			tx.commit();
			sess.close();
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
	@Path("/metainformation/")
	public Response getMetainformationAll(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getMetainformation", request,  headers,  null);
		} catch (Exception e) {
			return InformedUtility.locError(500,  "ERR0004", LogCriticality.error);
		}
		
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500,  "ERR0018",LogCriticality.error);
		}
		Query<ReturnedRHMetaInformation> retQuery = sess.createQuery("from ReturnedRHMetaInformation where state = :state",ReturnedRHMetaInformation.class);
		retQuery.setParameter("state", "active");  // only retrieve active entries
		
		List<ReturnedRHMetaInformation> lrhmi = retQuery.list();
		
		if (lrhmi.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				//ObjectMapper mapper = new ObjectMapper();
				ObjectMapper mapper = OMSingleton.getInstance().getOm();
				
				return buildResponse(Status.OK,mapper.writeValueAsString(lrhmi));
			} catch (Exception e) {
				return InformedUtility.locError(500, "ERR0016", LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	
	@GET
	@Path("/metainformation/{rhtype}/{rhvalue}")
	public Response getMetainformationRH(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getMetainformation", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}
		
		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018", LogCriticality.error);
		}
		
		Query<ReturnedRHMetaInformation> retQuery = sess.createQuery("from ReturnedRHMetaInformation where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid and state = :state",ReturnedRHMetaInformation.class);
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhid", rhvalue);
		retQuery.setParameter("state", "active");  // only retrieve active entries
		
		List<ReturnedRHMetaInformation> lrhmi = retQuery.list();
		
		if (lrhmi.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065", LogCriticality.info);
		} else {
			try {
				return buildResponse(Status.OK,lrhmi.get(0).toJSON());
			} catch (Exception e) {
				return InformedUtility.locError(500, "ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	@PUT
	@Path("/metainformation/{rhtype}/{rhvalue}")
	public Response putMetainformation(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("putMetainformation", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004", LogCriticality.error);
		}
		
		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);
		
		// Deserialize the input
		
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		
		ReturnedRHMetaInformation ritl = null;
		
		try {
			ritl = mapper.readValue(entity, ReturnedRHMetaInformation.class);
		} catch (JsonParseException e) {
			return InformedUtility.locError(400, "ERR0005", LogCriticality.info);
		} catch (JsonMappingException e) {
			return InformedUtility.locError(400, "ERR0006", LogCriticality.info);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0007", LogCriticality.error);
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
		Query<ReturnedRHMetaInformation> retQuery = sess.createQuery("from ReturnedRHMetaInformation where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid and state = :state",ReturnedRHMetaInformation.class);
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhid", rhvalue);
		retQuery.setParameter("state", "active");   // only if active
		
		List<ReturnedRHMetaInformation> retList = retQuery.list();
		
		ReturnedRHMetaInformation tosave = null;
		try {
		if (retList.isEmpty()) {
			// just store the object we have at input
			// Set the tourist information first, though
			ritl.setState("active");
			ritl.setVersion(1);
			ritl.setUpdated(System.currentTimeMillis());
			sess.save(ritl);
			tx.commit();
			sess.close();
			tosave = ritl;
		} else {
			// update retList[0] with ritl data and save it
			// or not -- now we're versioned
			retList.get(0).setState("inactive"); // current becomes inactive
			ritl.setVersion(retList.get(0).getVersion() + 1);
			ritl.setState("active");
			ritl.setUpdated(System.currentTimeMillis());

			tosave = ritl;
			sess.save(ritl);
			tx.commit();
			sess.close();
		}
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException("Transaction rollback",e);
		} finally {
		// Regardless, commit and close
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
	
	// RH RPList property handlers
	@DELETE
	@Path("/rplist/{rhtype}/{rhvalue}")
	public Response deleteRPListRH(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("deleteRPList", request, headers, null);
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
		
		// Get a transaction
		Transaction tx = sess.beginTransaction();
		
		try {
		Query<ReturnedRHRPList> getQuery = sess.createQuery("from ReturnedRHRPList where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid",ReturnedRHRPList.class);
		getQuery.setParameter("rhtype", rhtype);
		getQuery.setParameter("rhid", rhvalue);
		
		List<ReturnedRHRPList> rrpl = (List<ReturnedRHRPList>) getQuery.list();
		if (! rrpl.isEmpty()) {
			sess.delete(rrpl.get(0));  // not versioned
			tx.commit();
			sess.close();
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
	// Administrative bulk retrieval
	@GET
	@Path("/rplist/")
	public Response getAllRPLists(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getRPList", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}

		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}
		Query<ReturnedRHRPList> retQuery = sess.createQuery("from ReturnedRHRPList",ReturnedRHRPList.class);

		
		List<ReturnedRHRPList> rrpl = retQuery.list();
		
		if (rrpl.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				//ObjectMapper mapper = new ObjectMapper();
				ObjectMapper mapper = OMSingleton.getInstance().getOm();
				
				return buildResponse(Status.OK,mapper.writeValueAsString(rrpl));
			} catch (Exception e) {
				return InformedUtility.locError(500, "ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	@GET
	@Path("/rplist/{rhtype}/{rhvalue}")
	public Response getRPListRH(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getRPList", request, headers, null);
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
		Query<ReturnedRHRPList> retQuery = sess.createQuery("from ReturnedRHRPList where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid",ReturnedRHRPList.class);
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhid", rhvalue);
		
		List<ReturnedRHRPList> rrpl = retQuery.list();
		
		if (rrpl.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				return buildResponse(Status.OK,rrpl.get(0).toJSON());
			} catch (Exception e) {
				return InformedUtility.locError(500, "ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	@PUT
	@Path("/rplist/{rhtype}/{rhvalue}")
	public Response putRPListRH(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("putRPList", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}
		
		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);
		// Deserialize the input
		
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		
		ReturnedRHRPList rrpl = null;
		
		try {
			rrpl = mapper.readValue(entity, ReturnedRHRPList.class);
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
		Query<ReturnedRHRPList> retQuery = sess.createQuery("from ReturnedRHRPList where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid",ReturnedRHRPList.class);
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhid", rhvalue);
		
		List<ReturnedRHRPList> retList = retQuery.list();
		
		ReturnedRHRPList tosave = null;
		try {
		if (retList.isEmpty()) {
			// just store the object we have at input
			sess.save(rrpl);
			tx.commit();
			sess.close();
			tosave = rrpl;
		} else {
			// update retList[0] with ritl data and save it
			retList.get(0).setRplist(rrpl.getRplist());
			tosave = retList.get(0);
			tx.commit();
			sess.close();
		}
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException("Transaction rollback",e);
		} finally {
		// Regardless, commit and close
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
	
	// RH InfoItem list
	@DELETE
	@Path("/iilist/{rhtype}/{rhvalue}")
	public Response deleteIIListRH(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein) {
		
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("deleteIIList", request, headers, null);
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

		// Get a transaction
		Transaction tx = sess.beginTransaction();
		try {
		Query<ReturnedRHInfoItemList> getQuery = sess.createQuery("from ReturnedRHInfoItemList where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid",ReturnedRHInfoItemList.class);
		getQuery.setParameter("rhtype", rhtype);
		getQuery.setParameter("rhid", rhvalue);
		
		List<ReturnedRHInfoItemList> ret = getQuery.list();
		
		if (! ret.isEmpty()) {
			sess.delete(ret.get(0));
			tx.commit();
			sess.close();
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
		return buildResponse(Status.NO_CONTENT, "");
	}
	
	// Administrative bulk retrieval
	@GET
	@Path("/iilist/")
	public Response getAllIILists(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getIIList", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}

		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}
		
		Query<ReturnedRHInfoItemList> retQuery = sess.createQuery("from ReturnedRHInfoItemList",ReturnedRHInfoItemList.class);

		
		List<ReturnedRHInfoItemList> rhil = retQuery.list();
		
		if (rhil.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				//ObjectMapper mapper = new ObjectMapper();
				ObjectMapper mapper = OMSingleton.getInstance().getOm();
				
				return buildResponse(Status.OK,mapper.writeValueAsString(rhil));
			} catch (Exception e) {
				return InformedUtility.locError(500, "ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	
	@GET
	@Path("/iilist/{rhtype}/{rhvalue}")
	public Response getIIListRH(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein) {
		
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getIIList", request, headers, null);
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
		
		Query<ReturnedRHInfoItemList> retQuery = sess.createQuery("from ReturnedRHInfoItemList where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid",ReturnedRHInfoItemList.class);
		retQuery.setParameter("rhtype",rhtype);
		retQuery.setParameter("rhid",rhvalue);
		
		List<ReturnedRHInfoItemList> rhil = retQuery.list();
		
		if (rhil.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				return buildResponse(Status.OK,rhil.get(0).toJSON());
			} catch (Exception e) {
				return InformedUtility.locError(500, "ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	
	@PUT
	@Path("/iilist/{rhtype}/{rhvalue}")
	public Response putIIListRH(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhvalue") String rhvaluein) {
		
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("putIIList", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}

		// Unescape
		String rhvalue = InformedUtility.idUnEscape(rhvaluein);
		
		// Deserialize the input
		
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		
		ReturnedRHInfoItemList rril = null;
		
		try {
			rril = mapper.readValue(entity, ReturnedRHInfoItemList.class);
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
		Query<ReturnedRHInfoItemList> retQuery = sess.createQuery("from ReturnedRHInfoItemList where rhidentifier.rhtype = :rhtype and rhidentifier.rhid = :rhid",ReturnedRHInfoItemList.class);
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhid", rhvalue);
		
		List<ReturnedRHInfoItemList> retList = retQuery.list();
		
		ReturnedRHInfoItemList tosave = null;
		try {
		if (retList.isEmpty()) {
			// just store the object we have
			sess.save(rril);
			tx.commit();
			sess.close();
			tosave = rril;
		} else {
			// update retList[0] with ritl data and save it
			retList.get(0).setInfoitemlist(rril.getInfoitemlist());
			tosave = retList.get(0);
			tx.commit();
			sess.close();
		}
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException("Transaction rollback",e);
		} finally {
		// Regardless, commit and close
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
