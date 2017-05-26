package com.flickr4java.flickr;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFault;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.flickr4java.flickr.util.XMLUtilities;

/**
 * Flickr SOAP Response object.
 *
 * @author Matt Ray
 */
public class SOAPResponse implements Response {

    private static Logger _log = Logger.getLogger(SOAPResponse.class);

    private List<Element> payload;

    private String errorCode;

    private String errorMessage;

    private final SOAPEnvelope envelope;

    public SOAPResponse(SOAPEnvelope envelope) {
        this.envelope = envelope;
    }

    public void parse(Document document) {
        try {
            SOAPBody body = envelope.getBody();

            if (Flickr.debugStream) {
                _log.debug("SOAP RESPONSE.parse");
                _log.debug(body.toString());
            }

            SOAPFault fault = body.getFault();
            if (fault != null) {
                _log.warn("FAULT: " + fault.getFaultString());
                errorCode = fault.getFaultCode();
                errorMessage = fault.getFaultString();
            } else {
                for (@SuppressWarnings("unchecked")
                Iterator<Element> i = body.getChildElements(); i.hasNext();) {
                    Element bodyelement = i.next();
                    bodyelement.normalize();
                    // TODO: Verify that the payload is always a single XML node
                    payload = (List<Element>) XMLUtilities.getChildElements(bodyelement);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getStat() {
        return null;
    }

    public Element getPayload() {
        if (payload.isEmpty()) {
            throw new RuntimeException("SOAP response payload has no elements");
        }
        return payload.get(0);
    }

    public Collection<Element> getPayloadCollection() {
        return payload;
    }

    public boolean isError() {
        return errorCode != null;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
