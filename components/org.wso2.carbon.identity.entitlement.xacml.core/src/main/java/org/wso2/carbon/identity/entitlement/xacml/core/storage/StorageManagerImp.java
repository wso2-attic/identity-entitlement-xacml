package org.wso2.carbon.identity.entitlement.xacml.core.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;

;

/**
 * Its a adopter class for Storage Manager. All the implementation in entitlement module will use this class to
 * manage storage, in this class you can specify the storage plan used to store.
 * It will be an extension point for storage management
 * if you want to use different storage plan, just replace it in constructor. but it class have implements
 * StorageManager
 */
public class StorageManagerImp implements StorageManager {

    private static StorageManager storageManagerImp;
    private static StorageManager storageManager;
    private static final Logger logger = LoggerFactory.getLogger(StorageManagerImp.class);
    private static final Object lock = new Object();


    /**
     * Get a StorageManager instance. This method will return an
     * StorageManager instance if exists, or creates a new one
     *
     * @return StorageManager instance
     */
    public static StorageManager getStoreManager() {

        if (storageManagerImp == null) {
            synchronized (lock) {
                if (storageManagerImp == null) {
                    storageManagerImp = new StorageManagerImp();
                }
            }
        }
        return storageManagerImp;
    }

    /**
     * By default we use InMemoryStorageManager
     */
    private StorageManagerImp() {
        storageManager = new InMemoryStorageManager();
    }

    @Override
    public void store(PolicyStoreDTO policy, String store) throws EntitlementException {
        storageManager.store(policy, store);
    }

    @Override
    public PolicyStoreDTO read(String policyId, String store) throws EntitlementException {
        return storageManager.read(policyId, store);
    }

    @Override
    public void remove(String policyId, String store) throws EntitlementException {
        storageManager.remove(policyId, store);
    }

    @Override
    public PolicyStoreDTO[] readAll(String store) throws EntitlementException {
        return storageManager.readAll(store);
    }
}
