package se.inera.axel.shs.broker.rs.internal;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.converter.stream.InputStreamCache;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.axel.shs.broker.messagestore.ShsMessageEntry;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.processor.ShsMessageMarshaller;
import se.inera.axel.shs.processor.TimestampConverter;
import se.inera.axel.shs.xml.label.*;


import java.io.IOException;
import java.util.List;

/**
 * Created by gorerk on 3/26/2018.
 */
public class MessageInfoLogger {
    private static final Logger LOG = LoggerFactory.getLogger(MessageInfoLogger.class);

    public void log(Exchange exchange, String messageType) throws Exception {
        try {
            Object body = exchange.getIn().getBody();
            if (body instanceof ShsMessage) {
                LOG.info(createMessageToLog(exchange, ((ShsMessage) body).getLabel(), messageType));
            } else if (body instanceof ShsMessageEntry) {
                LOG.info(createMessageToLog(exchange, ((ShsMessageEntry) body).getLabel(), messageType));
            }else if (body instanceof String) {
                LOG.info(createMessageToLog(exchange, (String)body, messageType));
            }else if(body instanceof InputStreamCache) {
                ShsMessageMarshaller shsMessageMarshaller = new ShsMessageMarshaller();
                byte[] buffer  = shsMessageMarshaller.getBytesFromInputStream((InputStreamCache)body);
                String bodyAsString = new String(buffer);
                if( bodyAsString.contains("<shs.label")) {
                    LOG.info(createMessageToLog(exchange, shsMessageMarshaller.unmarshalLabel(buffer), messageType));
                }else{
                    LOG.info(createMessageToLog(exchange, bodyAsString, messageType));
                }
            } else {
                LOG.info(createMessageToLog(exchange, (ShsLabel)null, messageType));
            }

        }catch(Exception e){
            LOG.info("Failed log message:"+messageType, e);
        }
    }

    public void log(Exchange exchange, ShsLabel label, String messageType) throws Exception {
        try {
            LOG.info(createMessageToLog(exchange, label, messageType));
        }catch(Exception e){
            LOG.info("Failed log message:"+messageType, e);
        }
    }

    private String createMessageToLog(Exchange exchange, ShsLabel label, String messageType){
        StrBuilder  logBuilder = new StrBuilder();
        logBuilder.appendln("LogMessage="+messageType);
        logBuilder.appendln("ExchangeId="+exchange.getExchangeId());

        addMessageHeaders(exchange.getIn(), logBuilder);
        addMessageLabel(label, logBuilder);

        return logBuilder.toString();
    }

    private String createMessageToLog(Exchange exchange, String message, String messageType){
        StrBuilder  logBuilder = new StrBuilder();
        logBuilder.appendln("LogMessage="+messageType);
        logBuilder.appendln("ExchangeId="+exchange.getExchangeId());

        addMessageHeaders(exchange.getIn(), logBuilder);
        logBuilder.appendln("Message.body="+message.trim());

        return logBuilder.toString();
    }

    private void addMessageHeaders(Message inMessage, StrBuilder logBuilder) {
        addHeaderIfExist(Exchange.HTTP_URI, inMessage, logBuilder);
        addHeaderIfExist(Exchange.HTTP_RESPONSE_CODE, inMessage, logBuilder);
        addHeaderIfExist(ShsHeaders.X_SHS_TXID, inMessage, logBuilder);
        addHeaderIfExist(ShsHeaders.X_SHS_CORRID, inMessage, logBuilder);
        addHeaderIfExist(ShsHeaders.X_SHS_CONTENTID, inMessage, logBuilder);
        addHeaderIfExist(ShsHeaders.X_SHS_DUPLICATEMSG, inMessage, logBuilder);
        addHeaderIfExist(ShsHeaders.X_SHS_LOCALID, inMessage, logBuilder);
        addHeaderIfExist(ShsHeaders.X_SHS_ARRIVALDATE, inMessage, logBuilder);
        addHeaderIfExist(ShsHeaders.X_SHS_ERRORCODE, inMessage, logBuilder);
        addHeaderIfExist(ShsHeaders.DESTINATION_URI, inMessage, logBuilder);
    }

