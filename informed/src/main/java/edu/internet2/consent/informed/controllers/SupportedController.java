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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
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
import edu.internet2.consent.informed.model.SupportedIIType;
import edu.internet2.consent.informed.model.SupportedLanguage;
import edu.internet2.consent.informed.model.SupportedRHType;
import edu.internet2.consent.informed.model.SupportedRPType;
import edu.internet2.consent.informed.model.SupportedUserType;
import edu.internet2.consent.informed.util.InformedUtility;

@Path("/supported")
public class SupportedController {
	
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
	@Path("/iitypes/")
	public Response optionsIitypes(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	
	@OPTIONS
	@Path("/iitypes/{rhtype}/{rhid}")
	public Response optionsIitypes2(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	
	@OPTIONS
	@Path("/iitypes/{rhtype}/{rhid}/{itype}")
	public Response optionsIitypes3(@Context HttpServletRequest request,@Context HttpHeaders headers)
	{
			return buildResponse(Status.OK,"");
	}
	
	@OPTIONS
	@Path("/languages/")
	public Response optionsLanguages(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	@OPTIONS
	@Path("/languages/{lang}")
	public Response optionsLanguages2(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	
	@OPTIONS
	@Path("/utypes/")
	public Response optionsUsertypes(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	@OPTIONS
	@Path("/utypes/{utype}")
	public Response optionsUsertypes2(@Context HttpServletRequest request, @Context HttpHeaders headers) 
	{
		return buildResponse(Status.OK,"");
	}
	
	
	
	@OPTIONS
	@Path("/rhtypes/")
	public Response optionsRhtypes(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	@OPTIONS
	@Path("/rhtypes/{type}")
	public Response optionsRhtypes2(@Context HttpServletRequest request,@Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	@OPTIONS
	@Path("/rptypes/")
	public Response optionsRptypes(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	@OPTIONS
	@Path("/rptypes/{rhtype}/{rhid}/")
	public Response optionsRptypes2(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	@OPTIONS
	@Path("/rptypes/{rhtype}/{rhid}/{type}")
	public Response optionsRptypes3(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	
	// Get list of supported user types
	@GET
	@Path("/utypes/")
	public Response getSupportedUtypes(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity)
	{
		// init
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getSupportedUtypes", request,  headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR004", LogCriticality.error);
		}
		
		// Hibernate sesssion
		
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500,  "ERR0018", LogCriticality.error);
		}
		
		// Get the entire list
		
		Query<SupportedUserType> retQuery = sess.createQuery("from SupportedUserType",SupportedUserType.class);
		List<SupportedUserType> utl = retQuery.list();
		
		if (utl == null || utl.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				ObjectMapper mapper = new ObjectMapper();
				return buildResponse(Status.OK,mapper.writeValueAsString(utl));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	
	@GET
	@Path("/utypes/{utype}")
	public Response getSingularSupportedUserType(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("utype") String utype)
	{
		// init
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getSupportedUtypes", request,  headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR004", LogCriticality.error);
		}
		
		// Hibernate sesssion
		
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500,  "ERR0018", LogCriticality.error);
		}
		
		// Get the entire list
		
		Query<SupportedUserType> retQuery = sess.createQuery("from SupportedUserType where utype = :utype",SupportedUserType.class);
		retQuery.setParameter("utype", utype);
		
		List<SupportedUserType> utl = retQuery.list();
		
		if (utl == null || utl.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				ObjectMapper mapper = new ObjectMapper();
				return buildResponse(Status.OK,mapper.writeValueAsString(utl.get(0)));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	
	// Matching POST
	@POST
	@Path("/utypes/")
	public Response postSupportedUserTypes(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity)
	{
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("postSupportedUserTypes", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}

		// parse out the input into an instance
		ObjectMapper om = new ObjectMapper();
		SupportedUserType sl = null;
		try {
			sl = om.readValue(entity,SupportedUserType.class);
		} catch (JsonParseException e) {
			return InformedUtility.locError(400, "ERR0005",LogCriticality.info);
		} catch (JsonMappingException e) {
			return InformedUtility.locError(400, "ERR0006",LogCriticality.info);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0007",LogCriticality.info);
		}
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}
		
		Query<SupportedUserType> checkQuery = sess.createQuery("from SupportedUserType",SupportedUserType.class);
		List<SupportedUserType> als = checkQuery.list();
		
		if (als.contains(sl)) {
			return InformedUtility.locError(409, "ERR0067",LogCriticality.info);
		}
	
		// Create anew
		Transaction tx = sess.beginTransaction();
		
		try {
			sess.save(sl);
			tx.commit();
			sess.close();
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException("Transaction rollback: ",e);
		} finally {
			if (sess.isOpen()) {
				sess.close();
			}
		}
		
		// And return
		try {
			return buildResponse(Status.OK,sl.toJSON());
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0016",LogCriticality.error);
		}
	}
	
	// Matching PUT
	@PUT
	@Path("/utypes/{utype}") 
	public Response putSupportedUserTypes(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("utype") String utype) 
	{
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("putSupportedUserTypes", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}

		// parse out the input into an instance
		ObjectMapper om = new ObjectMapper();
		SupportedUserType sl = null;
		try {
			sl = om.readValue(entity,SupportedUserType.class);
		} catch (JsonParseException e) {
			return InformedUtility.locError(400, "ERR0005",LogCriticality.info);
		} catch (JsonMappingException e) {
			return InformedUtility.locError(400, "ERR0006",LogCriticality.info);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0007",LogCriticality.error);
		}
		
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}
		
		// Create transaction
		Transaction tx = sess.beginTransaction();
		
		Query<SupportedUserType> getQuery = sess.createQuery("from SupportedUserType where utype = :utype",SupportedUserType.class);
		if (getQuery != null) {
			getQuery.setParameter("utype", utype);
		}
		
		List<SupportedUserType> als = getQuery.list();
		
		try {
			if (! als.contains(sl)) {
			// 	return InformedUtility.locError(404, "ERR0068");
			// 	In this case, a PUT will result in POST behavior of the object does not already exist
			// 	Save the input object as a new object
				sess.save(sl);
				tx.commit();
				sess.close();
			} else {
			// 	Update the object
				als.get(0).setUtype(sl.getUtype());
				als.get(0).setDescription(sl.getDescription());
				tx.commit();
				sess.close();  // there is no versioning here
			}
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException("Transaction rollback",e);
		} finally {
			if (sess.isOpen())
				sess.close();
		}
			
		// And return
		try {
			return buildResponse(Status.OK,sl.toJSON());
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0016",LogCriticality.error);
		}
	}
	
	// Get for list of supported languages
	@GET
	@Path("/languages/")
	public Response getSupportedLanguages(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity)
	{
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getSupportedLanguages", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}

		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}

		// Query here is for all -- there is no "active" versus "inactive" here.
		
		Query<SupportedLanguage> retQuery = sess.createQuery("from SupportedLanguage",SupportedLanguage.class);
		
		List<SupportedLanguage> sll = retQuery.list();
		
		if (sll == null || sll.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				ObjectMapper mapper = new ObjectMapper();
				return buildResponse(Status.OK,mapper.writeValueAsString(sll));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	
	// Get for single supported language
	@GET
	@Path("/languages/{lang}")
	public Response getSingularSupportedLanguage(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity,@PathParam("lang") String lang)
	{
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getSupportedLanguages", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}

		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}

		// Query here is for all -- there is no "active" versus "inactive" here.
		
		Query<SupportedLanguage> retQuery = sess.createQuery("from SupportedLanguage where lang = :lang",SupportedLanguage.class);
		
		retQuery.setParameter("lang", lang);
		
		List<SupportedLanguage> sll = retQuery.list();
		
		if (sll == null || sll.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				ObjectMapper mapper = new ObjectMapper();
				return buildResponse(Status.OK,mapper.writeValueAsString(sll.get(0)));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	
	// POST for the same thing
	// Given an object, return a 409 if the object already exists or a 200 and create the object if it does not.
	//
	@POST
	@Path("/languages/")
	public Response addSupportedLanguage(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getSupportedLanguages", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}

		// parse out the input into an instance
		ObjectMapper om = new ObjectMapper();
		SupportedLanguage sl = null;
		try {
			sl = om.readValue(entity,SupportedLanguage.class);
		} catch (JsonParseException e) {
			return InformedUtility.locError(400, "ERR0005",LogCriticality.info);
		} catch (JsonMappingException e) {
			return InformedUtility.locError(400, "ERR0006",LogCriticality.info);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0007",LogCriticality.info);
		}
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}
		
		Query<SupportedLanguage> checkQuery = sess.createQuery("from SupportedLanguage",SupportedLanguage.class);
		List<SupportedLanguage> als = checkQuery.list();
		
		if (als.contains(sl)) {
			return InformedUtility.locError(409, "ERR0067",LogCriticality.info);
		}
	
		// Create anew
		Transaction tx = sess.beginTransaction();
		
		try {
			sess.save(sl);
			tx.commit();
			sess.close();
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException("Transaction rollback: ",e);
		} finally {
			if (sess.isOpen()) {
				sess.close();
			}
		}
		
		// And return
		try {
			return buildResponse(Status.OK,sl.toJSON());
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0016",LogCriticality.error);
		}
	}
	
	// PUT for the same thing
	// Given an object, update it if the object already exists or create the object if it does not.
	//
	@PUT
	@Path("/languages/{lang}")
	public Response updateSupportedLanguage(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("lang") String lang) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getSupportedLanguages", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}

		// parse out the input into an instance
		ObjectMapper om = new ObjectMapper();
		SupportedLanguage sl = null;
		try {
			sl = om.readValue(entity,SupportedLanguage.class);
		} catch (JsonParseException e) {
			return InformedUtility.locError(400, "ERR0005",LogCriticality.info);
		} catch (JsonMappingException e) {
			return InformedUtility.locError(400, "ERR0006",LogCriticality.info);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0007",LogCriticality.error);
		}
		
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}
		
		// Create transaction
		Transaction tx = sess.beginTransaction();
		
		Query<SupportedLanguage> getQuery = sess.createQuery("from SupportedLanguage where lang = :lang",SupportedLanguage.class);
		if (getQuery != null) {
			getQuery.setParameter("lang", lang);
		}
		
		List<SupportedLanguage> als = getQuery.list();
		
		try {
			if (! als.contains(sl)) {
			// 	return InformedUtility.locError(404, "ERR0068");
			// 	In this case, a PUT will result in POST behavior of the object does not already exist
			// 	Save the input object as a new object
				sess.save(sl);
				tx.commit();
				sess.close();
			} else {
			// 	Update the object
				als.get(0).setLang(sl.getLang());
				als.get(0).setDisplayname(sl.getDisplayname());
				tx.commit();
				sess.close();  // there is no versioning here
			}
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException("Transaction rollback",e);
		} finally {
			if (sess.isOpen())
				sess.close();
		}
			
		// And return
		try {
			return buildResponse(Status.OK,sl.toJSON());
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0016",LogCriticality.error);
		}
	}
	
	
	//RPTypes
	
	// Get for list of supported RPTypes (all)
	@GET
	@Path("/rptypes/")
	public Response getSupportedRPTypes(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity)
	{
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getSupportedRPTypes", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}

		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}

		// Query here is for all -- there is no "active" versus "inactive" here.
		
		Query<SupportedRPType> retQuery = sess.createQuery("from SupportedRPType",SupportedRPType.class);
		
		List<SupportedRPType> sll = retQuery.list();
		
		if (sll == null || sll.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			ArrayList<SupportedRPType> retval = new ArrayList<SupportedRPType>();
			ArrayList<String> added = new ArrayList<String>();
			for (SupportedRPType l : sll) {
				if (! added.contains(l.getRptype())) {
					retval.add(l);
					added.add(l.getRptype());
				}
			}
			try {
				ObjectMapper mapper = new ObjectMapper();
				return buildResponse(Status.OK,mapper.writeValueAsString(retval));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	
	// GET for specific RH's II Types
	@GET
	@Path("/rptypes/{rhtype}/{rhid}/")
	public Response getSupportedRPTypesForRH(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity,@PathParam("rhtype") String rhtype,@PathParam("rhid") String rhidin)
	{
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getSupportedRPTypes", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}
		
		// Convert
		String rhid = InformedUtility.idUnEscape(rhidin);

		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}

		// Query here is for all -- there is no "active" versus "inactive" here.
		
		Query<SupportedRPType> retQuery = sess.createQuery("from SupportedRPType where rhtype = :rhtype and rhid = :rhid",SupportedRPType.class);
		
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhid", rhid);
		
		List<SupportedRPType> sll = retQuery.list();
		
		if (sll == null || sll.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {

			try {
				ObjectMapper mapper = new ObjectMapper();
				return buildResponse(Status.OK,mapper.writeValueAsString(sll));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	// POST for the same thing
	// Given an object, return a 409 if the object already exists or a 200 and create the object if it does not.
	//
	@POST
	@Path("/rptypes/{rhtype}/{rhid}/")
	public Response addSupportedRPType(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhid") String rhidin) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("addSupportedRPType", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}
		
		// parse out the input into an instance
		ObjectMapper om = new ObjectMapper();
		SupportedRPType sl = null;
		try {
			sl = om.readValue(entity,SupportedRPType.class);
		} catch (JsonParseException e) {
			return InformedUtility.locError(400, "ERR0005",LogCriticality.info);
		} catch (JsonMappingException e) {
			return InformedUtility.locError(400, "ERR0006",LogCriticality.info);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0007",LogCriticality.info);
		}
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}
		
		Query<SupportedRPType> checkQuery = sess.createQuery("from SupportedRPType",SupportedRPType.class);
		List<SupportedRPType> als = checkQuery.list();
		
		if (als.contains(sl)) {
			return InformedUtility.locError(409, "ERR0067", LogCriticality.info);
		}
	
		// Create anew
		Transaction tx = sess.beginTransaction();
		
		try {
			sess.save(sl);
			tx.commit();
			sess.close();
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException("Transaction rollback: ",e);
		} finally {
			if (sess.isOpen()) {
				sess.close();
			}
		}
		
		// And return
		try {
			return buildResponse(Status.OK,sl.toJSON());
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0016",LogCriticality.error);
		}
	}
	
	// GET for individual supported rptype
	@GET
	@Path("/rptypes/{rhtype}/{rhid}/{rptype}/")
	public Response getSingleSupportedRPType(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype,@PathParam("rhid") String rhidin,@PathParam("rptype") String rptype) 
	{
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getSupportedRPTypes", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}
		
		// Convert
		String rhid = InformedUtility.idUnEscape(rhidin);

		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}

		// Query here is for all -- there is no "active" versus "inactive" here.
		
		Query<SupportedRPType> retQuery = sess.createQuery("from SupportedRPType where rhtype = :rhtype and rhid = :rhid and rptype = :rptype",SupportedRPType.class);
		
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhid", rhid);
		retQuery.setParameter("rptype", rptype);
		
		List<SupportedRPType> sll = retQuery.list();
		
		if (sll == null || sll.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {

			try {
				ObjectMapper mapper = new ObjectMapper();
				return buildResponse(Status.OK,mapper.writeValueAsString(sll.get(0)));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	
	// PUT for the same thing
	// Given an object, update it if the object already exists or create the object if it does not.
	//
	@PUT
	@Path("/rptypes/{rhtype}/{rhid}/{rptype}/")
	public Response updateSupportedRPType(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype,@PathParam("rhid") String rhidin,@PathParam("rptype") String rptype) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("putSupportedRPType", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}
		
		// Convert
		String rhid = InformedUtility.idUnEscape(rhidin);

		// parse out the input into an instance
		ObjectMapper om = new ObjectMapper();
		SupportedRPType sl = null;
		try {
			sl = om.readValue(entity,SupportedRPType.class);
		} catch (JsonParseException e) {
			return InformedUtility.locError(400, "ERR0005",LogCriticality.info);
		} catch (JsonMappingException e) {
			return InformedUtility.locError(400, "ERR0006",LogCriticality.info);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0007",LogCriticality.error);
		}
		
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}
		
		// Create transaction
		Transaction tx = sess.beginTransaction();
		
		Query<SupportedRPType> getQuery = sess.createQuery("from SupportedRPType where rhtype = :rhtype and rhid = :rhid and rptype = :rptype",SupportedRPType.class);
		if (getQuery != null) {
			getQuery.setParameter("rhtype",rhtype);
			getQuery.setParameter("rhid", rhid);
			getQuery.setParameter("rptype", rptype);
		}
		
		List<SupportedRPType> als = getQuery.list();
		
		try {
			if (! als.contains(sl)) {
			// 	In this case, a PUT will result in POST behavior of the object does not already exist
			// 	Save the input object as a new object
				sess.save(sl);
				tx.commit();
				sess.close();
			} else {
			// 	Update the object
				als.get(0).setRhtype(sl.getRhtype());
				als.get(0).setRhid(sl.getRhid());
				als.get(0).setRptype(sl.getRptype());
				als.get(0).setDescription(sl.getDescription());
				tx.commit();
				sess.close();  // there is no versioning here
			}
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException("Transaction rollback",e);
		} finally {
			if (sess.isOpen())
				sess.close();
		}
			
		// And return
		try {
			return buildResponse(Status.OK,sl.toJSON());
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0016",LogCriticality.error);
		}
	}
	
	//IITypes
	
	// Get for list of supported IITypes
	@GET
	@Path("/iitypes/")
	public Response getSupportedIITypes(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity)
	{
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getSupportedIITypes", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}

		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}

		// Query here is for all -- there is no "active" versus "inactive" here.
		
		Query<SupportedIIType> retQuery = sess.createQuery("from SupportedIIType",SupportedIIType.class);
		
		List<SupportedIIType> sll = retQuery.list();
		
		if (sll == null || sll.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			ArrayList<SupportedIIType> retval = new ArrayList<SupportedIIType>();
			ArrayList<String> added = new ArrayList<String>();
			for (SupportedIIType l : sll) {
				if (! added.contains(l.getIitype())) {
					retval.add(l);
					added.add(l.getIitype());
				}
			}
			try {
				ObjectMapper mapper = new ObjectMapper();
				return buildResponse(Status.OK,mapper.writeValueAsString(retval));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016", LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	
	// GET for specific RH's II Types
	@GET
	@Path("/iitypes/{rhtype}/{rhid}/")
	public Response getSupportedIITypesForRH(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity,@PathParam("rhtype") String rhtype,@PathParam("rhid") String rhidin)
	{
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getSupportedIITypes", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}
		
		// Convert
		String rhid = InformedUtility.idUnEscape(rhidin);

		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018", LogCriticality.error);
		}

		// Query here is for all -- there is no "active" versus "inactive" here.
		
		Query<SupportedIIType> retQuery = sess.createQuery("from SupportedIIType where rhtype = :rhtype and rhid = :rhid",SupportedIIType.class);
		
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhid", rhid);
		
		List<SupportedIIType> sll = retQuery.list();
		
		if (sll == null || sll.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {

			try {
				ObjectMapper mapper = new ObjectMapper();
				return buildResponse(Status.OK,mapper.writeValueAsString(sll));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016", LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	// POST for the same thing
	// Given an object, return a 409 if the object already exists or a 200 and create the object if it does not.
	//
	@POST
	@Path("/iitypes/{rhtype}/{rhid}/")
	public Response addSupportedIIType(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype, @PathParam("rhid") String rhidin) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("addSupportedIIType", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}
		

		// parse out the input into an instance
		ObjectMapper om = new ObjectMapper();
		SupportedIIType sl = null;
		try {
			sl = om.readValue(entity,SupportedIIType.class);
		} catch (JsonParseException e) {
			return InformedUtility.locError(400, "ERR0005",LogCriticality.info);
		} catch (JsonMappingException e) {
			return InformedUtility.locError(400, "ERR0006",LogCriticality.info);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0007",LogCriticality.info);
		}
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}
		
		Query<SupportedIIType> checkQuery = sess.createQuery("from SupportedIIType",SupportedIIType.class);
		List<SupportedIIType> als = checkQuery.list();
		
		if (als.contains(sl)) {
			return InformedUtility.locError(409, "ERR0067",LogCriticality.info);
		}
	
		// Create anew
		Transaction tx = sess.beginTransaction();
		
		try {
			sess.save(sl);
			tx.commit();
			sess.close();
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException("Transaction rollback: ",e);
		} finally {
			if (sess.isOpen()) {
				sess.close();
			}
		}
		
		// And return
		try {
			return buildResponse(Status.OK,sl.toJSON());
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0016",LogCriticality.error);
		}
	}
	
	// GET for individual supported iitype
	@GET
	@Path("/iitypes/{rhtype}/{rhid}/{iitype}/")
	public Response getSingleSupportedIIType(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype,@PathParam("rhid") String rhidin,@PathParam("iitype") String iitype) 
	{
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getSupportedIITypes", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}
		
		// Convert
		String rhid = InformedUtility.idUnEscape(rhidin);

		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}

		// Query here is for all -- there is no "active" versus "inactive" here.
		
		Query<SupportedIIType> retQuery = sess.createQuery("from SupportedIIType where rhtype = :rhtype and rhid = :rhid and iitype = :iitype",SupportedIIType.class);
		
		retQuery.setParameter("rhtype", rhtype);
		retQuery.setParameter("rhid", rhid);
		retQuery.setParameter("iitype", iitype);
		
		List<SupportedIIType> sll = retQuery.list();
		
		if (sll == null || sll.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {

			try {
				ObjectMapper mapper = new ObjectMapper();
				return buildResponse(Status.OK,mapper.writeValueAsString(sll.get(0)));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	
	// PUT for the same thing
	// Given an object, update it if the object already exists or create the object if it does not.
	//
	@PUT
	@Path("/iitypes/{rhtype}/{rhid}/{iitype}/")
	public Response updateSupportedIIType(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype,@PathParam("rhid") String rhidin,@PathParam("iitype") String iitype) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("putSupportedIIType", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}
		
		// Convert
		String rhid = InformedUtility.idUnEscape(rhidin);

		// parse out the input into an instance
		ObjectMapper om = new ObjectMapper();
		SupportedIIType sl = null;
		try {
			sl = om.readValue(entity,SupportedIIType.class);
		} catch (JsonParseException e) {
			return InformedUtility.locError(400, "ERR0005",LogCriticality.info);
		} catch (JsonMappingException e) {
			return InformedUtility.locError(400, "ERR0006",LogCriticality.info);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0007",LogCriticality.error);
		}
		
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}
		
		// Create transaction
		Transaction tx = sess.beginTransaction();
		
		Query<SupportedIIType> getQuery = sess.createQuery("from SupportedIIType where rhtype = :rhtype and rhid = :rhid and iitype = :iitype",SupportedIIType.class);
		if (getQuery != null) {
			getQuery.setParameter("rhtype",rhtype);
			getQuery.setParameter("rhid", rhid);
			getQuery.setParameter("iitype", iitype);
		}
		
		List<SupportedIIType> als = getQuery.list();
		
		try {
			if (! als.contains(sl)) {
			// 	In this case, a PUT will result in POST behavior of the object does not already exist
			// 	Save the input object as a new object
				sess.save(sl);
				tx.commit();
				sess.close();
			} else {
			// 	Update the object
				als.get(0).setRhtype(sl.getRhtype());
				als.get(0).setRhid(sl.getRhid());
				als.get(0).setIitype(sl.getIitype());
				als.get(0).setDescription(sl.getDescription());
				tx.commit();
				sess.close();  // there is no versioning here
			}
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException("Transaction rollback",e);
		} finally {
			if (sess.isOpen())
				sess.close();
		}
			
		// And return
		try {
			return buildResponse(Status.OK,sl.toJSON());
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0016", LogCriticality.error);
		}
	}
	
	// Get for list of supported rhtypes
	@GET
	@Path("/rhtypes/")
	public Response getSupportedRHTypes(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity)
	{
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getSupportedRHTypes", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004", LogCriticality.error);
		}

		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}

		// Query here is for all -- there is no "active" versus "inactive" here.
		
		Query<SupportedRHType> retQuery = sess.createQuery("from SupportedRHType",SupportedRHType.class);
		
		List<SupportedRHType> sll = retQuery.list();
		
		if (sll == null || sll.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				ObjectMapper mapper = new ObjectMapper();
				return buildResponse(Status.OK,mapper.writeValueAsString(sll));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	
	// Get for specific supported rhtype
	@GET
	@Path("/rhtypes/{rhtype}/")
	public Response getSingularSupportedRHType(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype)
	{
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getSupportedRHTypes", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}

		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}

		// Query here is for all -- there is no "active" versus "inactive" here.
		
		Query<SupportedRHType> retQuery = sess.createQuery("from SupportedRHType where rhtype = :rhtype",SupportedRHType.class);
		
		retQuery.setParameter("rhtype", rhtype);
		
		List<SupportedRHType> sll = retQuery.list();
		
		if (sll == null || sll.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				ObjectMapper mapper = new ObjectMapper();
				return buildResponse(Status.OK,mapper.writeValueAsString(sll.get(0)));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	
	// POST for the same thing
	// Given an object, return a 409 if the object already exists or a 200 and create the object if it does not.
	//
	@POST
	@Path("/rhtypes/")
	public Response addSupportedRHType(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("addSupportedRHType", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}

		// parse out the input into an instance
		ObjectMapper om = new ObjectMapper();
		SupportedRHType sl = null;
		try {
			sl = om.readValue(entity,SupportedRHType.class);
		} catch (JsonParseException e) {
			return InformedUtility.locError(400, "ERR0005",LogCriticality.info);
		} catch (JsonMappingException e) {
			return InformedUtility.locError(400, "ERR0006",LogCriticality.info);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0007",LogCriticality.info);
		}
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}
		
		Query<SupportedRHType> checkQuery = sess.createQuery("from SupportedRHType",SupportedRHType.class);
		List<SupportedRHType> als = checkQuery.list();
		
		if (als.contains(sl)) {
			return InformedUtility.locError(409, "ERR0067",LogCriticality.info);
		}
	
		// Create anew
		Transaction tx = sess.beginTransaction();
		
		try {
			sess.save(sl);
			tx.commit();
			sess.close();
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException("Transaction rollback: ",e);
		} finally {
			if (sess.isOpen()) {
				sess.close();
			}
		}
		
		// And return
		try {
			return buildResponse(Status.OK,sl.toJSON());
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0016", LogCriticality.error);
		}
	}
	
	// PUT for the same thing
	// Given an object, update it if the object already exists or create the object if it does not.
	//
	@PUT
	@Path("/rhtypes/{rhtype}")
	public Response updateSupportedRHType(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rhtype") String rhtype) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("putSupportedRHType", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}

		// parse out the input into an instance
		ObjectMapper om = new ObjectMapper();
		SupportedRHType sl = null;
		try {
			sl = om.readValue(entity,SupportedRHType.class);
		} catch (JsonParseException e) {
			return InformedUtility.locError(400, "ERR0005",LogCriticality.info);
		} catch (JsonMappingException e) {
			return InformedUtility.locError(400, "ERR0006",LogCriticality.info);
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0007",LogCriticality.error);
		}
		
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}
		
		// Create transaction
		Transaction tx = sess.beginTransaction();
		
		Query<SupportedRHType> getQuery = sess.createQuery("from SupportedRHType where rhtype = :rhtype",SupportedRHType.class);
		if (getQuery != null) {
			getQuery.setParameter("rhtype", rhtype);
		}
		
		List<SupportedRHType> als = getQuery.list();
		
		try {
			if (! als.contains(sl)) {
			// 	return InformedUtility.locError(404, "ERR0068");
			// 	In this case, a PUT will result in POST behavior of the object does not already exist
			// 	Save the input object as a new object
				sess.save(sl);
				tx.commit();
				sess.close();
			} else {
			// 	Update the object
				als.get(0).setRhtype(sl.getRhtype());
				als.get(0).setDescription(sl.getDescription());
				tx.commit();
				sess.close();  // there is no versioning here
			}
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException("Transaction rollback",e);
		} finally {
			if (sess.isOpen())
				sess.close();
		}
			
		// And return
		try {
			return buildResponse(Status.OK,sl.toJSON());
		} catch (Exception e) {
			return InformedUtility.locError(500, "ERR0016",LogCriticality.error);
		}
	}
}
