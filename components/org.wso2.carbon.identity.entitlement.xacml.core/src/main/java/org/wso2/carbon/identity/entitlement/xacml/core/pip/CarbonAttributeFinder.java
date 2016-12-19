/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.entitlement.xacml.core.pip;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.balana.ParsingException;
import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.attr.BagAttribute;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.ctx.Attribute;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.ctx.Status;
import org.wso2.balana.finder.AttributeFinderModule;
import org.wso2.balana.xacml3.Attributes;
import org.wso2.carbon.identity.entitlement.xacml.core.EntitlementUtil;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * CarbonAttributeFinder registers with sun-xacml engine as an AttributeFinderModule and delegate
 * functionality to the attribute handlers registered with it self.
 * <p/>
 * Whenever the XACML engine finds a missing attribute in the XACML request - it will call the
 * findAttribute() method of this class.
 */
@Component(
        name = "org.wso2.carbon.identity.entitlement.xacml.core.pip.CarbonAttributeFinder",
        immediate = true,
        service = AttributeFinderModule.class
)
public class CarbonAttributeFinder extends AttributeFinderModule {

    private List<PIPAttributeFinder> attrFinders = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(CarbonAttributeFinder.class);

//    private PIPAttributeCache attributeFinderCache = null;


    @Reference(
            name = "attribute.finder",
            service = PIPAttributeFinder.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterAttributeFinder"
    )
    protected void registerAttributeFinder(PIPAttributeFinder pipAttributeFinder) throws Exception {
        pipAttributeFinder.init(new Properties());
        attrFinders.add(pipAttributeFinder);
    }

    protected void unregisterAttributeFinder(PIPAttributeFinder pipAttributeFinder) {
        attrFinders.remove(pipAttributeFinder);
    }

    /**
     * Registers PIP attribute handlers with the PDP against their supported attributes. This PIP
     * attribute handlers are picked from pip-config.xml file - which should be inside
     * [CARBON_HOME]\repository\conf.
     */
    public void init() {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.balana.finder.AttributeFinderModule#findAttribute(java.net.URI, java.net.URI,
     * java.net.URI, java.net.URI, org.wso2.balana.EvaluationCtx, int)
     */
    public EvaluationResult findAttribute(URI attributeType, URI attributeId, String issuer,
                                          URI category, EvaluationCtx context) {

        List<AttributeValue> attrBag = new ArrayList<>();


        if (attrFinders.isEmpty()) {
            logger.debug("No attribute designators defined for the attribute " + attributeId.toString());
            return new EvaluationResult(BagAttribute.createEmptyBag(attributeType));

        }

        try {
            for (PIPAttributeFinder pipAttributeFinder : attrFinders) {
                logger.debug(String.format("Finding attributes with the PIP attribute handler %1$s",
                        pipAttributeFinder.getClass()));

                Set<String> attrs = pipAttributeFinder.getAttributeValues(attributeType, attributeId, category,
                        issuer, context);

                if (attrs != null) {
                    for (Iterator iterAttr = attrs.iterator(); iterAttr.hasNext(); ) {
                        final String attr = (String) iterAttr.next();
                        AttributeValue attribute = EntitlementUtil.
                                getAttributeValue(attr, attributeType.toString());
                        attrBag.add(attribute);
                    }
                }
            }
        } catch (ParsingException e) {
            logger.error("Error while parsing attribute values from EvaluationCtx : " + e);
            ArrayList<String> code = new ArrayList<String>();
            code.add(Status.STATUS_MISSING_ATTRIBUTE);
            Status status = new Status(code,
                    "Error while parsing attribute values from EvaluationCtx : " + e.getMessage());
            return new EvaluationResult(status);
        } catch (ParseException e) {
            e.printStackTrace();
            logger.error("Error while parsing attribute values from EvaluationCtx : " + e);
            ArrayList<String> code = new ArrayList<String>();
            code.add(Status.STATUS_MISSING_ATTRIBUTE);
            Status status = new Status(code,
                    "Error while parsing attribute values from EvaluationCtx : " + e.getMessage());
            return new EvaluationResult(status);
        } catch (URISyntaxException e) {
            logger.error("Error while parsing attribute values from EvaluationCtx : " + e);
            ArrayList<String> code = new ArrayList<String>();
            code.add(Status.STATUS_MISSING_ATTRIBUTE);
            Status status = new Status(code,
                    "Error while parsing attribute values from EvaluationCtx :" + e.getMessage());
            return new EvaluationResult(status);
        } catch (Exception e) {
            logger.error("Error while retrieving attribute values from PIP  attribute finder : " + e);
            ArrayList<String> code = new ArrayList<String>();
            code.add(Status.STATUS_MISSING_ATTRIBUTE);
            Status status = new Status(code, "Error while retrieving attribute values from PIP"
                    + " attribute finder : " + e.getMessage());
            return new EvaluationResult(status);
        }
        return new EvaluationResult(new BagAttribute(attributeType, attrBag));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.balana.finder.AttributeFinderModule#isDesignatorSupported()
     */
    public boolean isDesignatorSupported() {
        return true;
    }


    /*
     * (non-Javadoc)
     *
     * @see org.wso2.balana.finder.AttributeFinderModule#getSupportedIds()
     */
    public Set getSupportedIds() {
        return null;
    }


    /**
     * Clears attribute cache
     */
    public void clearAttributeCache() {
//        if (attributeFinderCache != null) {
//            attributeFinderCache.clearCache();
//            // clear decision cache
//            EntitlementEngine.getInstance().clearDecisionCache();
//        }
    }

    /**
     * Converts DOM object to String. This is a helper method for creating cache key
     *
     * @param evaluationCtx EvaluationCtx
     * @return String Object
     * @throws TransformerException Exception throws if fails
     */
    private String encodeContext(EvaluationCtx evaluationCtx) throws TransformerException {
        OutputStream stream = new ByteArrayOutputStream();
        evaluationCtx.getRequestCtx().encode(stream);
        String rowContext = stream.toString();
        String contextWithAttributeValues = rowContext + "][";

        StringBuilder builder = new StringBuilder();
        for (Attributes attributes : evaluationCtx.getRequestCtx().getAttributesSet()) {
            builder.append("<Attributes ").append(">");
            for (Attribute attribute : attributes.getAttributes()) {
                attribute.encode(builder);
            }
            builder.append("</Attributes>");
        }
        contextWithAttributeValues += builder.toString();

        return contextWithAttributeValues;
    }
}
