package se.inera.axel.riv.internal;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.StringSource;

import se.inera.axel.shs.processor.ShsHeaders;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;

/**
 * This class performs a custom XSL-transformation of the payload of the incoming message.
 * Included is also a separate transformation of newlines and the addition of a UTF-8 BOM (Byte order mark)
 * since these are difficult to do with XSLT.
 * 
 * @author hanwik
 *
 */
public class XslTransformer implements Processor {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XslTransformer.class);
	private static TransformerFactory factory = TransformerFactory.newInstance();
	
	private static final char BOM = '\ufeff';

	@Override
	public void process(Exchange exchange) throws Exception {

		String status = (String) exchange.getIn().getHeader(ShsHeaders.STATUS);
		String productId = (String) exchange.getIn().getHeader(ShsHeaders.PRODUCT_ID);
		
		String xslScript = (String) exchange.getProperty(ShsHeaders.X_SHS_XSL);
		Boolean useXsl = isTrue(xslScript);
		Boolean useBOM = isTrue((Boolean)exchange.getProperty(ShsHeaders.X_SHS_USE_BOM));
		Boolean useCrlf = isTrue((Boolean)exchange.getProperty(ShsHeaders.X_SHS_CRLF));
		
		/* 
		 * Check if any transformation should be done. If so log information about it.
		 * Otherwise just return.
		 */
		if(useXsl || useBOM || useCrlf) {
			log.info("The following transformations will be made for message with product {}"
					+ ": XSLT={}"
					+ ", UTF8BOM={} "
					+ ", ToCRLF={}", new Object[]{productId, useXsl, useBOM, useCrlf});
		} else {
			return;
		}

		/*
		 * Do the transformations.
		 */
		String payload = exchange.getIn().getBody(String.class);
		
		if (useXsl) {
			payload = transform(payload, xslScript);
		}
		if(useCrlf) {
			payload = convertToCrLf(payload);
		}
		if(useBOM) {
			payload = addBom(payload);
		}
		
		/*
		 * Update the payload in the exchange.
		 */
		if(payload != null) {
						
			exchange.getIn().setBody(payload);
			
			// Log transformed payload i test environment
			if("test".equalsIgnoreCase(status))
				log.info("transformed payload is:" + payload);
		}
	}

	@SuppressWarnings("unused")
	private String transform(String s, File xslStream) throws IOException, URISyntaxException, TransformerException {
	    Transformer transformer = factory.newTransformer(new StreamSource(xslStream));
	    transformer.setOutputProperty(OutputKeys.INDENT, "no");
	    return transform(transformer, s);
	}

	private String transform(String s, String xslString) throws IOException, URISyntaxException, TransformerException {
		if(s == null)
			return null;
		
	    Transformer transformer = factory.newTransformer(new StringSource(xslString));
	    transformer.setOutputProperty(OutputKeys.INDENT, "no");

	    return transform(transformer, s);
	}

	private String transform(Transformer transformer, String s) throws IOException, URISyntaxException, TransformerException {

	    StringWriter outWriter = new StringWriter();
	    transformer.transform(new StringSource(s), new StreamResult(outWriter));
		return outWriter.toString();
	}

	private boolean isTrue(String s) {
		if(s == null || s.trim().length() == 0)
			return false;
		else
			return true;
	}
	
	private boolean isTrue(Boolean b) {
		if(b == null)
			return false;
		else
			return b;
	}
	
	private String addBom(String s) {
		if(s == null)
			return null;
		else
			return BOM + s;
	}
	
	private String convertToCrLf(String s) {
		if(s == null)
			return null;
		else
			return s.replace("\n", "\r\n");
	}
}
