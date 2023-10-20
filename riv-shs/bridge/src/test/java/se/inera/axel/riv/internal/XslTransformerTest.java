package se.inera.axel.riv.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.List;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.CamelTestSupport;
import org.testng.annotations.Test;

import se.inera.axel.shs.processor.ShsHeaders;

public class XslTransformerTest extends CamelTestSupport {

	private static final Namespaces NAMESPACES = new Namespaces("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");

    static {
        NAMESPACES.add("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
        NAMESPACES.add("registry", "urn:riv:itintegration:registry:1");
        NAMESPACES.add("ns3", "urn:riv:lv:reporting:pharmacovigilance:ProcessIndividualCaseSafetyReportResponder:1");
        NAMESPACES.add("ns4", "urn:riv:lv:reporting:pharmacovigilance:1");
    }

    @Test
    public void returnCorrectTransformation() throws InterruptedException, IOException {
    	
    	final String expectedResult = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
							+ "\n<!DOCTYPE ichicsr SYSTEM \"http://eudravigilance.ema.europa.eu/dtd/icsr21xml.dtd\">"
							+ "\n<ichicsr lang=\"se\">"
							+ "\n<ichicsrmessageheader>"
							+ "\n<messagetype>ichicsr</messagetype>";

        MockEndpoint mockEndpoint = getMockEndpoint("mock:xsltransform");
        mockEndpoint.reset();
        mockEndpoint.expectedMinimumMessageCount(1);

        template().request("direct:xsltransform", null);

        List<Exchange> x = mockEndpoint.getReceivedExchanges();
        String s = x.get(0).getIn().getBody().toString().replace("\t", "").replace("\r", "");
        assertThat(s.substring(0, expectedResult.length()), 
                equalTo(expectedResult));
        
    }

    @Override
    protected RouteBuilder[] createRouteBuilders() throws Exception {

       	final XslTransformer xslTransformer = new XslTransformer();
    	
    	RouteBuilder rb = new RouteBuilder() {

			@Override
			public void configure() throws Exception {

				from("direct:xsltransform")
					.setBody().simple("resource:classpath:xslt/SE-SEBRA-22.xml")
					.setHeader(ShsHeaders.PRODUCT_ID, simple("SE-SEBRA"))
				    .setProperty(ShsHeaders.X_SHS_XSL, simple("resource:classpath:xslt/SE-SEBRA.xsl"))
		        	.process(xslTransformer)
					.to("mock:xsltransform");
			}
    		
    	};
    	
        return new RouteBuilder[]{rb};
    }
}
