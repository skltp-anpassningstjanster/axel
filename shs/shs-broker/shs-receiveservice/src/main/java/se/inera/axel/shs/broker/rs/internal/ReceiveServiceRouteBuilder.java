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
package se.inera.axel.shs.broker.rs.internal;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

import se.inera.axel.shs.broker.messagestore.MessageAlreadyExistsException;
import se.inera.axel.shs.camel.SetShsExceptionAsBody;
import se.inera.axel.shs.exception.IllegalSenderException;
import se.inera.axel.shs.exception.ShsException;
import se.inera.axel.shs.processor.AxelHeaders;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.processor.ShsMessageMarshaller;
import se.inera.axel.shs.processor.TimestampConverter;

import java.net.HttpURLConnection;

/**
 * Defines pipeline for processing and routing SHS messages
 */
public class ReceiveServiceRouteBuilder extends RouteBuilder {


    @Override
    public void configure() throws Exception {
        // Handle MimeMessage
        from("{{shsRsHttpEndpoint}}{{shsRsPathPrefix}}?disableStreamCache=true")
        .routeId("/shs/rs")
        .onException(ShsException.class)
            .handled(true)
            .log(LoggingLevel.ERROR, "ShsException caught: ${exception.stacktrace}")
            .bean(SetShsExceptionAsBody.class)
            .setHeader(ShsHeaders.X_SHS_ERRORCODE, simple("${body.errorCode}"))
            .removeHeader(AxelHeaders.CALLER_IP)
            .removeHeader(AxelHeaders.SENDER_CERTIFICATE)
            .transform(simple("${body.message}"))
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpURLConnection.HTTP_BAD_REQUEST))
        .end()
        .onException(Exception.class)
            .log(LoggingLevel.ERROR, "Exception caught: ${exception.stacktrace}")
            .transform(simple("${exception.message}"))
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpURLConnection.HTTP_INTERNAL_ERROR))
            .handled(true)
        .end()
        .filter(header(Exchange.HTTP_METHOD).isEqualTo("POST"))
        .to("shs:direct-vm:shs:rs")
        .choice().when().simple("${property.ShsLabel.transferType} == 'SYNCH'")
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpURLConnection.HTTP_OK))
        .otherwise()
            .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpURLConnection.HTTP_ACCEPTED))
        .end();


        // Handle ShsMessage object
        from("direct-vm:shs:rs").routeId("direct-vm:shs:rs")
        .streamCaching()
        .onException(MessageAlreadyExistsException.class)
            .onWhen(simple("${exception.label.transferType} == 'ASYNCH'"))
            .setHeader(ShsHeaders.X_SHS_CORRID, simple("${exception.label.corrId}"))
            .setHeader(ShsHeaders.X_SHS_CONTENTID, simple("${exception.label.content.contentId}"))
            .setHeader(ShsHeaders.X_SHS_NODEID, simple("${properties:nodeId}"))
            .setHeader(ShsHeaders.X_SHS_TXID, simple("${exception.label.txId}"))
            .setHeader(ShsHeaders.X_SHS_ARRIVALDATE, simple("${exception.previousMessageTimestamp}"))
            .setHeader(ShsHeaders.X_SHS_ARRIVALDATE,
                    simple("${date:header." + ShsHeaders.X_SHS_ARRIVALDATE
                            + ":" + TimestampConverter.DATETIME_FORMAT + "}"))
            .setHeader(ShsHeaders.X_SHS_DUPLICATEMSG, constant("yes"))
            .transform(header(ShsHeaders.X_SHS_TXID))
            .handled(true)
        .end()
        .setProperty(ShsHeaders.LABEL, method(ShsMessageMarshaller.class, "parseLabel"))
        .beanRef("senderValidationService", "validateSender(${header.AxelCallerIp}, ${header.AxelSenderCertificate}, ${property.ShsLabel.from.value})")
        .choice().when().simple("${property.ShsLabel.transferType} == 'SYNCH'")
            .to("direct-vm:shs:synch")
        .when(header(AxelHeaders.ROBUST_ASYNCH_SHS).isNotNull())
                .bean(SaveMessageProcessor.class)
                .transform(method("labelHistoryTransformer"))
                .transform(method("fromValueTransformer"))
                .to("direct-vm:shs:asynch_process")
                .transform(simple("ok"))
        .otherwise()
            .bean(SaveMessageProcessor.class)
            .transform(method("labelHistoryTransformer"))
            .transform(method("fromValueTransformer"))
            .to("direct-vm:shs:asynch")
            .setHeader(ShsHeaders.X_SHS_CORRID, simple("${body.label.corrId}"))
            .setHeader(ShsHeaders.X_SHS_CONTENTID, simple("${body.label.content.contentId}"))
            .setHeader(ShsHeaders.X_SHS_NODEID, simple("${properties:nodeId}"))
            .setHeader(ShsHeaders.X_SHS_LOCALID, simple("${body.id}"))
            .setHeader(ShsHeaders.X_SHS_TXID, simple("${body.label.txId}"))
            .setHeader(ShsHeaders.X_SHS_ARRIVALDATE, simple("${body.arrivalTimeStamp}"))
            .setHeader(ShsHeaders.X_SHS_ARRIVALDATE,
                    simple("${date:header." + ShsHeaders.X_SHS_ARRIVALDATE
                            + ":" + TimestampConverter.DATETIME_FORMAT + "}"))
            .setHeader(ShsHeaders.X_SHS_DUPLICATEMSG, constant("no"))
            .transform(simple("${body.label.txId}"))
        .end();
    }

}
