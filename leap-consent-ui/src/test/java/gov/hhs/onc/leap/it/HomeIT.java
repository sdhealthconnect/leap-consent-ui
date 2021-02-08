package gov.hhs.onc.leap.it;

import com.vaadin.flow.component.html.testbench.DivElement;
import org.junit.Assert;
import org.junit.Test;

public class HomeIT extends AbstractIT {

    @Test
    public void homeView() {
        getDriver().get(APP_URL);
        DivElement homeView = $(DivElement.class).id("home");
        Assert.assertNotNull(homeView);
    }

}
