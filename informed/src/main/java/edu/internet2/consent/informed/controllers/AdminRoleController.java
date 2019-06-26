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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.consent.informed.cfg.InformedConfig;
import edu.internet2.consent.informed.model.ActivityStreamEntry;
import edu.internet2.consent.informed.model.AdminRoleMapping;
import edu.internet2.consent.informed.model.LogCriticality;
import edu.internet2.consent.informed.util.InformedUtility;

@Path("adminrole")
public class AdminRoleController {

	@SuppressWarnings("unused")
	private String caller="";
	@SuppressWarnings("unused")
	private final Log LOG = LogFactory.getLog(IIICController.class);
	
	// Response builder
	private Response buildResponse(Status code, String entity) {
		return Response.status(code).entity(entity).header("Access-Control-Allow-Origin","http://editor.swagger.io").header("Access-Control-Allow-methods","GET, POST, PUT, DELETE").header("Access-Control-Allow-Credentials","true").header("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept").type("application/json").build();
	}
	
	//
	// We implement basic CRUD-style endpoints for admin role mappings.
	//
	// An individual mapping is essentially a 3-tuple of (subject, role, target).
	//
	// Subject may be any of a group identifier (typically urn), a user identifier (typically
	// eppn or another unique identifier) or an entitlement string.  
	//
	// Role is selected from the set of admin roles available in the tool:
	//
	//     superadmin - all the powers, all the time
	//     RHRegistrar - CRUD for all RHs (sans policies)
	//	   DelegatedRHRegistrar - edit for all properties of a single RH (sans policies)
	//	   PolicyAdmin - CRUD + reorder for all institutional and meta policies
	//	   DelegatedPolicyAdmin - PolicyAdmin for only single RH's policies
	//	   RPRegistrar - CRUD for all RPs
	//	   DelegatedRPRegistrar - CRUD for RPs attached to a single RH
	//     RPOwner - edit for properties of a single RP
	//	   Translator - managed language bindings across the environment
	//
	// Target is then a regex specifying the target(s) over which the subject may exert
	// the role.  For the superadmin, RHRegistrar, PolicyAdmin, and RPRegistrar roles,
	// the only meaningful target value is ".*" or empty (which is presumed to be ".*".
	// For other roles, the target is a regex that must be matched by the target of the 
	// role (eg., the RP Entity ID for an RPOwner, or the RH entity ID for a DelegatedRP
	// Registrar or DelegatedRHRegistrar/DelegatedPolicyAdmin.
	//
	// 
	
