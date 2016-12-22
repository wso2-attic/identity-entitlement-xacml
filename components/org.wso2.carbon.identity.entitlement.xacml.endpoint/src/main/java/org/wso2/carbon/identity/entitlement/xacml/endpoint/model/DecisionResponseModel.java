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
        "response"
})
@JsonPropertyOrder({
        "response"
})
@XmlRootElement(name = "decisionResponse")
public class DecisionResponseModel {

    @XmlElement(required = false)
    private String response;


    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
