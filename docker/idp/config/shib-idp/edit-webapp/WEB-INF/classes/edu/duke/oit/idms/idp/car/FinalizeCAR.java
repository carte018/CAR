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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.attribute.ByteAttributeValue;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.ScopedStringAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.XMLObjectAttributeValue;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.interceptor.AbstractProfileInterceptorAction;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.NameIDType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;

/**
 * @author shilen
 */
public class FinalizeCAR extends AbstractProfileInterceptorAction {

  /** Class logger. */
  @Nonnull
  private final Logger log = LoggerFactory.getLogger(FinalizeCAR.class);

  /** The {@link AttributeContext} to operate on. */
  @Nullable
  private AttributeContext attributeContext;

  /** Strategy used to find the {@link AttributeContext} from the {@link ProfileRequestContext}. */
  @Nonnull
  private Function<ProfileRequestContext, AttributeContext> attributeContextLookupStrategy;
  
  /** Constructor. */
  public FinalizeCAR() {
    attributeContextLookupStrategy =
      Functions.compose(new ChildContextLookup<RelyingPartyContext, AttributeContext>(AttributeContext.class),
          new ChildContextLookup<ProfileRequestContext, RelyingPartyContext>(RelyingPartyContext.class));    
  }

  /**
   * Set the attribute context lookup strategy.
   * 
   * @param strategy the attribute context lookup strategy
   */
  public void setAttributeContextLookupStrategy(Function<ProfileRequestContext, AttributeContext> strategy) {
    attributeContextLookupStrategy =
      Constraint.isNotNull(strategy, "Attribute context lookup strategy cannot be null");
  }

  /**
   * Get the attribute context.
   * 
   * @return the attribute context
   */
  @Nullable
  public AttributeContext getAttributeContext() {
    return attributeContext;
  }

  /** {@inheritDoc} */
  @Override
  protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
      @Nonnull final ProfileInterceptorContext interceptorContext) {
	  log.error("doPreExecute");
    attributeContext = attributeContextLookupStrategy.apply(profileRequestContext);
    if (attributeContext == null) {
      throw new RuntimeException("Invalid attribute context");
    }
    log.error("End doPreExecute");
    return super.doPreExecute(profileRequestContext, interceptorContext);
  }
  
  /** {@inheritDoc} */
  @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
      @Nonnull final ProfileInterceptorContext interceptorContext) {
	  log.error("doExecute");
    final Map<String, IdPAttribute> attributes = profileRequestContext.getSubcontext(CARContext.class).getIdPAttributes();
    log.error("doExecute2");
    final HttpServletRequest request = getHttpServletRequest();
    log.error("doExecute3");
    if (request == null) {
      throw new RuntimeException("Invalid profile context");
    }
    log.error("json retrieval");
    String jweString = request.getParameter("json");
    log.error("jweString POSTed was: " + jweString);
    String jsonDecoded = JWTUtils.decryptAndVerifySignature(jweString);
    log.error("Decoded JSON string was: " + jsonDecoded);
    
    
    JsonReader reader = null;
    JsonObject jsonData = null;
    try {
      reader = Json.createReader(new StringReader(jsonDecoded));
      jsonData = reader.readObject();
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
    log.error("Reader established");
    String carInstanceId = jsonData.getJsonObject("decisionResponse").getJsonObject("header").getString("carInstanceId");
    log.error("Found carInstance: " + carInstanceId);
    String decisionId = jsonData.getJsonObject("decisionResponse").getJsonObject("header").getString("decisionId");
    log.error("Found decisionId: " + decisionId);
    
    log.info("carInstanceId=" + carInstanceId + ", decisionId=" + decisionId);
    
    JsonArray decisions = jsonData.getJsonObject("decisionResponse").getJsonArray("decisions");
    Map<String, Set<String>> approvedAttributes = new HashMap<String, Set<String>>();
    log.error("Decision array has " + decisions.size() + " values");
    for (int i = 0; i < decisions.size(); i++) {
      JsonValue decision = decisions.get(i);
      String attributeName = ((JsonObject) decision).getString("name");
      String value = ((JsonObject) decision).getString("value");
      if ("permit".equalsIgnoreCase(((JsonObject) decision).getString("decision"))) {
        if (approvedAttributes.get(attributeName) == null) {
          approvedAttributes.put(attributeName, new HashSet<String>());
        }
        
        approvedAttributes.get(attributeName).add(value);
      }
    }
    log.error("Processing attribute release");
    final Map<String, IdPAttribute> releasedAttributes = new HashMap<String, IdPAttribute>(attributes);

    for (IdPAttribute attribute : attributes.values()) {
      if (!approvedAttributes.containsKey(attribute.getId())) {
        releasedAttributes.remove(attribute.getId());
        continue;
      }
      
      List<IdPAttributeValue<?>> existingValues = attributes.get(attribute.getId()).getValues();      
      List<IdPAttributeValue<?>> newValues = new ArrayList<IdPAttributeValue<?>>();
      
      for (IdPAttributeValue<?> value : existingValues) {
        String consentedValueCheck = null;
        if (value instanceof ScopedStringAttributeValue) {
          consentedValueCheck = ((ScopedStringAttributeValue)value).getValue() + "@" + ((ScopedStringAttributeValue)value).getScope();
        } else if (value instanceof StringAttributeValue) {
          consentedValueCheck = ((StringAttributeValue)value).getValue();
        } else if (value instanceof ByteAttributeValue) {
          consentedValueCheck = ((ByteAttributeValue)value).toBase64();
        } else if (value instanceof XMLObjectAttributeValue) {
          if (value.getValue() instanceof NameIDType) {
            consentedValueCheck = ((NameIDType) value.getValue()).getValue();
          } else {
            try {
              consentedValueCheck = SerializeSupport.nodeToString(XMLObjectSupport.marshall(((XMLObjectAttributeValue) value).getValue()));
            } catch (final MarshallingException e) {
              log.error("Error while marshalling XMLObject value", e);
              continue;
            }
          }
        } else {
          log.error("Unsupported type: " + value);
          continue;
        }
        
        if (approvedAttributes.get(attribute.getId()).contains(consentedValueCheck)) {
          newValues.add(value);
        }
      }
      
      if (newValues.size() == 0) {
        releasedAttributes.remove(attribute.getId());
        continue;
      }
      
      releasedAttributes.get(attribute.getId()).setValues(newValues);
    }
    log.error("{} Releasing attributes '{}'",getLogPrefix(),releasedAttributes);
    //log.debug("{} Releasing attributes '{}'", getLogPrefix(), releasedAttributes);
    //final MapDifference<String, IdPAttribute> diff = Maps.difference(attributes, releasedAttributes);
    //log.debug("{} Not releasing attributes '{}'", getLogPrefix(), diff.entriesOnlyOnLeft());

    attributeContext.setIdPAttributes(releasedAttributes.values());
    
    super.doExecute(profileRequestContext, interceptorContext);
  }
}