	@POST
	@Path("/")
	public Response postAdminRoleMapping(@Context HttpServletRequest req, @Context HttpHeaders headers, String entity) {
	
		@SuppressWarnings("unused")
		InformedConfig config = null;
		
		try {
			config = InformedUtility.init("postAdminRoleMapping", req, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}
		
		ObjectMapper mapper = new ObjectMapper();
		AdminRoleMapping entry = null;
		
		try {
			entry = mapper.readValue(entity, AdminRoleMapping.class);
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
		
		// Force time of creation and status
		
		entry.setCreated(System.currentTimeMillis());
		
		entry.setStatus("active");
		
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
			return InformedUtility.locError(500, "ERR0016",LogCriticality.error);
		}
	}
	
	@DELETE
	@Path("/{armid}")
	public Response deleteAdminRoleMapping(@Context HttpServletRequest req, @Context HttpHeaders headers, String entity, @PathParam("armid") long armid) {
		
		// Given an armid for an adminrole mapping, mark it as inactive (effectively
		// deleting it.  Set the archived data as well.
		//
		
		@SuppressWarnings("unused")
		InformedConfig config = null;
		
		try {
			config = InformedUtility.init("getAdminRoleMapping", req, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}		
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}
		
		Transaction tx = sess.beginTransaction();
		
		Query<AdminRoleMapping> getQuery = sess.createQuery("from AdminRoleMapping where adminRoleId = :armid",AdminRoleMapping.class);
		getQuery.setParameter("armid", armid);
		
		List<AdminRoleMapping> larm = getQuery.list();
		
		if (larm == null || larm.isEmpty()) {
			return buildResponse(Status.NOT_FOUND,"");
		} else {
			AdminRoleMapping tosave = larm.get(0);
			tosave.setArchived(System.currentTimeMillis());
			tosave.setStatus("inactive");
			try {
				tx.commit();
				sess.close();
			} catch (Exception e) {
				tx.rollback();
				throw new RuntimeException("Transaction rollback",e);
			} finally {
				if (sess.isOpen())
					sess.close();
			}
		}
		
		return buildResponse(Status.ACCEPTED,"");
	}
	
	@GET
	@Path("/")
	public Response getAdminRoleMappings(@Context HttpServletRequest req, @Context HttpHeaders headers, String entity) {
		
		@SuppressWarnings("unused")
		InformedConfig config = null;
		
		try {
			config = InformedUtility.init("getAdminRoleMapping", req, headers, null);
		} catch (Exception e) {
			return InformedUtility.locError(500,"ERR0004",LogCriticality.error);
		}		
		
		// Get a Hibernate session
		Session sess = InformedUtility.getHibernateSession();
		if (sess == null) {
			return InformedUtility.locError(500, "ERR0018",LogCriticality.error);
		}
		
		Query<AdminRoleMapping> getQuery = null;
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("from AdminRoleMapping where status = 'active'");
		
		// If there is a subject restriction, add it
		
		if (req.getParameter("subject") != null && ! req.getParameter("subject").contentEquals("")) {
			sb.append(" and (subject like '%*%' or subject = :subject)");
		}
		
		// If there is a collection of subject restrictions (via "subjects",
		// retrieve those into a phrase for retrieval
		//
		boolean subjects = false;
		if (req.getParameter("subjects") != null && ! req.getParameter("subjects").contentEquals("")) {
			subjects = true;  // we need to provision for the bulk subject request
			String sl = req.getParameter("subjects");
			sb.append(" and (subject like '%*%'");
			int ctr = 1;
			for (String s : sl.split(";")) {
				sb.append(" or subject=:sub"+ctr++);
			}
			sb.append(")");
		}
		
		
		// Likewise a role restriction
		
		if (req.getParameter("role") != null && ! req.getParameter("role").contentEquals("")) {
			sb.append(" and roleName = :role");  // exact only
		}
		
		// And a target restriction
		
		if (req.getParameter("target") != null && ! req.getParameter("target").contentEquals("")) {
			sb.append(" and (target like '%*%' or target = :target)");
		}
		
		String qstring = sb.toString();
		
		getQuery = sess.createQuery(qstring,AdminRoleMapping.class);
		
		if (qstring.contains(":subject")) {
			getQuery.setParameter("subject", req.getParameter("subject"));
		}
		if (qstring.contains(":role")) {
			getQuery.setParameter("role", req.getParameter("role"));
		}
		if (qstring.contains(":target")) {
			getQuery.setParameter("target", req.getParameter("target"));
		}
		
		if (subjects) {
			// we need to produce the subjects insertions
			int ctr = 1;
			for (String s : req.getParameter("subjects").split(";")) {
				getQuery.setParameter("sub"+ctr++, s);
			}
		}
		
		List<AdminRoleMapping> larm = getQuery.list();
		
		if (larm.isEmpty()) {
			sess.close();
			return InformedUtility.locError(404, "ERR0065",LogCriticality.info);
		} else {
			try {
				ObjectMapper om = new ObjectMapper();
				return buildResponse(Status.OK,om.writeValueAsString(larm));
			} catch (Exception e) {
				return InformedUtility.locError(500,"ERR0016",LogCriticality.error);
			} finally {
				sess.close();
			}
		}
	}
}
