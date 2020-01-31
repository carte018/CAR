/*
 * Copyright 20xx - 20xx Duke University
    
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
package edu.duke.oit.idms.idp.car;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;

import net.shibboleth.idp.attribute.ByteAttributeValue;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.ScopedStringAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.XMLObjectAttributeValue;
import net.shibboleth.utilities.java.support.codec.Base64Support;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;

import org.apache.commons.lang.StringUtils;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.messaging.context.BaseContext;
import org.opensaml.saml.saml2.core.NameIDType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.nio.charset.StandardCharsets;
/**
 * @author shilen
 */
public class CARContext extends BaseContext {

  /** Class logger. */
  @Nonnull
  private final Logger log = LoggerFactory.getLogger(CARContext.class);

  
  private Map<String, IdPAttribute> attributes = new HashMap<String, IdPAttribute>();
  
  private String spRelyingPartyId;
  
  private String idpRelyingPartyId;

  /**
   * @return attributes
   */
  public Map<String, IdPAttribute> getIdPAttributes() {
    return attributes;
  }
  
  /**
   * @param httpServletRequest 
   * @param environment 
   * @param flowExecutionUrl 
   * @return request
   */
  public String getRequest(HttpServletRequest httpServletRequest, Environment environment, String flowExecutionUrl) {

	String urlPrefix = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName();
    if (!StringUtils.isEmpty(environment.getProperty("car.urlPrefix"))) {
      urlPrefix = environment.getProperty("car.urlPrefix");
    }
    
    JsonArrayBuilder attributesJson = Json.createArrayBuilder();
    
    for (String attributeName : attributes.keySet()) {
      IdPAttribute idpAttribute = attributes.get(attributeName);
      if (idpAttribute.getValues() == null || idpAttribute.getValues().size() == 0) {
        continue;
      }
      
      JsonArrayBuilder valuesJson = Json.createArrayBuilder();

      for (IdPAttributeValue<?> idpAttributeValue : idpAttribute.getValues()) {
        if (idpAttributeValue instanceof ScopedStringAttributeValue) {
          valuesJson.add(((ScopedStringAttributeValue)idpAttributeValue).getValue() + "@" + ((ScopedStringAttributeValue)idpAttributeValue).getScope());
        } else if (idpAttributeValue instanceof StringAttributeValue) {
          valuesJson.add(((StringAttributeValue)idpAttributeValue).getValue());
        } else if (idpAttributeValue instanceof ByteAttributeValue) {
          valuesJson.add(((ByteAttributeValue)idpAttributeValue).toBase64());
        } else if (idpAttributeValue instanceof XMLObjectAttributeValue) {          
          if (idpAttributeValue.getValue() instanceof NameIDType) {
            valuesJson.add(((NameIDType) idpAttributeValue.getValue()).getValue());
          } else {
            try {
              valuesJson.add(SerializeSupport.nodeToString(XMLObjectSupport.marshall(((XMLObjectAttributeValue) idpAttributeValue).getValue())));
            } catch (final MarshallingException e) {
              log.error("Error while marshalling XMLObject value", e);
              //return null;
            }
          }
        } else {
          // unsupported type?
          log.error("Unsupported type: " + idpAttributeValue);
        }
      }
      
      attributesJson.add(
        Json.createObjectBuilder()
          .add("name", idpAttribute.getId())
          .add("values", valuesJson.build())
        .build());
    }
    
    JsonObject request = Json.createObjectBuilder()
      .add("request",
        Json.createObjectBuilder()
          .add("header",
            Json.createObjectBuilder()
              .add("identifierName", environment.getProperty("car.identifierName"))
              .add("callbackUrl", urlPrefix + flowExecutionUrl + "&_eventId_proceed=1")
              .add("spRelyingPartyId", spRelyingPartyId)
              .add("idpRelyingPartyId", idpRelyingPartyId)
              .build())
          .add("attributes", attributesJson.build())
          .build())
      .build();

    String jsonRequest = request.toString();
    String base64Request = null;
    try {
    	base64Request = Base64Support.encode(jsonRequest.getBytes(StandardCharsets.UTF_8), false);
    	log.error("Encoded presign request: " + base64Request);
    	log.error("Decoded presign request in UTF8: " + new String(Base64Support.decode(base64Request),"UTF-8"));
    } catch (Exception exc) {
    	throw new RuntimeException("Failed encoding ",exc);
    }
    return JWTUtils.signAndEncrypt(base64Request, idpRelyingPartyId);
  }

  
  /**
   * @return the spRelyingPartyId
   */
  public String getSpRelyingPartyId() {
    return spRelyingPartyId;
  }

  
  /**
   * @param spRelyingPartyId the spRelyingPartyId to set
   */
  public void setSpRelyingPartyId(String spRelyingPartyId) {
    this.spRelyingPartyId = spRelyingPartyId;
  }

  
  /**
   * @return the idpRelyingPartyId
   */
  public String getIdpRelyingPartyId() {
    return idpRelyingPartyId;
  }

  
  /**
   * @param idpRelyingPartyId the idpRelyingPartyId to set
   */
  public void setIdpRelyingPartyId(String idpRelyingPartyId) {
    this.idpRelyingPartyId = idpRelyingPartyId;
  }
}
