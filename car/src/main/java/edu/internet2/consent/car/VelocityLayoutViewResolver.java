package edu.internet2.consent.car;

import net.shibboleth.ext.spring.velocity.VelocityViewResolver;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

public class VelocityLayoutViewResolver extends VelocityViewResolver {

    private String layoutUrl;
    private String layoutKey;
    private String screenContentKey;

    public VelocityLayoutViewResolver() {
            setViewClass(VelocityLayoutView.class);
    }

    @Override
    protected Class requiredViewClass() {
            return net.shibboleth.ext.spring.velocity.VelocityView.class;
    }

    public void setLayoutUrl(String layoutUrl) {
            this.layoutUrl = layoutUrl;
    }

    public void setLayoutKey(String layoutKey) {
            this.layoutKey = layoutKey;
    }

    public void setScreenContentKey(String screenContentKey) {
            this.screenContentKey = screenContentKey;
    }

    // Work happens here

    @Override
    protected AbstractUrlBasedView buildView(String viewName) throws Exception {
            edu.internet2.consent.car.VelocityLayoutView view = (edu.internet2.consent.car.VelocityLayoutView) super.buildView(viewName);

            if (this.layoutUrl != null) {
                    view.setLayoutUrl(this.layoutUrl);
            }
            if (this.layoutKey != null) {
                view.setLayoutKey(this.layoutKey);
        }
        if (this.screenContentKey != null) {
                view.setScreenContentKey(this.screenContentKey);
        }

        return view;
}


}
