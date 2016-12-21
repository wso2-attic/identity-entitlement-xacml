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

package org.wso2.carbon.identity.entitlement.xacml.endpoint.service;

import io.swagger.annotations.ApiOperation;
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
import org.wso2.carbon.identity.entitlement.xacml.core.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.xacml.core.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.xacml.core.exception.EntitlementException;
import org.wso2.carbon.identity.entitlement.xacml.core.policy.store.PolicyStore;
import org.wso2.carbon.identity.entitlement.xacml.endpoint.exception.EntitlementServiceException;
import org.wso2.msf4j.HttpStreamHandler;
import org.wso2.msf4j.HttpStreamer;
import org.wso2.msf4j.Microservice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * EntitlementAdmin micro service.
 */
@Component(
        name = "org.wso2.carbon.identity.entitlement.xacml.endpoint.service.EntitlementAdminService",
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
@Path("/entitlement/admin")
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
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterPolicyStore"
    )
    protected void registerPolicyStore(PolicyStore policyStore) {
        this.policyStore = policyStore;
    }

    protected void unregisterPolicyStore(PolicyStore policyStore) {

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
            @ApiResponse(code = 4040, message = "policy store not found")})
    public Response getAllPolcies() throws EntitlementServiceException {
        try {
            PolicyDTO[] policyDTOs = policyStore.readAllPolicyDTOs();
            return Response.status(Response.Status.OK).entity(policyDTOs).build();
        } catch (EntitlementException e) {
            throw new EntitlementServiceException(4040, "There is no policyStore");
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
            @ApiResponse(code = 4040, message = "policy item not found")})
    public Response getPolcy(@PathParam("policyId") String policyId) throws EntitlementServiceException {
        try {
            PolicyDTO policyDTO = policyStore.readPolicyDTO(policyId);
            return Response.status(Response.Status.OK).entity(policyDTO).build();
        } catch (EntitlementException e) {
            throw new EntitlementServiceException(4040, "There is no policy with policyId : " + policyId);
        }
    }

    @POST
    @Path("/policy/create/{policyId}")
    @ApiOperation(
            value = "Upload the policy. PolicyId have to in the path param")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Policy Created"),
            @ApiResponse(code = 4040, message = "Error in creating Policy")})
    public void createPolicy(@Context HttpStreamer httpStreamer,
                             @PathParam("policyId") String policyId) throws EntitlementServiceException {
        String fileName = policyId + EntitlementConstants.POLICY_BUNDLE_EXTENSTION;
        try {
            httpStreamer.callback(new HttpStreamHandlerImpl(fileName));
        } catch (FileNotFoundException e) {
            throw new EntitlementServiceException(4040, "Please provide policy file");
        }
    }

    @Override
    public String toString() {
        return "EntitlementAdminService-OSGi-bundle";
    }


    /**
     *
     */
    private static class HttpStreamHandlerImpl implements HttpStreamHandler {

        private static final Logger logger = LoggerFactory.getLogger(HttpStreamHandlerImpl.class);

        private static final java.nio.file.Path POLICY_PATH = Paths.get(EntitlementConstants.POLICY_STORE_LOCATION);
        private FileChannel fileChannel = null;
        private org.wso2.msf4j.Response response;

        public HttpStreamHandlerImpl(String fileName) throws FileNotFoundException {
            File file = Paths.get(POLICY_PATH.toString(), fileName).toFile();
            if (file.getParentFile().exists() || file.getParentFile().mkdirs()) {
                fileChannel = new FileOutputStream(file).getChannel();
            }
        }

        @Override
        public void init(org.wso2.msf4j.Response response) {
            this.response = response;
        }

        @Override
        public void end() throws Exception {
            fileChannel.close();
            response.setStatus(Response.Status.ACCEPTED.getStatusCode());
            response.send();
        }

        @Override
        public void chunk(ByteBuffer content) throws Exception {
            if (fileChannel == null) {
                throw new IOException("Unable to write file");
            }
            fileChannel.write(content);
        }

        @Override
        public void error(Throwable cause) {
            try {
                if (fileChannel != null) {
                    fileChannel.close();
                }
            } catch (IOException e) {
                // Log if unable to close the output stream
                logger.error("Unable to close file output stream", e);
            }
        }
    }
}
