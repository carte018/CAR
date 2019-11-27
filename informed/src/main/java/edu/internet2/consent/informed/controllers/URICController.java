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
import edu.internet2.consent.informed.model.ReturnedUserRPMetaInformation;
import edu.internet2.consent.informed.util.InformedUtility;
import edu.internet2.consent.informed.util.OMSingleton;

@Path("/uric")
public class URICController {

	@SuppressWarnings("unused")
	private String caller="";
	@SuppressWarnings("unused")
	private final Log LOG = LogFactory.getLog(URICController.class);
	
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
	@Path("/urmetainformation/{rptype}/{rpvalue}/{usertype}/{uservalue}")
	public Response optionsURMetaInformation(@Context HttpServletRequest request, @Context HttpHeaders headers)
	{
		return buildResponse(Status.OK,"");
	}
	
	@DELETE
	@Path("/urmetainformation/{rptype}/{rpvalue}/{usertype}/{uservalue}")
	public Response deleteURMetaInformation(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rptype") String rptype, @PathParam("rpvalue") String rpvaluein, @PathParam("usertype") String usertype, @PathParam("uservalue") String uservalue) {
		
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("deleteURMetaInformation", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}
		
		// Unescape
		String rpvalue = InformedUtility.idUnEscape(rpvaluein);

		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}

		// Get a transaction
		Transaction tx = sess.beginTransaction();
		try {
		Query<ReturnedUserRPMetaInformation> getQuery = sess.createQuery("from ReturnedUserRPMetaInformation where rpidentifier.rptype = :rptype and rpidentifier.rpid = :rpid and useridentifier.usertype = :usertype and useridentifier.userid = :userid",ReturnedUserRPMetaInformation.class);
		getQuery.setParameter("rptype", rptype);
		getQuery.setParameter("rpid", rpvalue);
		getQuery.setParameter("usertype", usertype);
		getQuery.setParameter("userid", uservalue);
		
		List<ReturnedUserRPMetaInformation> rurpmi = getQuery.list();
		
		if (! rurpmi.isEmpty()) {
			sess.delete(rurpmi.get(0));
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
		
	// Administratively retrieve all user metainformation
	@GET
	@Path("/urmetainformation/")
	public Response getAllURMetaInformation(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity) {
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getURMetaInformation", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}


		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}

		Query<ReturnedUserRPMetaInformation> retQuery = sess.createQuery("from ReturnedUserRPMetaInformation",ReturnedUserRPMetaInformation.class);

		
		List<ReturnedUserRPMetaInformation> rurpmi = retQuery.list();
		
		if (rurpmi.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				//ObjectMapper mapper = new ObjectMapper();
				ObjectMapper mapper = OMSingleton.getInstance().getOm();
				
				return buildResponse(Status.OK,mapper.writeValueAsString(rurpmi));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}

	@GET
	@Path("/urmetainformation/{rptype}/{rpvalue}/{usertype}/{uservalue}")
	public Response getURMetaInformation(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rptype") String rptype, @PathParam("rpvalue") String rpvaluein, @PathParam("usertype") String usertype, @PathParam("uservalue") String uservalue) {
		
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("getURMetaInformation", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}

		// Unescape
		String rpvalue = InformedUtility.idUnEscape(rpvaluein);
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}

		Query<ReturnedUserRPMetaInformation> retQuery = sess.createQuery("from ReturnedUserRPMetaInformation where rpidentifier.rptype = :rptype and rpidentifier.rpid = :rpid and useridentifier.usertype = :usertype and useridentifier.userid = :userid",ReturnedUserRPMetaInformation.class);
		retQuery.setParameter("rptype", rptype);
		retQuery.setParameter("rpid", rpvalue);
		retQuery.setParameter("usertype", usertype);
		retQuery.setParameter("userid",uservalue);
		
		List<ReturnedUserRPMetaInformation> rurpmi = retQuery.list();
		
		if (rurpmi.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				return buildResponse(Status.OK,rurpmi.get(0).toJSON());
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
	
	@PUT
	@Path("/urmetainformation/{rptype}/{rpvalue}/{usertype}/{uservalue}")
	public Response putURMetaInformation(@Context HttpServletRequest request, @Context HttpHeaders headers, String entity, @PathParam("rptype") String rptype, @PathParam("rpvalue") String rpvaluein, @PathParam("usertype") String usertype, @PathParam("uservalue") String uservalue) {
		
		// init first
		@SuppressWarnings("unused")
		InformedConfig config = null;
		try {
			config = InformedUtility.init("putURMetaInformation", request, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}
		
		//Unescape
		String rpvalue = InformedUtility.idUnEscape(rpvaluein);

		// Deserialize the input
		
		//ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper = OMSingleton.getInstance().getOm();
		
		ReturnedUserRPMetaInformation rurpmi = null;
		
		try {
			rurpmi = mapper.readValue(entity, ReturnedUserRPMetaInformation.class);
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
		Query<ReturnedUserRPMetaInformation> retQuery = sess.createQuery("from ReturnedUserRPMetaInformation where rpidentifier.rptype = :rptype and rpidentifier.rpid = :rpid and useridentifier.usertype = :usertype and useridentifier.userid = :userid",ReturnedUserRPMetaInformation.class);
		retQuery.setParameter("rptype", rptype);
		retQuery.setParameter("rpid", rpvalue);
		retQuery.setParameter("usertype", usertype);
		retQuery.setParameter("userid", uservalue);
		
		List<ReturnedUserRPMetaInformation> retList = retQuery.list();
		
		ReturnedUserRPMetaInformation tosave = null;
		try {
		if (retList.isEmpty()) {
			// just store the object we have
			sess.save(rurpmi);
			tx.commit();
			sess.close();
			tosave = rurpmi;
		} else {
			// update retList[0] with ritl data and save it
			retList.get(0).setShowagain(rurpmi.isShowagain());
			retList.get(0).setLastinteracted(rurpmi.getLastinteracted());
			tosave=retList.get(0);
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
