/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.entitlement.xacml.endpoint;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;
import org.wso2.carbon.identity.entitlement.xacml.core.pdp.EntitlementEngine;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.store.PolicyStore;
import org.wso2.carbon.identity.entitlement.xacml.endpoint.exception.EntityNotFoundException;
import org.wso2.carbon.identity.entitlement.xacml.endpoint.model.XAMCLRequest;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * EntitlementAdmin micro service.
 */
@Component(
        name = "org.wso2.carbon.identity.entitlement.xacml.endpoint.EntitlementAdminService",
        service = Microservice.class,
        immediate = true
)
@SwaggerDefinition(
        info = @Info(
                title = "EntitlementAdminService Swagger Definition", version = "1.0",
                description = "Entitlement Admin Service",
                license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0"),
                contact = @Contact(
                        name = "",
                        email = "",
                        url = "http://wso2.com"
                ))
)
@Path("/entitlement")
public class EntitlementAdminService implements Microservice {

    private static final Logger logger = LoggerFactory.getLogger(EntitlementAdminService.class);

    private PolicyStore policyStore;

    @Activate
    protected void activate(BundleContext bundleContext) {
        // Nothing to do
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        // Nothing to do
    }

    @Reference(
            name = "identity.policy.store.service",
            service = PolicyStore.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.STATIC
//            unbind = "unregisterPolicyStore"
    )
    protected void registerDeployer(PolicyStore policyStore) {
        this.policyStore = policyStore;
    }


    @GET
    public String welcome() {
        return "welcome";
    }

    @GET
    @Path("/policy/getall")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(
            value = "Returns list of all available policies",
            notes = "Returns HTTP 404 if the is not found")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "policy store available"),
            @ApiResponse(code = 404, message = "policy store not found")})
    public Response getAllPolcies() throws EntityNotFoundException {
        try {
            PolicyStoreDTO[] policyDTOs = policyStore.readAllPolicyDTOs();
            return Response.status(Response.Status.OK).entity(policyDTOs).build();
        } catch (EntitlementException e) {
            throw new EntityNotFoundException("not found ", e);
        }
    }

    @GET
    @Path("/policy/{policyId}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(
            value = "Return policy corresponding to the policyId",
            notes = "Returns HTTP 404 if the policyId is not found")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Valid policy item found"),
            @ApiResponse(code = 404, message = "policy item not found")})
    public Response getPolcy(@PathParam("policyId") String policyId) throws EntityNotFoundException {
        try {
            PolicyStoreDTO policyDTO = policyStore.readPolicyDTO(policyId);
            return Response.status(Response.Status.OK).entity(policyDTO).build();
        } catch (EntitlementException e) {
            throw new EntityNotFoundException("policy not found ", e);
        }
    }

    @POST
    @Path("/evaluate")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(
            value = "Get response by evaluating JSON/XML XACML request")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "XACML JSON/XML Response"),
            @ApiResponse(code = 404, message = "policy item not found")})
    public Response evaluate(@ApiParam(value = "Response Media Type", required = true)
                             @HeaderParam("Content-Type") String contentType,
                             @ApiParam(value = "XACML JSON/XML Request", required = true) XAMCLRequest
                                     xacmlRequest) {
        EntitlementEngine entitlementEngine = EntitlementEngine.getInstance();
        String result = entitlementEngine.testPolicy(xacmlRequest.getAction(), xacmlRequest.getResource(),
                xacmlRequest.getSubject(), xacmlRequest.getEnvironment());
        return Response.status(Response.Status.OK).entity(result).build();

    }

    @Override
    public String toString() {
        return "EntitlementAdminService-OSGi-bundle";
    }
}
