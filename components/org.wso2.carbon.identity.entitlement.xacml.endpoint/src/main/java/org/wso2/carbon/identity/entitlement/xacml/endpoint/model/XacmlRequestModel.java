package org.wso2.carbon.identity.entitlement.xacml.endpoint.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Model class representing resonse of request
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "request",
})
@JsonPropertyOrder({
        "request",
})
@XmlRootElement(name = "xacmlRequest")
public class XacmlRequestModel {

    @XmlElement(required = true)
    private String request;

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
}


