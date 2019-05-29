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
package se.inera.axel.shs.broker.messagestore;

import org.apache.commons.lang3.SerializationUtils;
import se.inera.axel.shs.xml.label.ShsLabel;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Entity that represents an SHS message as routed in the broker and
 * stored in the message log database.
 * <p/>
 * Contains the message header (label), id and state.
 */
public class ShsMessageEntry implements Serializable {


    private String id;

    private ShsLabel label;

    private MessageState state;

    private Date stateTimeStamp;

    private Date arrivalTimeStamp;

    private String statusCode;

    private String statusText;

    private int retries;

    private boolean acknowledged;
    
    private boolean archived;

    public ShsMessageEntry() {

    }

	public static ShsMessageEntry newInstance(ShsMessageEntry shsMessageEntry) {
		return (ShsMessageEntry) SerializationUtils.clone(shsMessageEntry);
	}

    public ShsMessageEntry(String id) {
        setId(id);
    }

    public ShsMessageEntry(String id, ShsLabel label) {
        setLabel(label);
        setId(id);
    }

    public ShsMessageEntry(ShsLabel label) {
        setLabel(label);
        setId(UUID.randomUUID().toString());
    }


    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public ShsLabel getLabel() {
        return label;
    }

    public void setLabel(ShsLabel label) {
        this.label = label;
    }

    public MessageState getState() {
        return state;
    }

    public void setState(MessageState state) {
        this.state = state;
    }

    public Date getStateTimeStamp() {
        return stateTimeStamp;
    }

    public void setStateTimeStamp(Date stateTimeStamp) {
        this.stateTimeStamp = stateTimeStamp;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public Date getArrivalTimeStamp() {
        return arrivalTimeStamp;
    }

    public void setArrivalTimeStamp(Date arrivalTimeStamp) {
        this.arrivalTimeStamp = arrivalTimeStamp;
    }

    public static ShsMessageEntry createNewEntry(ShsLabel label) {
        return new ShsMessageEntry(label);
    }

    @Override
    public String toString() {
        return "ShsMessageEntry{" +
                "id='" + id + '\'' +
                ", label=" + label +
                ", state=" + state +
                ", stateTimeStamp=" + stateTimeStamp +
                ", statusCode=" +statusCode +
                ", statusText=" +statusText +
                ", arrivalTimeStamp=" + arrivalTimeStamp +
                ", retries=" + retries +
                '}';
    }

}
