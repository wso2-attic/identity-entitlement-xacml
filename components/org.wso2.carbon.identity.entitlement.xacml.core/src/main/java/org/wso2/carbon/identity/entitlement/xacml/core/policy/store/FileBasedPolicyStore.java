package org.wso2.carbon.identity.entitlement.xacml.core.policy.store;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;

import java.util.HashMap;

/**
 * 
 */
@Component(
        name = "org.wso2.carbon.identity.entitlement.xacml.core.policy.store.FileBasedPolicyStore",
        immediate = true,
        service = PolicyStore.class
)
public class FileBasedPolicyStore extends PolicyStore {

    private HashMap<String, PolicyStoreDTO> policyStore = new HashMap<>();

    @Override
    public PolicyStoreDTO readPolicyDTO(String policyId) throws EntitlementException {
        return policyStore.get(policyId);
    }

    @Override
    public boolean isExistPolicy(String policyId) throws EntitlementException {
        return policyStore.containsKey(policyId);
    }

    @Override
    public PolicyStoreDTO[] readAllPolicyDTOs() throws EntitlementException {
        return policyStore.values().toArray(new PolicyStoreDTO[policyStore.size()]);
    }

    @Override
    public void removePolicy(String policyId) throws EntitlementException {
        policyStore.remove(policyId);
    }

    @Override
    protected void doAdd(PolicyStoreDTO policy) throws EntitlementException {
        policyStore.put(policy.getPolicyId(), policy);
    }
}
