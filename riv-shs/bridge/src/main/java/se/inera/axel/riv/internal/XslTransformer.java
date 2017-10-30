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
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;


public class XslTransformer implements Processor {

	private static TransformerFactory factory = TransformerFactory.newInstance();

	@Override
	public void process(Exchange exchange) throws Exception {
		
		String xslFileName = "xslt/" + (String) exchange.getIn().getHeader(ShsHeaders.PRODUCT_ID) + ".xsl";
		InputStream xslFile = ClassLoader.getSystemClassLoader().getResourceAsStream(xslFileName);
		
		if(xslFile != null) {
			String payload = transform(exchange.getIn().getBody(String.class), xslFile);			
			exchange.getIn().setBody(payload);			
		}
		
	}

	private String transform(String s, InputStream xslStream) throws IOException, URISyntaxException, TransformerException {
	    Transformer transformer = factory.newTransformer(new StreamSource(xslStream));

	    StringWriter outWriter = new StringWriter();
	    transformer.transform(new StringSource(s), new StreamResult(outWriter));
		return outWriter.toString();
	}

	
}
