package org.wso2.carbon.identity.entitlement.xacml.core;

/**
 *
 */
public class EntitlementConstants {

    public static final String POLICY_STORE_LOCATION = "/home/senthalan/policy/";

    public static final String POLICY_BUNDLE_EXTENSTION = ".xacml";


    /**
     * String constants used XACML policy
     */
    public static final String POLICY_REFERENCE = "policyIdReferences";

    public static final String POLICY_SET_REFERENCE = "policySetIdReferences";

    public static final String TARGET_ELEMENT = "Target";

    public static final String RULE_ELEMENT = "Rule";

    public static final String CONDITION_ELEMENT = "Condition";

    public static final String POLICY_ELEMENT = "Policy";

    public static final String APPLY_ELEMENT = "Apply";

    public static final String MATCH_ELEMENT = "Match";

    public static final String SUBJECT_ELEMENT = "Subject";

    public static final String ACTION_ELEMENT = "Action";

    public static final String RESOURCE_ELEMENT = "Resource";

    public static final String ENVIRONMENT_ELEMENT = "Environment";

    public static final String ANY_OF = "AnyOf";

    public static final String ALL_OF = "AllOf";

    public static final String ATTRIBUTE_DESIGNATOR = "AttributeDesignator";

    public static final String ATTRIBUTE_ID = "AttributeId";

    public static final String DATA_TYPE = "DataType";

    public static final String CATEGORY = "Category";

    public static final String REQUEST_CONTEXT_PATH = "RequestContextPath";

    public static final String ATTRIBUTE_SELECTOR = "AttributeSelector";

    public static final String ATTRIBUTE_VALUE = "AttributeValue";

    public static final String SUBJECT_ID_DEFAULT = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";

    public static final String ENVIRONMENT_ID_DEFAULT = "urn:oasis:names:tc:xacml:1.0:environment:environment-id";

    public static final String RESOURCE_ID_DEFAULT = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";

    public static final String ACTION_ID_DEFAULT = "urn:oasis:names:tc:xacml:1.0:action:action-id";


    public static final String XACML_3_POLICY_XMLNS = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17";

    public static final String XACML_2_POLICY_XMLNS = "urn:oasis:names:tc:xacml:2.0:policy:schema:os";

    public static final String XACML_1_POLICY_XMLNS = "urn:oasis:names:tc:xacml:1.0:policy";

    public static final String XACML_3_POLICY_SCHEMA_FILE = "xacml3.xsd";

    public static final String XACML_2_POLICY_SCHEMA_FILE = "xacml2.xsd";

    public static final String XACML_1_POLICY_SCHEMA_FILE = "xacml1.xsd";

    public static final String UNKNOWN = "UNKNOWN";

    public static final String SEARCH_WARNING_MESSAGE1 = "Attribute values are not defined directly";

    public static final String SEARCH_WARNING_MESSAGE2 = "No Attributes are defined";

    public static final String SEARCH_WARNING_MESSAGE3 = "Attribute Selector Element is contained with Xpath expression";

    public static final String SEARCH_WARNING_MESSAGE4 = "Apply Element is not contained within Condition Element";
}
