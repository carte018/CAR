package edu.internet2.consent.caradmin;

import net.shibboleth.ext.spring.velocity.VelocityView;

import java.io.StringWriter;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.context.ApplicationContextException;

public class VelocityLayoutView extends VelocityView {

    public static final String DEFAULT_LAYOUT_URL = "layout.vm";
    public static final String DEFAULT_LAYOUT_KEY = "layout";
    public static final String DEFAULT_SCREEN_CONTENT_KEY = "screen_content";

    public String layoutUrl = DEFAULT_LAYOUT_URL;
    public String layoutKey = DEFAULT_LAYOUT_KEY;
    public String screenContentKey = DEFAULT_SCREEN_CONTENT_KEY;

    public void setLayoutUrl(String layoutUrl) {
            this.layoutUrl = layoutUrl;
    }

    public void setLayoutKey(String layoutKey) {
            this.layoutKey = layoutKey;
    }

    public void setScreenContentKey(String screenContentKey) {
            this.screenContentKey = screenContentKey;
    }

    protected void checkTemplate() throws ApplicationContextException {

            try {
                    getTemplate(this.layoutUrl);
            } catch (ResourceNotFoundException e) {
                    throw new ApplicationContextException("Cannot find velocity template for URL [" + this.layoutUrl + "]:  Did you specify the correct resource loader path?",e);
            } catch (Exception ex) {
                throw new ApplicationContextException( "Could not load velocity template for URL [" + this.layoutUrl + "]",ex);
         }
 }

 protected void doRender(Context context, HttpServletResponse response) throws Exception {

         renderScreenContent(context);

         String layoutUrlToUse = (String) context.get(this.layoutKey);
         if (layoutUrlToUse != null) {
                 if (logger.isDebugEnabled()) {
                         logger.debug("Screen content template has requested layout [" + layoutUrlToUse + "]");
                 }
         } else {
                 layoutUrlToUse = this.layoutUrl;
         }

         mergeTemplate(getTemplate(layoutUrlToUse), context, response);
 }

 private void renderScreenContent(Context velocityContext) throws Exception {
         if (logger.isDebugEnabled()) {
                 logger.debug("Rendering screen content template [" + getUrl() + "]");
         }

         StringWriter sw = new StringWriter();
         Template screenContentTemplate = getTemplate(getUrl());
         screenContentTemplate.merge(velocityContext, sw);

         velocityContext.put(this.screenContentKey, sw.toString());
 }
}

