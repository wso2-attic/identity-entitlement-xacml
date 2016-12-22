package org.wso2.carbon.identity.entitlement.xacml.core.deployer;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.identity.entitlement.xacml.core.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.PolicyReader;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.collection.PolicyCollection;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.store.PolicyStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

/**
 * It's a carbon deployer which will listen to the provided directory and keep the PolicyStore and PolicyCollection
 * updated
 */
@Component(
        name = "org.wso2.carbon.identity.entitlement.xacml.core.deployer.PolicyDeployer",
        immediate = true
)
public class PolicyDeployer implements Deployer {

    private static final Logger logger = LoggerFactory.getLogger(PolicyDeployer.class);
    private ArtifactType artifactType;
    private URL repository;
    private PolicyCollection policyCollection;
    private PolicyStore policyStore;

    @Reference(
            name = "policy.collection.service",
            service = PolicyCollection.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterPolicyCollection"
    )
    protected void registerPolicyCollection(PolicyCollection policyCollection) {
        this.policyCollection = policyCollection;
    }

    protected void unregisterPolicyCollection(PolicyCollection policyCollection) {
    }

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
    public void init() {
        logger.debug("Initializing the PolicyDeployer");
        artifactType = new ArtifactType<>("policy");
        try {
            repository = new URL("file:" + EntitlementConstants.POLICY_STORE_LOCATION);
        } catch (MalformedURLException e) {
            logger.error("Error while initializing deployer", e.getMessage());
        }
    }

    @Override
    public String deploy(Artifact artifact) throws CarbonDeploymentException {
        logger.debug("Deploying : " + artifact.getName());
        readArtifact(artifact);
        return artifact.getName();
    }

    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        if (!(key instanceof String)) {
            throw new CarbonDeploymentException("Error while Un Deploying : " + key + "is not a String value");
        }
        logger.debug("Undeploying : " + key);
        String policyId = (String) key;
        if (policyId.endsWith(EntitlementConstants.POLICY_BUNDLE_EXTENSTION)) {
            policyId = policyId.substring(0, policyId.lastIndexOf(EntitlementConstants.POLICY_BUNDLE_EXTENSTION));
            try {
                policyStore.removePolicy(policyId);
                policyCollection.deletePolicy(policyId);
            } catch (EntitlementException e) {
                logger.error(e.getMessage());
            }
        }
    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        logger.debug("Updating : " + artifact.getName());
        readArtifact(artifact);
        return artifact.getName();
    }

    @Override
    public URL getLocation() {
        return repository;
    }

    @Override
    public ArtifactType getArtifactType() {
        return artifactType;
    }

    /**
     * Read the artifacts and save the policy and metadata to PolicyStore and PolicyCollection
     * @param artifact deployed articles
     */
    private synchronized void readArtifact(Artifact artifact) {
        String artifactName = artifact.getName();

        if (artifact.getName().endsWith(EntitlementConstants.POLICY_BUNDLE_EXTENSTION)) {
            PolicyDTO policyDTO = new PolicyDTO();
            ArrayList<Boolean> policyRequirements = new ArrayList<>(2);
            policyRequirements.add(0, false);
            policyRequirements.add(1, false);
            String policyId = artifactName.substring(0,
                    artifactName.lastIndexOf(EntitlementConstants.POLICY_BUNDLE_EXTENSTION));
            try (ZipFile zipFile = new ZipFile(artifact.getFile().getAbsoluteFile())) {
                zipFile.stream()
                        .forEach(zipEntry -> {
                            if (zipEntry.getName().endsWith(EntitlementConstants.POLICY_EXTENSTION)) {
                                try (InputStream inputStream = zipFile.getInputStream(zipEntry);
                                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                                    String content = reader.lines().collect(Collectors.joining());
                                    if (content != null) {
                                        content = content.replaceAll(">\\s+<", "><").replaceAll("\n", " ")
                                                .replaceAll("\r", " ").replaceAll("\t", " ");
                                        policyDTO.setPolicyId(policyId);
                                        policyDTO.setPolicy(content);
                                        policyRequirements.add(0, true);
                                    }
                                } catch (IOException e) {
                                    logger.error("Error in reading the xml file ", e);
                                }
                            }
                            if (zipEntry.getName().endsWith(EntitlementConstants.POLICY_PROPERTIES_EXTENSTION)) {
                                try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
                                    Properties prop = new Properties();
                                    prop.load(inputStream);
                                    String active = prop.getProperty("active");
                                    if (active == null) {
                                        policyDTO.setActive(true);
                                    } else {
                                        policyDTO.setActive(Boolean.parseBoolean(active));
                                    }
                                    String order = prop.getProperty("order");
                                    if (order == null) {
                                        policyDTO.setPolicyOrder(0);
                                    } else {
                                        policyDTO.setPolicyOrder(Integer.parseInt(order));
                                    }
                                    String version = prop.getProperty("version");
                                    if (version == null) {
                                        policyDTO.setVersion(1);
                                    } else {
                                        policyDTO.setVersion(Integer.parseInt(version));
                                    }
                                    policyRequirements.add(1, true);
                                } catch (IOException e) {
                                    logger.error("Error in reading the properties file ", e);
                                } catch (NumberFormatException e) {
                                    logger.error("Error in casting the property data ", e);
                                }
                            }
                        });
            } catch (IOException e) {
                logger.error("Error in reading the xacml file ", e);
            }
            if (policyRequirements.get(0) && policyRequirements.get(1)) {
                try {
                    policyStore.addPolicy(policyDTO);
                    if (policyDTO.isActive()) {
                        PolicyReader.getInstance().getPolicy(policyDTO.getPolicy()).
                                ifPresent(abstractPolicy -> policyCollection.addPolicy(abstractPolicy));
                    } else {
                        policyCollection.deletePolicy(policyDTO.getPolicyId());
                    }
                } catch (EntitlementException e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }
}