    private void addHeaderIfExist(String headerName, Message inMessage, StrBuilder logBuilder) {
        Object header = inMessage.getHeader(headerName);
        if(header!=null) {
            logBuilder.appendln(String.format("Header.%s=%s", headerName, header.toString()));
        }
    }

    private void addMessageLabel(ShsLabel shsLabel, StrBuilder result){
        if(shsLabel==null){
            return;
        }

        result.appendln("Message.TxId=" + shsLabel.getTxId());
        if(shsLabel.getFrom()!=null){
            result.appendln("Message.From=" + String.format("%s (%s)", shsLabel.getFrom().getValue(),shsLabel.getFrom().getCommonName()));
        }
        if(shsLabel.getTo()!=null) {
            result.appendln("Message.To=" + String.format("%s (%s)", shsLabel.getTo().getValue(),shsLabel.getTo().getCommonName()));
        }
        result.appendln("Message.Originator=" + shsLabel.getOriginator());
        result.appendln("Message.EndRecipient=" + shsLabel.getEndRecipient());
        result.appendln("Message.CorrId=" + shsLabel.getCorrId());
        result.appendln("Message.SequenceType=" + shsLabel.getSequenceType());
        try {
            result.appendln("Message.Timestamp=" + TimestampConverter.dateToString(shsLabel.getDatetime()));
        } catch (Exception e) {
        }
        if(shsLabel.getProduct()!=null) {
            result.appendln("Message.Product=" + String.format("%s (%s)", shsLabel.getProduct().getValue(),shsLabel.getProduct().getCommonName()));
        }
        result.appendln("Message.Subject=" + shsLabel.getSubject());
        result.appendln("Message.TransferType=" + shsLabel.getTransferType());
        result.appendln("Message.MessageType=" + shsLabel.getMessageType());
        result.appendln("Message.Status=" + shsLabel.getStatus());
        result.appendln(getMetadataListAsText(shsLabel.getMeta()));
        result.appendln("Message.ShsAgreement=" + shsLabel.getShsAgreement());
        result.appendln("Message.Version=" + shsLabel.getVersion());
        result.appendln("Message.DocumentType=" + shsLabel.getDocumentType());
        result.append(getLabelContentAsText(shsLabel.getContent()));
        result.append(getHistoryAsText(shsLabel.getHistory()));
    }

    private String getHistoryAsText(List<History> historyList) {
        StrBuilder  result = new StrBuilder();
        if(historyList!=null){
            result.appendln("Message.History.noOfRecords="+historyList.size());
            for(History history: historyList) {
                result.appendln("Message.History.timestamp=" + history.getDatetime());
                result.appendln("Message.History.nodeId=" + history.getNodeId());
                result.appendln("Message.History.comment=" + history.getComment());
                result.appendln("Message.History.localId=" + history.getLocalId());
            }
        }
        return result.toString();
    }

    private String getMetadataListAsText(List<Meta> metaList) {
        StrBuilder  result = new StrBuilder();
        if(metaList!=null) {
            result.append("Message.Metadata=");
            for (Meta meta : metaList) {
                result.append(String.format("%s (%s),", meta.getValue(), meta.getName()));
            }
        }
        return result.toString();
    }

    private String getLabelContentAsText( Content content ) {
        StrBuilder  result = new StrBuilder();
        if(content!=null) {
            result.appendln("Message.Content.id=" + content.getContentId());
            result.appendln("Message.Content.comment=" + content.getComment());
            result.appendln("Message.Content.noDataParts=" + content.getDataOrCompound().size());
            for (Object dataOrCompound : content.getDataOrCompound()) {
                if (dataOrCompound instanceof Data) {
                    Data data = (Data) dataOrCompound;
                    result.appendln("Message.Content.data.filename=" + data.getFilename());
                    result.appendln("Message.Content.data.type=" + data.getDatapartType());
                    result.appendln("Message.Content.data.noOfBytes=" + data.getNoOfBytes());
                    result.appendln("Message.Content.data.NoOfRecords=" + data.getNoOfRecords());
                } else if (dataOrCompound instanceof Compound) {
                    Compound compund = (Compound) dataOrCompound;
                    result.appendln("Message.Content.compound.nrOfParts=" + compund.getNoOfParts());
                }
            }
        }
        return result.toString();
    }
}
