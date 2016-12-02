package org.wso2.carbon.identity.entitlement.xacml.core.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.entitlement.xacml.core.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;

import java.util.HashMap;
import java.util.Objects;

/**
 *
 */
class InMemoryStorageManager implements StorageManager {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryStorageManager.class);

    private HashMap<String, PolicyStoreDTO> papStore = new HashMap<>();
    private HashMap<String, PolicyStoreDTO> pdpStore = new HashMap<>();

    @Override
    public void store(PolicyStoreDTO policy, String store) throws EntitlementException {

        if (Objects.equals(store, EntitlementConstants.PAP_STORE)) {
            logger.debug("saving policy in PAP : " + policy.getPolicyId());
            papStore.put(policy.getPolicyId(), policy);
        }
        if (Objects.equals(store, EntitlementConstants.PDP_STORE)) {
            pdpStore.put(policy.getPolicyId(), policy);
        }
    }

    @Override
    public PolicyStoreDTO read(String policyId, String store) throws EntitlementException {
        if (Objects.equals(store, EntitlementConstants.PAP_STORE)) {
            logger.debug("getting policy in PAP : " + policyId);
            return papStore.get(policyId);
        }
        if (Objects.equals(store, EntitlementConstants.PDP_STORE)) {
            return pdpStore.get(policyId);
        }
        throw new EntitlementException("invalid store name ", null);
    }

    @Override
    public void remove(String policyId, String store) throws EntitlementException {
        if (Objects.equals(store, EntitlementConstants.PAP_STORE)) {
            logger.debug("removing policy in PAP : " + policyId);
            papStore.remove(policyId);
            logger.debug("current policy in PAP : " + papStore.size());
        }
        if (Objects.equals(store, EntitlementConstants.PDP_STORE)) {
            pdpStore.remove(policyId);
        }
    }

    @Override
    public PolicyStoreDTO[] readAll(String store) throws EntitlementException {
        if (Objects.equals(store, EntitlementConstants.PAP_STORE)) {
            logger.debug("getting all policy in PAP ");
            return papStore.values().toArray(new PolicyStoreDTO[papStore.size()]);
        }
        if (Objects.equals(store, EntitlementConstants.PDP_STORE)) {
            return pdpStore.values().toArray(new PolicyStoreDTO[pdpStore.size()]);
        }
        throw new EntitlementException("invalid store name ", null);

    }
}
