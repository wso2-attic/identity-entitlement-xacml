package org.wso2.carbon.identity.entitlement.xacml.core.policy.finder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.entitlement.xacml.core.dto.AttributeDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;
import org.wso2.carbon.identity.entitlement.xacml.core.pdp.EntitlementEngine;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.store.PolicyStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 *
 */
@Component(
        name = "org.wso2.carbon.identity.entitlement.xacml.core.policy.finder.CarbonPolicyFinderModule",
        immediate = true,
        service = PolicyFinderModule.class
)
public class CarbonPolicyFinderModule implements PolicyFinderModule {

    private static final String MODULE_NAME = "Policy Finder Module";

    private static final Logger logger = LoggerFactory.getLogger(EntitlementEngine.class);
    private PolicyStore policyStore;

    @Reference(
            name = "policy.store.service",
            service = PolicyStore.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterPolicyStore"
    )
    protected void registerPolicyStore(PolicyStore policyStore) {
        this.policyStore = policyStore;
    }

    protected void unregisterPolicyStore(PolicyStore policyStore) {

    }


    @Override
    public void init(Properties properties) throws EntitlementException {
        //
    }

    @Override
    public String[] getOrderedPolicyIdentifiers() throws EntitlementException {
        logger.debug("Start retrieving ordered policy identifiers at : " + new Date());
        List<String> policyIdentifiers = new ArrayList<>();
        List<PolicyStoreDTO> policyStoreDTOs = Arrays.asList(policyStore.readAllPolicyDTOs(false, true));
        policyStoreDTOs.forEach(policy -> policyIdentifiers.add(policy.getPolicyId()));
        logger.debug("Finish retrieving ordered policy identifiers at : " + new Date());
        return policyIdentifiers.toArray(new String[policyIdentifiers.size()]);
    }


    @Override
    public String[] getActivePolicies() throws EntitlementException {
        logger.debug("Start retrieving active policies at : " + new Date());
        List<String> policyIdentifiers = new ArrayList<>();
        List<PolicyStoreDTO> policyStoreDTOs = Arrays.asList(policyStore.readAllPolicyDTOs(true, true));
        policyStoreDTOs.forEach(policy -> policyIdentifiers.add(policy.getPolicyId()));
        logger.debug("Finish retrieving active policies at : " + new Date());
        return policyIdentifiers.toArray(new String[policyIdentifiers.size()]);
    }

    @Override
    public Map<String, Set<AttributeDTO>> getSearchAttributes(String identifier, Set<AttributeDTO> givenAttribute)
            throws EntitlementException {
        Map<String, Set<AttributeDTO>> attributeMap = new HashMap<>();
        List<PolicyStoreDTO> policyDTOs = Arrays.asList(policyStore.readAllPolicyDTOs(true, true));

        policyDTOs.forEach(policyDTO -> {
            Set<AttributeDTO> attributeDTOs = new HashSet<>(Arrays.asList(policyDTO.getAttributeDTOs()));
            List<String> policyIdRef = Arrays.asList(policyDTO.getPolicyIdReferences());
            List<String> policySetIdRef = Arrays.asList(policyDTO.getPolicySetIdReferences());
            policyDTOs.forEach(dto -> {
                policyIdRef.stream().filter(policyId -> dto.getPolicyId().equals(policyId))
                        .forEach(policyId -> attributeDTOs.addAll(Arrays.asList(dto.getAttributeDTOs())));
                policySetIdRef.stream().filter(policySetId -> dto.getPolicyId().equals(policySetId))
                        .forEach(policySetId -> attributeDTOs.addAll(Arrays.asList(dto.getAttributeDTOs())));
            });
            attributeMap.put(policyDTO.getPolicyId(), attributeDTOs);
        });

        return attributeMap;
    }

    @Override
    public Optional<String> getReferencedPolicy(String policyId) throws EntitlementException {
        PolicyStoreDTO dto = policyStore.readPolicyDTO(policyId);
        if (dto.getPolicy() != null && !dto.isActive()) {
            return Optional.ofNullable(dto.getPolicy());
        }
        return Optional.empty();
    }

    @Override
    public String getPolicy(String policyId) throws EntitlementException {
        return (policyStore.readPolicyDTO(policyId)).getPolicy();
    }

    @Override
    public int getPolicyOrder(String policyId) throws EntitlementException {
        return (policyStore.readPolicyDTO(policyId)).getPolicyOrder();
    }

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public int getSupportedSearchAttributesScheme() {
        return PolicyFinderModule.COMBINATIONS_BY_CATEGORY_AND_PARAMETER;
    }

    @Override
    public boolean isDefaultCategoriesSupported() {
        return true;
    }

    @Override
    public boolean isPolicyOrderingSupport() {
        return true;
    }

    @Override
    public boolean isPolicyDeActivationSupport() {
        return true;
    }
}
