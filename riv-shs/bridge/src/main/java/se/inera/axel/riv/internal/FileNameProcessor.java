package se.inera.axel.riv.internal;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.apache.camel.spi.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.axel.riv.RivShsMappingService;
import se.inera.axel.shs.processor.ShsHeaders;

public class FileNameProcessor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(FileNameProcessor.class);

    private static final String DEFAULT_FILENAME = "req-.xml";
	
    final static String HEADER_FILENAME = String.format("req-${in.header.%s}.xml", RivShsMappingService.HEADER_RIV_CORRID);
    
	@Override
	public void process(Exchange exchange) throws Exception {
		CamelContext context = exchange.getContext();

		Language language = context.resolveLanguage("simple");
		
		String value;
		String fileNameTemplate = exchange.getProperty(ShsHeaders.X_SHS_FILETEMPLATE, String.class);
		if(fileNameTemplate == null || fileNameTemplate.length()==0)
			value = DEFAULT_FILENAME;
		else {
			Expression expression; 
			try {
				expression = language.createExpression(fileNameTemplate); 
				value = expression.evaluate(exchange, String.class); 
			} catch (RuntimeException e) {
				log.warn("Can not parse filename template {}. Will use default name. Error is {}", fileNameTemplate, e.getMessage());
				value = DEFAULT_FILENAME;
			}
		}
		exchange.getIn().setHeader(ShsHeaders.DATAPART_FILENAME, value);
	}
}