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
package se.inera.axel.shs.broker.ds.internal;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.MessageState;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntryMaker;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntryMaker.ShsMessageEntryInstantiator;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.mime.ShsMessageMaker;
import se.inera.axel.shs.xml.label.*;
import se.inera.axel.shs.xml.label.ShsLabelMaker.EndRecipientInstantiator;
import se.inera.axel.shs.xml.label.ShsLabelMaker.ToInstantiator;

import java.util.ArrayList;
import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessageInstantiator.label;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.*;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.*;

/**
 * @author Björn Bength, bjorn.bength@r2m.se
 */
@Configuration
public class MockConfig {

    public static final String SUBJECT_WITH_SPECIAL_CHARS = "subject_åäö";

    @Mock
    MessageLogService messageLogService;


    public MockConfig() {
        MockitoAnnotations.initMocks(this);
    }

    @SuppressWarnings("unchecked")
	@Bean
    public MessageLogService messageLogService() {
        final List<ShsMessageEntry> entries = new ArrayList<ShsMessageEntry>();

        ShsLabel label1 = make(a(ShsLabel,
                with(endRecipient, a(EndRecipient, with(EndRecipientInstantiator.value,
                        ShsLabelMaker.DEFAULT_TEST_ENDRECIPIENT))),
                with(subject, SUBJECT_WITH_SPECIAL_CHARS),
                with(transferType, TransferType.SYNCH)));
        History history = new History();
        history.setFrom("from");
        label1.getHistory().add(history);

        label1.getOriginatorOrFrom().clear();

        Originator originator = new Originator();
        originator.setLabeledURI("0_LabelURI");
        originator.setName("0_Name");
        originator.setValue("0000000000");
        label1.getOriginatorOrFrom().add(originator);
        
        From from = new From();
        from.setCommonName("1_CommonName");
        from.setEMail("1_EMail");
        from.setLabeledURI("1_LabelURI");
        from.setValue("11111111111");
        label1.getOriginatorOrFrom().add(from);
        
        entries.add(make(a(ShsMessageEntryMaker.ShsMessageEntry,
                with(ShsMessageEntryInstantiator.state, MessageState.RECEIVED),
                with(ShsMessageEntryInstantiator.label, label1))));
        
        entries.add(make(a(ShsMessageEntryMaker.ShsMessageEntry,
                with(ShsMessageEntryInstantiator.state, MessageState.RECEIVED),
                with(ShsMessageEntryInstantiator.label, a(ShsLabel,
                        with(endRecipient, a(EndRecipient, with(EndRecipientInstantiator.value,
                                ShsLabelMaker.DEFAULT_TEST_ENDRECIPIENT))),
                        with(transferType, TransferType.ASYNCH))))));

        entries.add(make(a(ShsMessageEntryMaker.ShsMessageEntry,
                with(ShsMessageEntryInstantiator.state, MessageState.RECEIVED),
                with(ShsMessageEntryInstantiator.label, a(ShsLabel,
                        with(to, a(To, with(ToInstantiator.value, ShsLabelMaker.DEFAULT_TEST_TO))),
                        with(transferType, TransferType.ASYNCH))))));

        entries.add(make(a(ShsMessageEntryMaker.ShsMessageEntry,
                with(ShsMessageEntryInstantiator.state, MessageState.RECEIVED),
                with(ShsMessageEntryInstantiator.label, a(ShsLabel,
                        with(to, a(To, with(ToInstantiator.value, ShsLabelMaker.DEFAULT_TEST_TO))),
                        with(transferType, TransferType.ASYNCH))))));


        given(messageLogService.listMessages(any(String.class), any(MessageLogService.Filter.class)))
                .willAnswer(new Answer<Iterable<ShsMessageEntry>>() {
                    @Override
                    public Iterable<ShsMessageEntry> answer(InvocationOnMock invocation) throws Throwable {
                        String shsTo = (String)invocation.getArguments()[0];
                        if (shsTo == null) {
                            throw new IllegalArgumentException("shsTo must be provided");
                        }

                        if (!shsTo.equals(ShsLabelMaker.DEFAULT_TEST_TO))
                            return new ArrayList<ShsMessageEntry>();

                        return entries;
                    }
                });

        given(messageLogService.loadEntry(any(String.class), any(String.class)))
        .willAnswer(new Answer<ShsMessageEntry>() {
            @Override
            public ShsMessageEntry answer(InvocationOnMock invocation) throws Throwable {

                for (ShsMessageEntry entry : entries) {
                    if (entry.getLabel().getTxId().equals((String)invocation.getArguments()[1])) {
                        return entry;
                    }
                }

                return null;
            }
        });

        given(messageLogService.loadEntryAndLockForFetching(any(String.class), any(String.class)))
        .willAnswer(new Answer<ShsMessageEntry>() {
            @Override
            public ShsMessageEntry answer(InvocationOnMock invocation) throws Throwable {

                for (ShsMessageEntry entry : entries) {
                    if (entry.getLabel().getTxId().equals((String)invocation.getArguments()[1])) {
                    	if (entry.getState() == MessageState.RECEIVED) {
                    		entry.setState(MessageState.FETCHING_IN_PROGRESS);
                    		return entry;
                    	} else {
                    		return null;
                    	}
                    }
                }

                return null;
            }
        });


        given(messageLogService.loadMessage(any(ShsMessageEntry.class)))
        .willAnswer(new Answer<ShsMessage>() {
            @Override
            public ShsMessage answer(InvocationOnMock invocation) throws Throwable {

                for (ShsMessageEntry entry : entries) {
                    if (entry.getLabel().getTxId()
                            .equals(((ShsMessageEntry)invocation.getArguments()[0]).getLabel().getTxId())) {
                        return make(a(ShsMessageMaker.ShsMessage,
                                                with(label, entry.getLabel())));
                    }
                }

                return null;
            }
        });

        given(messageLogService.messageAcknowledged(any(ShsMessageEntry.class)))
        .willAnswer(new Answer<ShsMessageEntry>() {
            @Override
            public ShsMessageEntry answer(InvocationOnMock invocation) throws Throwable {
                for (ShsMessageEntry entry : entries) {
                    if (entry.getLabel().getTxId()
                            .equals(((ShsMessageEntry)invocation.getArguments()[0]).getLabel().getTxId())) {

                        entry.setAcknowledged(true);
                        return entry;
                    }
                }

                return null;

            }
        });

        return messageLogService;
    }

}
