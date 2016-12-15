package org.wso2.carbon.identity.entitlement.xacml.core.dto;

import java.util.Arrays;

/**
 * encapsulates the policy data that is stored in the policy store
 */
public class PolicyStoreDTO {

    private String policyId;

    private String policy;

    private int policyOrder;

    private boolean active;

    private String policyType;

    private int version;

    private AttributeDTO[] attributeDTOs = new AttributeDTO[0];

    private String[] policySetIdReferences = new String[0];

    private String[] policyIdReferences = new String[0];

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public int getPolicyOrder() {
        return policyOrder;
    }

    public void setPolicyOrder(int policyOrder) {
        this.policyOrder = policyOrder;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public AttributeDTO[] getAttributeDTOs() {
        return Arrays.copyOf(attributeDTOs, attributeDTOs.length);
    }

    public void setAttributeDTOs(AttributeDTO[] attributeDTOs) {
        this.attributeDTOs = Arrays.copyOf(attributeDTOs, attributeDTOs.length);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String[] getPolicySetIdReferences() {
        return Arrays.copyOf(policySetIdReferences, policySetIdReferences.length);
    }

    public void setPolicySetIdReferences(String[] policySetIdReferences) {
        this.policySetIdReferences = Arrays.copyOf(policySetIdReferences, policySetIdReferences.length);
    }

    public String[] getPolicyIdReferences() {
        return Arrays.copyOf(policyIdReferences, policyIdReferences.length);
    }

    public void setPolicyIdReferences(String[] policyIdReferences) {
        this.policyIdReferences = Arrays.copyOf(policyIdReferences, policyIdReferences.length);
    }

    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }

    public String getPolicyType() {
        return policyType;
    }
}
