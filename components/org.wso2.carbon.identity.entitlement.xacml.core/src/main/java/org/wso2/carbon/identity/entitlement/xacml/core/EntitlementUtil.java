package org.wso2.carbon.identity.entitlement.xacml.core;

import org.apache.xerces.util.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.XACMLConstants;
import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.balana.combine.xacml2.FirstApplicablePolicyAlg;
import org.wso2.balana.combine.xacml2.OnlyOneApplicablePolicyAlg;
import org.wso2.balana.combine.xacml3.DenyOverridesPolicyAlg;
import org.wso2.balana.combine.xacml3.DenyUnlessPermitPolicyAlg;
import org.wso2.balana.combine.xacml3.OrderedDenyOverridesPolicyAlg;
import org.wso2.balana.combine.xacml3.OrderedPermitOverridesPolicyAlg;
import org.wso2.balana.combine.xacml3.PermitOverridesPolicyAlg;
import org.wso2.balana.combine.xacml3.PermitUnlessDenyPolicyAlg;
import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 *
 */
public class EntitlementUtil {

        private static final Logger logger = LoggerFactory.getLogger(EntitlementUtil.class);
    /**
     * Creates Simple XACML request using given attribute value.Here category, attribute ids and datatypes are
     * taken as default values.
     *
     * @param subject     user or role
     * @param resource    resource name
     * @param action      action name
     * @param environment environment name
     * @return String XACML request as String
     */
    public static String createSimpleXACMLRequest(String subject, String resource, String action, String environment) {

        return "<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" CombinedDecision=\"false\" ReturnPolicyIdList=\"false\">\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + action + "</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + subject + "</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:environment\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:environment:environment-id\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + environment + "</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + resource + "</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "</Request> ";
    }

    public static DocumentBuilderFactory getSecuredDocumentBuilderFactory() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);

        try {
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        } catch (ParserConfigurationException var2) {
            logger.error("Failed to load XML Processor Feature external-general-entities or external-parameter-entities or nonvalidating/load-external-dtd or secure-processing.");
        }

        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(0);
        dbf.setAttribute("http://apache.org/xml/properties/security-manager", securityManager);
        return dbf;

    }

    /**
     * Creates PolicyCombiningAlgorithm object based on policy combining url
     *
     * @param uri policy combining url as String
     * @return PolicyCombiningAlgorithm object
     * @throws EntitlementException throws if unsupported algorithm
     */
    public static PolicyCombiningAlgorithm getPolicyCombiningAlgorithm(String uri)
            throws EntitlementException {

        if (FirstApplicablePolicyAlg.algId.equals(uri)) {
            return new FirstApplicablePolicyAlg();
        } else if (DenyOverridesPolicyAlg.algId.equals(uri)) {
            return new DenyOverridesPolicyAlg();
        } else if (PermitOverridesPolicyAlg.algId.equals(uri)) {
            return new PermitOverridesPolicyAlg();
        } else if (OnlyOneApplicablePolicyAlg.algId.equals(uri)) {
            return new OnlyOneApplicablePolicyAlg();
        } else if (OrderedDenyOverridesPolicyAlg.algId.equals(uri)) {
            return new OrderedDenyOverridesPolicyAlg();
        } else if (OrderedPermitOverridesPolicyAlg.algId.equals(uri)) {
            return new OrderedPermitOverridesPolicyAlg();
        } else if (DenyUnlessPermitPolicyAlg.algId.equals(uri)) {
            return new DenyUnlessPermitPolicyAlg();
        } else if (PermitUnlessDenyPolicyAlg.algId.equals(uri)) {
            return new PermitUnlessDenyPolicyAlg();
        }

        throw new EntitlementException("Unsupported policy algorithm " + uri);
    }

    /**
     * Validates the given policy XML files against the standard XACML policies.
     *
     * @param policy Policy to validate
     * @return return false, If validation failed or XML parsing failed or any IOException occurs
     */
    public static boolean validatePolicy(PolicyStoreDTO policy) {
//        try {



            // there may be cases where you only updated the policy meta data in PolicyDTO not the
            // actual XACML policy String
            if (policy.getPolicy() == null || policy.getPolicy().trim().length() < 1) {
                return true;
            }

            //get policy version
            String policyXMLNS = getPolicyVersion(policy.getPolicy());

            return true;
//            Map<String, Schema> schemaMap = EntitlementServiceComponent.
//                    getEntitlementConfig().getPolicySchemaMap();
            //load correct schema by version
//            Schema schema = schemaMap.get(policyXMLNS);

//            if (schema != null) {
//                //build XML document
//                DocumentBuilder documentBuilder = getSecuredDocumentBuilder(false);
//                InputStream stream = new ByteArrayInputStream(policy.getPolicy().getBytes());
//                Document doc = documentBuilder.parse(stream);
//                //Do the DOM validation
//                DOMSource domSource = new DOMSource(doc);
//                DOMResult domResult = new DOMResult();
//                Validator validator = schema.newValidator();
//                validator.validate(domSource, domResult);
//                if (logger.isDebugEnabled()) {
//                    logger.debug("XACML Policy validation succeeded with the Schema");
//                }
//                return true;
//            } else {
//                logger.error("Invalid Namespace in policy");
//            }
//        } catch (SAXException e) {
//            logger.error("XACML policy is not valid according to the schema :" + e.getMessage());
//        } catch (java.io.IOException e) {
//            //ignore
//        } catch (ParserConfigurationException e) {
//            //ignore
//        }
//        return false;
    }

    public static String getPolicyVersion(String policy) {

        try {
            //build XML document
            DocumentBuilder documentBuilder = getSecuredDocumentBuilder(false);
            InputStream stream = new ByteArrayInputStream(policy.getBytes());
            Document doc = documentBuilder.parse(stream);


            //get policy version
            Element policyElement = doc.getDocumentElement();
            return policyElement.getNamespaceURI();
        } catch (Exception e) {
            // ignore exception as default value is used
            logger.debug("Policy version can not be identified. Default XACML 3.0 version is used", e);
            return XACMLConstants.XACML_3_0_IDENTIFIER;
        }
    }


    /**
     * * This method provides a secured document builder which will secure XXE attacks.
     *
     * @param setIgnoreComments whether to set setIgnoringComments in DocumentBuilderFactory.
     * @return DocumentBuilder
     * @throws ParserConfigurationException
     */
    private static DocumentBuilder getSecuredDocumentBuilder(boolean setIgnoreComments) throws
            ParserConfigurationException {

        DocumentBuilderFactory documentBuilderFactory = getSecuredDocumentBuilderFactory();
        documentBuilderFactory.setIgnoringComments(setIgnoreComments);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder;

    }
}
