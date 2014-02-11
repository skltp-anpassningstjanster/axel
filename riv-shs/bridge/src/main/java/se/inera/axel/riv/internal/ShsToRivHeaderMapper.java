package se.inera.axel.riv.internal;

import org.apache.camel.Body;
import org.apache.camel.Property;
import org.apache.camel.component.cxf.CxfPayload;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.headers.Header;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.ws.addressing.Names;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.label.ShsLabel;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.util.List;

/**
 * Performs the mapping from SHS to RIV headers.
 *
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class ShsToRivHeaderMapper {
    private static final JAXBDataBinding STRING_JAXB_BINDING;

    private static final QName RIV_LOGICAL_ADDRESS_QNAME = new QName("urn:riv:itintegration:registry:1", "LogicalAddress");

    static {
        try {
            STRING_JAXB_BINDING = new JAXBDataBinding(String.class);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    public CxfPayload<SoapHeader> addRivHeaders(@Body CxfPayload<SoapHeader> body,
                                                @Property(ShsHeaders.LABEL) ShsLabel label) {
        List<SoapHeader> soapHeaders = body.getHeaders();

        if (label == null) {
            throw new IllegalArgumentException("Missing " + ShsHeaders.LABEL + " property");
        }

        SoapHeader toHeader = createStringSoapHeader(RIV_LOGICAL_ADDRESS_QNAME, rivRecipient(label));
        toHeader.setDirection(Header.Direction.DIRECTION_OUT);

        soapHeaders.add(toHeader);

        return body;
    }

    /**
     * Fetch the RIV recipient from the label. If end recipient is set it is used
     * otherwise to fall backs to the to organisation of the label.
     *
     * @param label the label
     * @return the RIV to address
     */
    private String rivRecipient(ShsLabel label) {
        String to = null;

        if (label.getEndRecipient() != null)
            to = label.getEndRecipient().getValue();


        if (StringUtils.isEmpty(to))
            to = label.getTo().getValue();
        return to;
    }

    private SoapHeader createStringSoapHeader(QName qname, String to) {
        JAXBElement<String> toElement = new JAXBElement<>(qname,
                String.class,
                to);

        return new SoapHeader(toElement.getName(),
                toElement,
                STRING_JAXB_BINDING);
    }
}