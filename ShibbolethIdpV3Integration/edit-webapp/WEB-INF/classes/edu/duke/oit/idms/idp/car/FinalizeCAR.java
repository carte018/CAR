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

    attributeContext = attributeContextLookupStrategy.apply(profileRequestContext);
    if (attributeContext == null) {
      throw new RuntimeException("Invalid attribute context");
    }

    return super.doPreExecute(profileRequestContext, interceptorContext);
  }
  
  /** {@inheritDoc} */
  @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
      @Nonnull final ProfileInterceptorContext interceptorContext) {

    final Map<String, IdPAttribute> attributes = profileRequestContext.getSubcontext(CARContext.class).getIdPAttributes();

    final HttpServletRequest request = getHttpServletRequest();
    if (request == null) {
      throw new RuntimeException("Invalid profile context");
    }
    
    String jweString = request.getParameter("json");
    String jsonDecoded = JWTUtils.decryptAndVerifySignature(jweString);
    
    
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
    
    String carInstanceId = jsonData.getJsonObject("decisionResponse").getJsonObject("header").getString("carInstanceId");
    String decisionId = jsonData.getJsonObject("decisionResponse").getJsonObject("header").getString("decisionId");
    
    log.info("carInstanceId=" + carInstanceId + ", decisionId=" + decisionId);
    
    JsonArray decisions = jsonData.getJsonObject("decisionResponse").getJsonArray("decisions");
    Map<String, Set<String>> approvedAttributes = new HashMap<String, Set<String>>();
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

    //log.debug("{} Releasing attributes '{}'", getLogPrefix(), releasedAttributes);
    //final MapDifference<String, IdPAttribute> diff = Maps.difference(attributes, releasedAttributes);
    //log.debug("{} Not releasing attributes '{}'", getLogPrefix(), diff.entriesOnlyOnLeft());

    attributeContext.setIdPAttributes(releasedAttributes.values());
    
    super.doExecute(profileRequestContext, interceptorContext);
  }
}
