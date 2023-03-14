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
package se.inera.axel.riv.internal;

import org.apache.camel.ExchangeProperty;
import org.apache.camel.Header;
import org.apache.commons.lang3.StringUtils;
import se.inera.axel.riv.RivShsMappingService;
import se.inera.axel.riv.RivShsServiceMapping;
import se.inera.axel.riv.RivShsServiceMappingRepository;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.label.ShsLabel;

import javax.annotation.Resource;


public class RepositoryRivShsMappingService implements RivShsMappingService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RepositoryRivShsMappingService.class);

	@Resource
	RivShsServiceMappingRepository repository;
	
	/* (non-Javadoc)
	 * @see se.inera.axel.riv.mongo.RivShsMappingService#mapRivServiceToShsProduct(java.lang.String)
	 */
	@Override
	public String mapRivServiceToShsProduct(@Header(RivShsMappingService.HEADER_SOAP_ACTION) String rivServiceNamespace, @ExchangeProperty(ShsHeaders.TO) String logicalAddress) {
		
		log.debug("mapRivServiceToShsProduct({})", rivServiceNamespace);
		
		RivShsServiceMapping mapping = findByRivServiceNamespaceAndlogicalAddress(rivServiceNamespace, logicalAddress);

		return mapping.getShsProductId();
	}

	@Override
	public String mapRivServiceToRivEndpoint(@Header(RivShsMappingService.HEADER_SOAP_ACTION) String rivServiceNamespace, @ExchangeProperty(ShsHeaders.TO) String logicalAddress) {
		
		log.debug("mapRivServiceToRivEndpoint({})", rivServiceNamespace);
		
		RivShsServiceMapping mapping = findByRivServiceNamespaceAndlogicalAddress(rivServiceNamespace, logicalAddress);

		return mapping.getRivServiceEndpoint();
	}
	
	@Override
	public String mapShsProductToRivService(@ExchangeProperty(ShsHeaders.LABEL) ShsLabel shsLabel) {
		String productId = shsLabel.getProduct().getValue();
		log.debug("mapShsProductToRivService({})", productId);
		
		RivShsServiceMapping mapping = findByShsProductId(productId);
		
		if (mapping == null) {
			throw new RuntimeException("No RIV Service found for SHS ProductId: " + productId);
		}
		
		return mapping.getRivServiceNamespace();
	}

    @Override
	public String mapRivServiceToXslScript(@Header(RivShsMappingService.HEADER_SOAP_ACTION) String rivServiceNamespace, @ExchangeProperty(ShsHeaders.TO) String logicalAddress) {
		log.debug("mapShsProductToXslScript({})", rivServiceNamespace);
		
		RivShsServiceMapping mapping = findByRivServiceNamespaceAndlogicalAddress(rivServiceNamespace, logicalAddress);

		String response = mapping.getXslScript();
		return StringUtils.isEmpty(response) ? null : response;
	}

    @Override
	public Boolean mapRivServiceToUseBOM(@Header(RivShsMappingService.HEADER_SOAP_ACTION) String rivServiceNamespace, @ExchangeProperty(ShsHeaders.TO) String logicalAddress) {
		log.debug("mapShsProductToXslScript({})", rivServiceNamespace);
		
		RivShsServiceMapping mapping = findByRivServiceNamespaceAndlogicalAddress(rivServiceNamespace, logicalAddress);

		Boolean response = mapping.getUseBOM();
		return response == null ? Boolean.FALSE : response;
	}

    @Override
	public Boolean mapRivServiceToUseCrLf(@Header(RivShsMappingService.HEADER_SOAP_ACTION) String rivServiceNamespace, @ExchangeProperty(ShsHeaders.TO) String logicalAddress) {
		log.debug("mapShsProductToXslScript({})", rivServiceNamespace);
		
		RivShsServiceMapping mapping = findByRivServiceNamespaceAndlogicalAddress(rivServiceNamespace, logicalAddress);
		
		Boolean response = mapping.getUseWindowsCRLF();
		return response == null ? Boolean.FALSE : response;
	}

    @Override
    public String mapRivServiceToResponseBody(@Header(RivShsMappingService.HEADER_SOAP_ACTION) String rivServiceNamespace, @ExchangeProperty(ShsHeaders.TO) String logicalAddress) {
        log.debug("mapRivServiceToResponseBody({})", rivServiceNamespace);

		RivShsServiceMapping mapping = findByRivServiceNamespaceAndlogicalAddress(rivServiceNamespace, logicalAddress);

        String response = mapping.getAsynchronousResponseSoapBody();

        return StringUtils.isEmpty(response) ? null : response;
    }

    @Override
    public Boolean useAsynchronousShs(@Header(RivShsMappingService.HEADER_SOAP_ACTION) String rivServiceNamespace, @ExchangeProperty(ShsHeaders.TO) String logicalAddress) {
        log.debug("useAsynchronousShs({})", rivServiceNamespace);

        RivShsServiceMapping mapping = findByRivServiceNamespaceAndlogicalAddress(rivServiceNamespace, logicalAddress);

        return mapping.getUseAsynchronousShs();
    }

	@Override
	public String mapRivShsFileNameTemplate(@Header(RivShsMappingService.HEADER_SOAP_ACTION) String rivServiceNamespace, @ExchangeProperty(ShsHeaders.TO) String logicalAddress) {
		
		log.debug("mapRivServiceToRivEndpoint({})", rivServiceNamespace);
		
		RivShsServiceMapping mapping = findByRivServiceNamespaceAndlogicalAddress(rivServiceNamespace, logicalAddress);

		String fileNameTemplate = mapping.getFileNameTemplate();
		if(fileNameTemplate == null || fileNameTemplate.trim().length() == 0)
			fileNameTemplate = "req-.xml";
		
		return fileNameTemplate;
	}
	
	@Override
	public String mapRivShsLabelStatus(@Header(RivShsMappingService.HEADER_SOAP_ACTION) String rivServiceNamespace, @ExchangeProperty(ShsHeaders.TO) String logicalAddress) {
	    
		log.debug("mapRivServiceToRivEndpoint({})", rivServiceNamespace);
		
		RivShsServiceMapping mapping = findByRivServiceNamespaceAndlogicalAddress(rivServiceNamespace, logicalAddress);
				
		String labelStatus = mapping.getLabelStatus();
		if(labelStatus == null || labelStatus.trim().length() == 0)
			labelStatus = DEFAULT_LABEL_STATUS;
		
		return labelStatus;
	}
	    
    @SuppressWarnings("unused")
	private RivShsServiceMapping findByRivServiceNamespace(String rivServiceNamespace) {
		return repository.findByRivServiceNamespace(clean(rivServiceNamespace));
	}

    private RivShsServiceMapping findByRivServiceNamespaceAndlogicalAddress(String rivServiceNamespace, String logicalAddress) {
		RivShsServiceMapping mapping = repository.findByRivServiceNamespaceAndLogicalAddress(clean(rivServiceNamespace), clean(logicalAddress));
		if (mapping == null) {
            throw new RuntimeException("No mapping found for RIV Service " + rivServiceNamespace +" and logical address " + logicalAddress);
		}
		return mapping;
	}

	private RivShsServiceMapping findByShsProductId(String shsProductId) {
		return repository.findByShsProductId(clean(shsProductId));
	}

	private String clean(String s) {
		if(s == null)
			return "";
		return StringUtils.remove(s, '"').trim();
	}
}
