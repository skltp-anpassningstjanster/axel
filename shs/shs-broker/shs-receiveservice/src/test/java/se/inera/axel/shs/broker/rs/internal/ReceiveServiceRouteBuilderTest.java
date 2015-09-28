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

import com.natpryce.makeiteasy.Maker;

import org.apache.camel.*;
import org.apache.camel.component.mock.MockEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import se.inera.axel.shs.broker.messagestore.MessageAlreadyExistsException;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntryMaker;
import se.inera.axel.shs.broker.validation.SenderValidationService;
import se.inera.axel.shs.exception.IllegalSenderException;
import se.inera.axel.shs.exception.UnknownReceiverException;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.AxelHeaders;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.processor.TimestampConverter;
import se.inera.axel.shs.xml.label.TransferType;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessage;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessageInstantiator.label;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabel;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.*;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.To;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ToInstantiator.value;

@ContextConfiguration
public class ReceiveServiceRouteBuilderTest extends AbstractTestNGSpringContextTests {

    static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReceiveServiceRouteBuilderTest.class);

    @Autowired
    MessageLogService messageLogService;

    @Autowired
    SenderValidationService senderValidationService;

    @Produce(context = "shs-broker-main-test", uri = "direct:in-vm")
    ProducerTemplate camel;

    @EndpointInject(uri = "mock:synchron")
    MockEndpoint synchronEndpoint;

    @EndpointInject(uri = "mock:asynchron")
    MockEndpoint asynchronEndpoint;

    @Value("shsRsPathPrefix")
    private String pathPrefix;

    static {
        System.setProperty("shsRsHttpEndpoint",
                String.format("jetty://http://localhost:%s", org.apache.camel.test.AvailablePortFinder.getNextAvailable()));
    }

    @DirtiesContext
    @Test
    public void sendingSynchRequestWithInvalidSenderShouldThrowException() throws Exception {
        doThrow(new IllegalSenderException("sender not valid")).when(
                senderValidationService).validateSender(any(String.class), any(String.class), any(String.class));

        final String CERTIFICATE_VALUE = "certificateValue";
        final String IP_ADDRESS_VALUE = "ipAddressValue";
        ShsMessage testMessage = make(createSynchMessageWithKnownReceiver());
        try {

            Map<String, Object> headers = new HashMap<String, Object>();
            headers.put(AxelHeaders.SENDER_CERTIFICATE, CERTIFICATE_VALUE );
            headers.put(AxelHeaders.CALLER_IP, IP_ADDRESS_VALUE);

            camel.sendBodyAndHeaders("direct:in-vm", testMessage, headers );

            Assert.fail("request should throw exception");
        } catch (CamelExecutionException e) {
            Throwable cause = e.getCause();
            
            Assert.assertNotNull(cause);
            Assert.assertTrue(cause instanceof IllegalSenderException, "exception should be 'IllegalSenderException'");
        }
        
        verify(senderValidationService).validateSender(IP_ADDRESS_VALUE, CERTIFICATE_VALUE, testMessage.getLabel().getFrom().getValue());
    }

    @DirtiesContext
    @Test
    public void sendingAsynchRequestWithInvalidSenderShouldThrowException() throws Exception {
        doThrow(new IllegalSenderException("sender not valid")).when(
                senderValidationService).validateSender(any(String.class), any(String.class), any(String.class));

        final String CERTIFICATE_VALUE = "certificateValue";
        final String IP_ADDRESS_VALUE = "ipAddressValue";
        ShsMessage testMessage = make(createAsynchMessageWithKnownReceiver());
        try {

            Map<String, Object> headers = new HashMap<String, Object>();
            headers.put(AxelHeaders.SENDER_CERTIFICATE, CERTIFICATE_VALUE );
            headers.put(AxelHeaders.CALLER_IP, IP_ADDRESS_VALUE);

            camel.sendBodyAndHeaders("direct:in-vm", testMessage, headers );

            Assert.fail("request should throw exception");
        } catch (CamelExecutionException e) {
            Throwable cause = e.getCause();
            
            Assert.assertNotNull(cause);
            Assert.assertTrue(cause instanceof IllegalSenderException, "exception should be 'IllegalSenderException'");
        }
        
        verify(senderValidationService).validateSender(IP_ADDRESS_VALUE, CERTIFICATE_VALUE, testMessage.getLabel().getFrom().getValue());
    }

    @DirtiesContext
    @Test
    public void sendingSynchRequestWithKnownReceiverInVmShouldWork() throws Exception {
        final ShsMessage testMessageEntry = make(createSynchMessageWithKnownReceiver());

        synchronEndpoint.expectedMessageCount(1);

        ShsMessage response = camel.requestBody("direct:in-vm", testMessageEntry, ShsMessage.class);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getLabel().getTxId(), testMessageEntry.getLabel().getTxId());

        MockEndpoint.assertIsSatisfied(synchronEndpoint);
    }

    @DirtiesContext
    @Test(enabled = false)
    public void sendingSynchRequestWithUnknownReceiverInVmShouldThrow() throws Exception {
        synchronEndpoint.expectedMessageCount(1);

        try {
            ShsMessage testMessage = make(createSynchMessageWithUnknownReceiver());
            camel.requestBody("direct:in-vm", testMessage, String.class);

            Assert.fail("request should throw exception");
        } catch (Exception e) {
            Throwable cause = e.getCause();
            Assert.assertNotNull(cause);
            Assert.assertTrue(cause instanceof UnknownReceiverException, "exception should be 'UnknownReceiverException'");
        }

    }

    @DirtiesContext
    @Test
    public void sendingAsynchRequestInVmShouldWork() throws Exception {
        asynchronEndpoint.expectedMessageCount(1);

        ShsMessage shsMessageEntry = make(createAsynchMessageWithKnownReceiver());

        String response = camel.requestBody("direct:in-vm", shsMessageEntry, String.class);

        Assert.assertNotNull(response);

        MockEndpoint.assertIsSatisfied(asynchronEndpoint);

        List<Exchange> exchanges = asynchronEndpoint.getReceivedExchanges();
        ShsMessageEntry entry = exchanges.get(0).getIn().getMandatoryBody(ShsMessageEntry.class);
        Assert.assertNotNull(entry);
    }


    @DirtiesContext
    @Test
    public void sendingAsynchMessageInVmShouldReturnCorrectHeaders() throws Exception {

        ShsMessage shsMessageEntry = make(createAsynchMessageWithKnownReceiver());

        Exchange exchange = camel.getDefaultEndpoint().createExchange(ExchangePattern.InOut);
        Message in = exchange.getIn();
        in.setBody(shsMessageEntry);

        System.out.println("label: " + shsMessageEntry.getLabel());

        Exchange response = camel.send("direct:in-vm", exchange);

        assertNotNull(response);

        Message out = response.getOut();

        assertEquals(out.getMandatoryBody(String.class), shsMessageEntry.getLabel().getTxId());
        assertEquals(out.getHeader(ShsHeaders.X_SHS_TXID), shsMessageEntry.getLabel().getTxId());
        assertEquals(out.getHeader(ShsHeaders.X_SHS_CORRID), shsMessageEntry.getLabel().getCorrId());
        assertEquals(out.getHeader(ShsHeaders.X_SHS_CONTENTID), shsMessageEntry.getLabel().getContent().getContentId());
        assertEquals(out.getHeader(ShsHeaders.X_SHS_DUPLICATEMSG), "no");
        assertNotNull(out.getHeader(ShsHeaders.X_SHS_LOCALID));
        assertNotNull(out.getHeader(ShsHeaders.X_SHS_ARRIVALDATE));
        assertNotNull(TimestampConverter.stringToDate(out.getHeader(ShsHeaders.X_SHS_ARRIVALDATE, String.class)));
        Assert.assertNull(out.getHeader(ShsHeaders.X_SHS_ERRORCODE));
    }

    @DirtiesContext
    @Test
    public void sendingAsynchMessageShouldReturnCorrectHeaders() throws Exception {

        ShsMessage testMessage = make(createAsynchMessageWithKnownReceiver());

        given(messageLogService.saveMessageStream(any(se.inera.axel.shs.xml.label.ShsLabel.class), any(InputStream.class)))
                .willReturn(make(a(ShsMessageEntryMaker.ShsMessageEntry,
                    with(ShsMessageEntryMaker.ShsMessageEntryInstantiator.label, testMessage.getLabel()))));

        Exchange exchange = camel.getDefaultEndpoint().createExchange(ExchangePattern.InOut);
        Message in = exchange.getIn();
        in.setBody(testMessage);

        System.out.println("label: " + testMessage.getLabel());

        Exchange response = camel.send("direct:in-http", exchange);

        assertNotNull(response);

        Message out = response.getOut();

        assertEquals(out.getMandatoryBody(String.class), testMessage.getLabel().getTxId());
        assertEquals(out.getHeader(ShsHeaders.X_SHS_TXID), testMessage.getLabel().getTxId());
        assertEquals(out.getHeader(ShsHeaders.X_SHS_CORRID), testMessage.getLabel().getCorrId());
        assertEquals(out.getHeader(ShsHeaders.X_SHS_CONTENTID), testMessage.getLabel().getContent().getContentId());
        assertEquals(out.getHeader(ShsHeaders.X_SHS_DUPLICATEMSG), "no");
        assertNotNull(out.getHeader(ShsHeaders.X_SHS_LOCALID));
        assertNotNull(out.getHeader(ShsHeaders.X_SHS_ARRIVALDATE));
        assertNotNull(TimestampConverter.stringToDate(out.getHeader(ShsHeaders.X_SHS_ARRIVALDATE, String.class)));
        Assert.assertNull(out.getHeader(ShsHeaders.X_SHS_ERRORCODE));
    }

    @DirtiesContext
    @Test
    public void sendingAsynchDuplicateMessageShouldFail() throws Exception {
        // a "fail" is currently specified as information in a label.
        ShsMessage testMessage = make(createAsynchDuplicateMessage());

        given(messageLogService.saveMessageStream(any(se.inera.axel.shs.xml.label.ShsLabel.class), any(InputStream.class)))
                .willThrow(
                        new MessageAlreadyExistsException(testMessage.getLabel(),
                        TimestampConverter.stringToDate(MockConfig.DUPLICATE_TIMESTAMP)));

        Exchange exchange = camel.getDefaultEndpoint().createExchange(ExchangePattern.InOut);
        Message in = exchange.getIn();
        in.setBody(testMessage);

        Exchange response = camel.send("direct:in-http", exchange);

        assertNotNull(response);

        Message out = response.getOut();

        assertEquals(out.getMandatoryBody(String.class), testMessage.getLabel().getTxId());
        assertEquals(out.getHeader(ShsHeaders.X_SHS_TXID), testMessage.getLabel().getTxId());
        assertEquals(out.getHeader(ShsHeaders.X_SHS_TXID), MockConfig.DUPLICATE_TX_ID);
        assertEquals(out.getHeader(ShsHeaders.X_SHS_CORRID), testMessage.getLabel().getCorrId());
        assertEquals(out.getHeader(ShsHeaders.X_SHS_CONTENTID), testMessage.getLabel().getContent().getContentId());
        assertEquals(out.getHeader(ShsHeaders.X_SHS_DUPLICATEMSG), "yes");
        assertEquals(out.getHeader(ShsHeaders.X_SHS_ARRIVALDATE), MockConfig.DUPLICATE_TIMESTAMP);
        Assert.assertNull(out.getHeader(ShsHeaders.X_SHS_ERRORCODE));
    }

    private Maker<ShsMessage> createAsynchMessageWithKnownReceiver() {
        return a(ShsMessage,
                with(label, createAsynchMessageLabelWithKnownReceiver()));
    }

    private Maker<se.inera.axel.shs.xml.label.ShsLabel> createAsynchMessageLabelWithKnownReceiver() {
        return a(ShsLabel,
                with(transferType, TransferType.ASYNCH));
    }

    private Maker<ShsMessage> createAsynchDuplicateMessage() {
        return a(ShsMessage,
                with(label, a(ShsLabel,
                        with(txId, MockConfig.DUPLICATE_TX_ID),
                        with(transferType, TransferType.ASYNCH))));
    }

    private Maker<ShsMessage> createSynchMessageWithKnownReceiver() {
        return a(ShsMessage,
                with(label, a(ShsLabel,
                        with(transferType, TransferType.SYNCH))));
    }

    private Maker<ShsMessage> createSynchMessageWithUnknownReceiver() {
        return a(ShsMessage,
                with(label, a(ShsLabel,
                        with(transferType, TransferType.SYNCH),
                        with(to, a(To,
                                with(value, "1111111111"))))));
    }

}
