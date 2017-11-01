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
	
	private String xsltLocation;
	public void setXsltLocation(String xsltLocation) {
		this.xsltLocation = xsltLocation;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		
		if(xsltLocation == null)
			return;
		
		String xslFileName = xsltLocation + (String) exchange.getIn().getHeader(ShsHeaders.PRODUCT_ID) + ".xsl";
		File xslFile = new File(xslFileName);

		if(xslFile.exists()) {
			String payload = transform(exchange.getIn().getBody(String.class), xslFile);			
			exchange.getIn().setBody(payload);			
		}
		
	}

	private String transform(String s, File xslStream) throws IOException, URISyntaxException, TransformerException {
	    Transformer transformer = factory.newTransformer(new StreamSource(xslStream));

	    StringWriter outWriter = new StringWriter();
	    transformer.transform(new StringSource(s), new StreamResult(outWriter));
		return outWriter.toString();
	}

	
}
