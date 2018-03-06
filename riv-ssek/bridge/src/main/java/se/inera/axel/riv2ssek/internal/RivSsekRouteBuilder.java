/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.riv2ssek.internal;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static org.apache.camel.builder.PredicateBuilder.not;
import static org.apache.camel.builder.PredicateBuilder.or;

/**
 * Defines Camel routes for RIV <---> SSEK
 */
public class RivSsekRouteBuilder extends RouteBuilder {
    private final Namespaces namespaces = new Namespaces("riv", "urn:riv:itintegration:registry:1")
            .add("add", "http://www.w3.org/2005/08/addressing")
            .add("soap", "http://schemas.xmlsoap.org/soap/envelope/")
            .add("ssek", "http://schemas.ssek.org/ssek/2006-05-10/");
    private final String SSEK_MAPPING = "ssekMapping";
    public static final String RIV_CORR_ID = "x-skltp-correlation-id";

    @Override
    public void configure() throws Exception {
        from("{{riv2ssekEndpoint}}{{riv2ssekEndpoint.path}}").routeId("riv2ssek")
        .onException(Exception.class)
            .handled(true)
            .logHandled(true)
            .to("direct:soapFaultErrorResponse")
        .end()
        .setHeader("sender", simple("${properties:ssekDefaultSender}"))
        .setHeader("receiver").xpath("//riv:LogicalAddress", String.class, namespaces)
        .choice().when(header("receiver").isEqualTo(""))
            .setHeader("receiver").xpath("//add:To", String.class, namespaces)
        .end()
        .validate(not(header("receiver").isEqualTo("")))
        .setHeader("txId", header(RIV_CORR_ID))
        .choice().when(or(header("txId").isNull(), header("txId").isEqualTo("")))
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader("txId", UUID.randomUUID().toString());
                }
            })
        .end()
        .to("direct:camel2ssek");

        from("direct:camel2ssek")
        .onException(Exception.class)
            .handled(true)
            .logHandled(true)
            .to("direct:soapFaultErrorResponse")
        .end()
        .setProperty("ssekService",
                method("rivSsekMappingService", "lookupSsekService(${header.receiver}, ${header.SOAPAction}))"))
        .to("xquery:META-INF/xquery/camel2ssek.xquery")
        .removeHeaders("*")
        .setHeader("SOAPAction", constant(""))
        .setHeader(Exchange.CONTENT_TYPE, constant("application/xml"))
        .setHeader(Exchange.HTTP_URI, simple("${property.ssekService.address}"))
        .to("jetty://http://ssekService?throwExceptionOnFailure=false&sslContextParametersRef=mySslContext");

        from("direct:soapFaultErrorResponse")
        .setHeader("faultstring").simple("Error reported: ${exception.message} - could not process request.")
        .to("velocity:/META-INF/velocity/soapfault.vm")
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
    }
}
