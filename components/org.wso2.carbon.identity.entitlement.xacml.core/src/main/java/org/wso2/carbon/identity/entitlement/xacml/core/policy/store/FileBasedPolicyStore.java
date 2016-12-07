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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Component(
        name = "org.wso2.carbon.identity.entitlement.xacml.core.policy.store.FileBasedPolicyStore",
        immediate = true,
        service = PolicyStore.class
)
public class FileBasedPolicyStore implements PolicyStore {

    private static final Logger logger = LoggerFactory.getLogger(FileBasedPolicyStore.class);

    private String policyLocation = "/home/senthalan/wso2_projects/wso2msf4j-2.1.0/deployment/xacml/policy/";
    private String regString = "[a-zA-Z0-9._:-]{3,100}$";


    @Override
    public PolicyStoreDTO readPolicyDTO(String policyId) throws EntitlementException {
        String policyPath = policyLocation + policyId + ".xml";
        String content;
        try {
            content = new String(Files.readAllBytes(Paths.get(policyPath)), "UTF-8");
        } catch (IOException e) {
            throw new EntitlementException("Error in reading the policy : ", e);
        }
        AbstractPolicy policyObj = PolicyReader.getInstance(null).getPolicy(content);
        if (policyObj == null) {
            throw new EntitlementException("Unsupported Entitlement Policy. Policy can not be parsed");
        }
        content = content.replaceAll(">\\s+<", "><").replaceAll("\n", " ").replaceAll("\r", " ");
        PolicyStoreDTO policyStoreDTO = new PolicyStoreDTO();
        policyStoreDTO.setPolicyId(policyId);
        policyStoreDTO.setPolicy(content);
        policyStoreDTO.setActive(true);
        policyStoreDTO.setPolicyOrder(1);

        PolicyAttributeBuilder policyAttributeBuilder = new PolicyAttributeBuilder();
        policyStoreDTO.setAttributeDTOs(policyAttributeBuilder.getPolicyMetaDataFromPolicy(content));

        OMElement omElement;
        try {
            omElement = AXIOMUtil.stringToOM(content);
            policyStoreDTO.setPolicyType(omElement.getLocalName());
        } catch (XMLStreamException e) {
            throw new EntitlementException("Error in reading from policy", e);
        }

        Iterator iterator1 = omElement.getChildrenWithLocalName(EntitlementConstants.POLICY_REFERENCE);
        if (iterator1 != null) {
            ArrayList<String> policyReferences = new ArrayList<>();
            while (iterator1.hasNext()) {
                OMElement policyReference = (OMElement) iterator1.next();
                policyReferences.add(policyReference.getText());
            }
            policyStoreDTO.setPolicyIdReferences(policyReferences.toArray(new String[policyReferences.size()]));
        }

        Iterator iterator2 = omElement.getChildrenWithLocalName(EntitlementConstants.POLICY_SET_REFERENCE);
        if (iterator2 != null) {
            ArrayList<String> policySetReferences = new ArrayList<>();
            while (iterator2.hasNext()) {
                OMElement policySetReference = (OMElement) iterator2.next();
                policySetReferences.add(policySetReference.getText());
            }
            policyStoreDTO.setPolicySetIdReferences(policySetReferences.toArray(new String[policySetReferences.size()]));
        }
        logger.debug("Policy read with policyId : " + policyId);
        return policyStoreDTO;
    }

    @Override
    public void addPolicy(PolicyStoreDTO policy, boolean newPolicy) throws EntitlementException {
        if (policy == null) {
            throw new EntitlementException("Policy is null ");
        }
        if (!EntitlementUtil.validatePolicy(policy)) {
            throw new EntitlementException("Invalid Entitlement Policy. " +
                    "Policy is not valid according to XACML schema");
        }

        AbstractPolicy policyObj = PolicyReader.getInstance(null).getPolicy(policy.getPolicy());
        if (policyObj == null) {
            throw new EntitlementException("Unsupported Entitlement Policy. Policy can not be parsed");
        }
        String policyId = policyObj.getId().toASCIIString();
        policy.setPolicyId(policyId);

        if (policyId.contains("/")) {
            throw new EntitlementException(
                    " Policy Id cannot contain / characters. Please correct and upload again");
        }
        if (!policyId.matches(regString)) {
            throw new EntitlementException(
                    "An Entitlement Policy Id is not valid. It contains illegal characters");
        }


        if (isExistPolicy(policyId) && newPolicy) {
            throw new EntitlementException(
                    "An Entitlement Policy with the given PolicyId already exists");
        }

        //save the policy as .xml file
        try {
            Files.write(Paths.get(policyLocation + policyId + ".xml"), policy.getPolicy().getBytes("UTF-8"));
            logger.debug("Policy created with policyId : " + policyId);
        } catch (IOException e) {
            throw new EntitlementException("Error in creating file ", e);
        }
    }

    @Override
    public void updatePolicy(PolicyStoreDTO policy) throws EntitlementException {
            addPolicy(policy, false);
    }

    @Override
    public PolicyStoreDTO[] readAllPolicyDTOs() throws EntitlementException {
        List<PolicyStoreDTO> policyStoreDTOs = new ArrayList<>();
        try {
            Files.list(Paths.get(policyLocation))
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        String policyId = file.getFileName().toString();
                        if (policyId.endsWith(".xml")) {
                            policyId = policyId.substring(0, policyId.lastIndexOf("."));
                            try {
                                policyStoreDTOs.add(readPolicyDTO(policyId));
                            } catch (EntitlementException e) {
//
                            }
                        }
                    });
        } catch (IOException e) {
            throw new EntitlementException("Error in reading the policy : ", e);
        }
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
        File policy = new File(policyLocation + policyId + ".xml");
        return policy.exists();
    }

    @Override
    public void removePolicy(String policyId) throws EntitlementException {
        if (!isExistPolicy(policyId)) {
            logger.error("There is no policy in store with policyId : " + policyId);
            return;
        }
        try {
            Files.delete(Paths.get(policyLocation + policyId + ".xml"));
            logger.debug("Policy deleted with policyId : " + policyId);
        } catch (IOException e) {
            throw new EntitlementException("Error in accessing the file" ,e);
        }
    }

}
