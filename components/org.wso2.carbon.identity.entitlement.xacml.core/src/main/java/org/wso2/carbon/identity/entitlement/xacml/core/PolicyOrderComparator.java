package org.wso2.carbon.identity.entitlement.xacml.core;

import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyStoreDTO;

import java.io.Serializable;
import java.util.Comparator;

/**
 *
 */
public class PolicyOrderComparator implements Serializable, Comparator {

    private static final long serialVersionUID = -4125227115004608650L;

    @Override
    public int compare(Object o1, Object o2) {

        if (o1 instanceof PolicyStoreDTO && o2 instanceof PolicyStoreDTO) {
            PolicyStoreDTO dto1 = (PolicyStoreDTO) o1;
            PolicyStoreDTO dto2 = (PolicyStoreDTO) o2;
            if (dto1.getPolicyOrder() > dto2.getPolicyOrder()) {
                return -1;
            } else if (dto1.getPolicyOrder() == dto2.getPolicyOrder()) {
                return 0;
            } else {
                return 1;
            }
        } else if (o1 instanceof PolicyDTO && o2 instanceof PolicyDTO) {
            PolicyDTO dto1 = (PolicyDTO) o1;
            PolicyDTO dto2 = (PolicyDTO) o2;
            if (dto1.getPolicyOrder() > dto2.getPolicyOrder()) {
                return -1;
            } else if (dto1.getPolicyOrder() == dto2.getPolicyOrder()) {
                return 0;
            } else {
                return 1;
            }
        } else {
            throw new ClassCastException("PolicyOrderComparator only works for PolicyStoreDTO types");
        }
    }
}
