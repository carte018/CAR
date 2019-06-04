package edu.duke.oit.idms.idp.car;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.HashSet;
import java.util.Map;

import net.shibboleth.idp.attribute.context.AttributeContext;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.profile.interceptor.AbstractProfileInterceptorAction;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;

/**
 * @author shilen
 */
public class InitializeCARContext extends AbstractProfileInterceptorAction {

  /** Class logger. */
  @Nonnull
  private final Logger log = LoggerFactory.getLogger(InitializeCARContext.class);

  /** The {@link AttributeContext} to operate on. */
  @Nullable
  private AttributeContext attributeContext;
  
  @Nullable
  private RelyingPartyContext relyingPartyContext;

  /** Strategy used to find the {@link AttributeContext} from the {@link ProfileRequestContext}. */
  @Nonnull
  private Function<ProfileRequestContext, AttributeContext> attributeContextLookupStrategy;

  /** RelyingPartyContext lookup strategy. */
  @Nonnull
  private Function<ProfileRequestContext, RelyingPartyContext> relyingPartyContextLookupStrategy;
  
  /** Constructor. */
  public InitializeCARContext() {
    attributeContextLookupStrategy =
      Functions.compose(new ChildContextLookup<RelyingPartyContext, AttributeContext>(AttributeContext.class),
          new ChildContextLookup<ProfileRequestContext, RelyingPartyContext>(RelyingPartyContext.class));
    
    relyingPartyContextLookupStrategy = new ChildContextLookup<ProfileRequestContext, RelyingPartyContext>(RelyingPartyContext.class);
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
   * Set the strategy used to locate the {@link RelyingPartyContext} to operate on.
   *
   * @param strategy lookup strategy
   */
  public void setRelyingPartyContextLookupStrategy(Function<ProfileRequestContext, RelyingPartyContext> strategy) {
      relyingPartyContextLookupStrategy = Constraint.isNotNull(strategy,
              "RelyingPartyContext lookup strategy cannot be null");
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
    log.debug("{} Found attributeContext '{}'", getLogPrefix(), attributeContext);
    if (attributeContext == null) {
      throw new RuntimeException("Invalid attribute context");
    }

    relyingPartyContext = relyingPartyContextLookupStrategy.apply(profileRequestContext);
    if (relyingPartyContext == null) {
      throw new RuntimeException("Invalid relying party context");
    }

    return super.doPreExecute(profileRequestContext, interceptorContext);
  }

  /** {@inheritDoc} */
  @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
      @Nonnull final ProfileInterceptorContext interceptorContext) {

    final Map<String, IdPAttribute> attributes = attributeContext.getIdPAttributes();

    final CARContext consentContext = new CARContext();
    consentContext.setSpRelyingPartyId(relyingPartyContext.getRelyingPartyId());
    consentContext.setIdpRelyingPartyId(relyingPartyContext.getConfiguration().getResponderId());
    consentContext.getIdPAttributes().putAll(attributes);
    attributeContext.setIdPAttributes(new HashSet<IdPAttribute>());

    profileRequestContext.addSubcontext(consentContext, true);

    super.doExecute(profileRequestContext, interceptorContext);
  }
}
