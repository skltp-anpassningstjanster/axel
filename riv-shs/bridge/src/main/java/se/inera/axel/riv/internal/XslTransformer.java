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


public class XslTransformer implements Processor {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XslTransformer.class);
	private static TransformerFactory factory = TransformerFactory.newInstance();
	

	@Override
	public void process(Exchange exchange) throws Exception {

		String status = (String) exchange.getIn().getHeader(ShsHeaders.STATUS);
		String productId = (String) exchange.getIn().getHeader(ShsHeaders.PRODUCT_ID);
		
		String xslScript = (String) exchange.getProperty("AxelXslScript");
		String payload = null;
		
		if (xslScript != null) {
			log.info("Found xslt-script for product {}. Will make transformation.", productId);
			payload = transform(exchange.getIn().getBody(String.class), xslScript);
		}
		
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

	    return transform(transformer, s);
	}

	private String transform(String s, String xslString) throws IOException, URISyntaxException, TransformerException {
	    Transformer transformer = factory.newTransformer(new StringSource(xslString));

	    return transform(transformer, s);
	}

	private String transform(Transformer transformer, String s) throws IOException, URISyntaxException, TransformerException {

	    StringWriter outWriter = new StringWriter();
	    transformer.transform(new StringSource(s), new StreamResult(outWriter));
		return outWriter.toString();
	}

}
