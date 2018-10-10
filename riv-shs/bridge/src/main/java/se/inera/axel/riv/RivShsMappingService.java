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
package se.inera.axel.riv;

import se.inera.axel.shs.xml.label.ShsLabel;


public interface RivShsMappingService {

	final static String HEADER_RIV_CORRID ="x-skltp-correlation-id";
	final static String USE_HEADER_RIV_CORRID ="x-use-skltp-correlation-id";
	final static String HEADER_SOAP_ACTION ="SOAPAction";
	final static String HEADER_FILENAME = String.format("req-${in.header.%s}.xml", HEADER_RIV_CORRID);
	final static String CONTENT_TYPE = "application/xml";
	final static String DEFAULT_LABEL_STATUS = "default";
	
	String mapRivServiceToShsProduct(String rivServiceNamespace, String logicalAddress);
	String mapShsProductToRivService(ShsLabel shsLabel);
	String mapRivServiceToRivEndpoint(String rivServiceNamespace, String logicalAddress);
    String mapRivServiceToResponseBody(String rivServiceNamespace, String logicalAddress);
    Boolean useAsynchronousShs(String rivServiceNamespace, String logicalAddress);
	String mapRivServiceToXslScript(String rivServiceNamespace, String logicalAddress);
	Boolean mapRivServiceToUseBOM(String rivServiceNamespace, String logicalAddress);
	Boolean mapRivServiceToUseCrLf(String rivServiceNamespace, String logicalAddress);
	String mapRivShsFileNameTemplate(String rivServiceNamespace, String logicalAddress);
	String mapRivShsLabelStatus(String rivServiceNamespace, String logicalAddress);

}