package se.inera.axel.rivssek.webconsole;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.testng.annotations.Test;
import se.inera.axel.riv2ssek.RivSsekServiceMapping;
import se.inera.axel.riv2ssek.RivSsekServiceMappingRepository;

import static org.mockito.Mockito.*;

import static org.testng.Assert.assertEquals;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class RivSsekServiceMappingEditPageTest extends RivSsekWebconsolePageTest {

    private RivSsekServiceMappingRepository rivSsekServiceMappingRepository;

    @Override
    protected void beforeMethodSetup() {
        super.beforeMethodSetup();

        rivSsekServiceMappingRepository = mock(RivSsekServiceMappingRepository.class);

        injector.registerBean("rivSsekServiceMappingRepository", rivSsekServiceMappingRepository);
    }

    @Test
    public void renderNew() {
        PageParameters pageParameters = new PageParameters();
        tester.startPage(RivSsekServiceMappingEditPage.class, pageParameters);
        tester.assertRenderedPage(RivSsekServiceMappingEditPage.class);
        tester.assertNoErrorMessage();
    }

    @Test
    public void saveNewMapping() {
        PageParameters pageParameters = new PageParameters();
        tester.startPage(RivSsekServiceMappingEditPage.class, pageParameters);

        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("control.rivServiceNamespace:rivServiceNamespace", "urn:riv:test");
        formTester.setValue("control.rivLogicalAddress:rivLogicalAddress", "123456-0000");
        formTester.setValue("control.address:address", "http://test/ssek/service");
        formTester.setValue("control.ssekReceiverType:ssekReceiverType", "CN");
        formTester.setValue("control.ssekReceiver:ssekReceiver", "SsekReceiver");

        formTester.submit();

        tester.assertNoErrorMessage();
        verify(rivSsekServiceMappingRepository, atLeastOnce()).save(any(RivSsekServiceMapping.class));
    }

    @Test
    public void saveWithIncompleteFormShouldFail() {
        PageParameters pageParameters = new PageParameters();
        tester.startPage(RivSsekServiceMappingEditPage.class, pageParameters);

        FormTester formTester = tester.newFormTester("form");

        formTester.submit();

        assertEquals(tester.getMessages(FeedbackMessage.ERROR).size(), 4);
        verify(rivSsekServiceMappingRepository, never()).save(any(RivSsekServiceMapping.class));
    }

}
