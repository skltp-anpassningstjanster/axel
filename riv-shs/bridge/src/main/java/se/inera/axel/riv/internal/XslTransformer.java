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
	
	private String xsltLocation;
	public void setXsltLocation(String xsltLocation) {
		this.xsltLocation = xsltLocation;
		if(this.xsltLocation != null) {
			this.xsltLocation = this.xsltLocation + "/";
			this.xsltLocation = this.xsltLocation.replace("//", "/").replace("file:", "");
		}
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		
		String productId = (String) exchange.getIn().getHeader(ShsHeaders.PRODUCT_ID);
		log.info("Looking for xsl-file for {} in {}", productId, xsltLocation);
		
		if(xsltLocation == null) {
			log.info("No location for xslt-files is defined");
			return;
		}
			
		String xslFileName = xsltLocation + productId + ".xsl";
		File xslFile = new File(xslFileName);

		if(xslFile.exists()) {
			log.info("Found xslt-file {}. Will make transformation.", xslFileName);
			String payload = transform(exchange.getIn().getBody(String.class), xslFile);			
			exchange.getIn().setBody(payload);			
		} else
			log.info("The xslt-file {} was not found. No transformation is made.", xslFileName);		
	}

	private String transform(String s, File xslStream) throws IOException, URISyntaxException, TransformerException {
	    Transformer transformer = factory.newTransformer(new StreamSource(xslStream));

	    StringWriter outWriter = new StringWriter();
	    transformer.transform(new StringSource(s), new StreamResult(outWriter));
		return outWriter.toString();
	}

}
