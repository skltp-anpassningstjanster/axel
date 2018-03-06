package se.inera.axel.riv2ssek.internal;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
@Component
public class RivSsekBridgeTestRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("direct:in-riv2ssek")
        .to("{{riv2ssekEndpoint}}{{riv2ssekEndpoint.path}}");

        from("jetty:{{ssekEndpoint.server}}:{{ssekEndpoint.port}}?matchOnUriPrefix=true").routeId("ssek-mockserver")
        .convertBodyTo(String.class)
        .to("log:se.inera.test?level=INFO&showHeaders=true")
        .choice().when(header(Exchange.HTTP_PATH).isEqualTo("registerMedicalCertificate"))
            .to("mock:ssekRegisterMedicalCertificate")
        .otherwise()
            .to("mock:ssekHelloWorld");
    }
}
