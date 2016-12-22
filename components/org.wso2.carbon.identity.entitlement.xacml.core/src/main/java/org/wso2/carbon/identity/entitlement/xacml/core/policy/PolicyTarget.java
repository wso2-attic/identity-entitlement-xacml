package org.wso2.carbon.identity.entitlement.xacml.core.policy;

import org.wso2.balana.AbstractTarget;

/**
 * This class encapsulates the policy target data with org.wso2.balana.Target and policyId
 */
public class PolicyTarget {

    private AbstractTarget target;

    private String policyId;

    public AbstractTarget getTarget() {
        return target;
    }

    public void setTarget(AbstractTarget target) {
        this.target = target;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    @Override
    public int hashCode() {
        if (this.target != null) {
            return target.encode().hashCode();
        } else {
            return 0;
        }
    }
}
