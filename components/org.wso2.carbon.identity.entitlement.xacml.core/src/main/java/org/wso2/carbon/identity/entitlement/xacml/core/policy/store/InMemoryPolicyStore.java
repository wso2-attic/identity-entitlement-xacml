package org.wso2.carbon.identity.entitlement.xacml.core.policy.store;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.balana.AbstractPolicy;
import org.wso2.carbon.identity.entitlement.xacml.core.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.xacml.core.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.xacml.core.PolicyOrderComparator;
import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.PolicyAttributeBuilder;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.PolicyReader;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 */
@Component(
        name = "org.wso2.carbon.identity.entitlement.xacml.core.policy.store.InMemoryPolicyStore",
        immediate = true,
        service = PolicyStore.class
)
public class InMemoryPolicyStore implements PolicyStore {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryPolicyStore.class);

    private Map<String, PolicyStoreDTO> policyStore = new HashMap<>();

    private String regString = "[a-zA-Z0-9._:-]{3,100}$";


    @Override
    public PolicyStoreDTO readPolicyDTO(String policyId) throws EntitlementException {
        isExistPolicy(policyId);
        PolicyStoreDTO policyStoreDTO = policyStore.get(policyId);
        logger.debug("Reading policy from InMemoryPolicyStore : " + policyId);
        return policyStoreDTO;
    }

    @Override
    public void addPolicy(PolicyDTO policyDTO, boolean newPolicy) throws EntitlementException {
        if (policyDTO == null) {
            throw new EntitlementException("Policy is null ");
        }
        if (!EntitlementUtil.validatePolicy(policyDTO.getPolicy())) {
            throw new EntitlementException("Invalid Entitlement Policy. " +
                    "Policy is not valid according to XACML schema");
        }

        if (policyDTO.getPolicyId().contains("/")) {
            throw new EntitlementException(
                    " Policy Id cannot contain / characters. Please correct and upload again");
        }
        if (!policyDTO.getPolicyId().matches(regString)) {
            throw new EntitlementException(
                    "An Entitlement Policy Id is not valid. It contains illegal characters");
        }


        if (isExistPolicy(policyDTO.getPolicyId()) && newPolicy) {
            throw new EntitlementException(
                    "An Entitlement Policy with the given PolicyId already exists");
        }

        AbstractPolicy abstractPolicy =  PolicyReader.getInstance(null).getPolicy(policyDTO.getPolicy())
                .orElseThrow(()-> new EntitlementException("Unsupported Entitlement Policy. Policy can not be parsed"));

        if (!Objects.equals(policyDTO.getPolicyId(), abstractPolicy.getId().toString())) {
            throw new EntitlementException("The policy file name must match policyId " );
        }

        PolicyStoreDTO policyStoreDTO = new PolicyStoreDTO();

        policyStoreDTO.setPolicyId(policyDTO.getPolicyId());
        policyStoreDTO.setPolicy(policyDTO.getPolicy());
        policyStoreDTO.setActive(policyDTO.isActive());
        policyStoreDTO.setPolicyOrder(policyDTO.getPolicyOrder());
        policyStoreDTO.setVersion(policyDTO.getVersion());

        if (newPolicy) {
            policyStoreDTO = generatePolicyData(policyStoreDTO);
        } else {
            if (!Objects.equals(readPolicyDTO(policyDTO.getPolicyId()).getPolicy(), policyDTO.getPolicy())) {
                policyStoreDTO = generatePolicyData(policyStoreDTO);
            }
        }
        logger.debug("Adding policy to InMemory PolicyStore : " + policyStoreDTO.getPolicyId());
        policyStore.put(policyStoreDTO.getPolicyId(), policyStoreDTO);
    }

    private PolicyStoreDTO generatePolicyData(PolicyStoreDTO policyStoreDTO) throws EntitlementException {

        PolicyAttributeBuilder policyAttributeBuilder = new PolicyAttributeBuilder();
        policyStoreDTO.setAttributeDTOs(policyAttributeBuilder.getPolicyMetaDataFromPolicy(policyStoreDTO.getPolicy()));

        OMElement omElement;
        try {
            omElement = AXIOMUtil.stringToOM(policyStoreDTO.getPolicy());
        } catch (XMLStreamException e) {
            throw new EntitlementException("Error in reading from policy ", e);
        }
        if (omElement == null) {
            throw new EntitlementException("Error in reading from policy OMElement is null");
        }
        policyStoreDTO.setPolicyType(omElement.getLocalName());

        Iterator iterator1 = omElement.getChildrenWithLocalName(EntitlementConstants.POLICY_REFERENCE);
        if (iterator1 != null) {
            ArrayList<String> policyReferences = new ArrayList<>();
            iterator1.forEachRemaining(policyReference -> policyReferences.add(((OMElement)policyReference).getText()));
            policyStoreDTO.setPolicyIdReferences(policyReferences.toArray(new String[policyReferences.size()]));
        }

        Iterator iterator2 = omElement.getChildrenWithLocalName(EntitlementConstants.POLICY_SET_REFERENCE);
        if (iterator2 != null) {
            ArrayList<String> policySetReferences = new ArrayList<>();
            iterator2.forEachRemaining(policyReference ->
                    policySetReferences.add(((OMElement)policyReference).getText()));
            policyStoreDTO.setPolicySetIdReferences(policySetReferences
                    .toArray(new String[policySetReferences.size()]));
        }
        return policyStoreDTO;
    }

    @Override
    public void updatePolicy(PolicyDTO policy) throws EntitlementException {
        logger.debug("Updating policy from InMemoryPolicyStore : " + policy.getPolicyId());
        addPolicy(policy, false);
    }

    @Override
    public PolicyStoreDTO[] readAllPolicyDTOs() throws EntitlementException {
        Collection<PolicyStoreDTO> policyStoreDTOs = policyStore.values();
        return policyStoreDTOs.toArray(new PolicyStoreDTO[policyStoreDTOs.size()]);
    }

    @Override
    public PolicyStoreDTO[] readAllPolicyDTOs(boolean active, boolean order) throws EntitlementException {
        PolicyStoreDTO[] policyStoreDTOs = readAllPolicyDTOs();
        List<PolicyStoreDTO> collect = Arrays.stream(policyStoreDTOs).
                filter((policyDto -> policyDto.isActive() == active)).collect(Collectors.toList());
        policyStoreDTOs = collect.toArray(new PolicyStoreDTO[collect.size()]);
        if (order) {
            Arrays.sort(policyStoreDTOs, new PolicyOrderComparator());
        }
        return policyStoreDTOs;
    }

    @Override
    public boolean isExistPolicy(String policyId) throws EntitlementException {
        return policyStore.containsKey(policyId);
    }

    @Override
    public void removePolicy(String policyId) throws EntitlementException {
        if (!isExistPolicy(policyId)) {
            throw new EntitlementException("There is no policy in store with policyId : " + policyId);
        }
        logger.debug("Removing policy from InMemoryPolicyStore : " + policyId);
        policyStore.remove(policyId);
    }

}
