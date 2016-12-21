package org.wso2.carbon.identity.entitlement.xacml.endpoint.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.entitlement.xacml.core.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.xacml.core.pdp.EntitlementEngine;
import org.wso2.carbon.identity.entitlement.xacml.endpoint.exception.EntitlementServiceException;
import org.wso2.carbon.identity.entitlement.xacml.endpoint.model.DecisionRequestModel;
import org.wso2.carbon.identity.entitlement.xacml.endpoint.model.DecisionResponseBooleanModel;
import org.wso2.carbon.identity.entitlement.xacml.endpoint.model.DecisionResponseModel;
import org.wso2.carbon.identity.entitlement.xacml.endpoint.util.EntitlementEndpointConstants;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Entry point class for the REST API end points
 */
@Component(
        name = "org.wso2.carbon.identity.entitlement.xacml.endpoint.service.EntitlementEvaluationService",
        service = Microservice.class,
        immediate = true
)
@Api(value = "/entitlement/evaluation", description = "Evaluate XACML Policies")
@Path("/entitlement/evaluation")
public class EntitlementEvaluationService implements Microservice {

    private static final Logger logger = LoggerFactory.getLogger(EntitlementEvaluationService.class);

    /**
     * API endpoint for evaluating policy by attributes as queries
     *
     * @return XML Policy result string
     */
    @POST
    @Path("by-attrib")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get response by evaluating attributes", response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "XACML JSON/XML Response")
    })
    public Response getDecisionByAttributes(@ApiParam(value = "Request Media Type", required = true)
                                            @HeaderParam(EntitlementEndpointConstants.ACCEPT_HEADER) String format,
                                            @ApiParam(value = "Response Media Type", required = true)
                                            @HeaderParam(EntitlementEndpointConstants.CONTENT_TYPE_HEADER)
                                                    String contentType,
                                            @ApiParam(value = "Decision Request Model", required = true)
                                                    DecisionRequestModel request) throws EntitlementServiceException {

        EntitlementEngine entitlementEngine = EntitlementEngine.getInstance();
        String result = entitlementEngine.evaluate(request.getAction(), request.getResource(),
                request.getSubject(), request.getEnvironment());
        DecisionResponseModel decisionResponseModel = new DecisionResponseModel();
        decisionResponseModel.setResponse(result);
        return Response.status(Response.Status.OK).entity(decisionResponseModel).build();
    }

    /**
     * API endpoint evaluating policy by using attributes as queries and return if true or false
     *
     * @return Boolean
     */

    @POST
    @Path("by-attrib-boolean")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get boolean response by evaluating attributes", response = Boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Boolean response")
    })
    public Response getBooleanDecision(@ApiParam(value = "Request Media Type", required = true)
                                       @HeaderParam(EntitlementEndpointConstants.ACCEPT_HEADER) String format,
                                       @ApiParam(value = "Response Media Type", required = true)
                                       @HeaderParam(EntitlementEndpointConstants.CONTENT_TYPE_HEADER) String contentType,
                                       @ApiParam(value = "Decision Request Model", required = true)
                                               DecisionRequestModel request) throws EntitlementServiceException {

        EntitlementEngine entitlementEngine = EntitlementEngine.getInstance();
        String response = entitlementEngine.evaluate(request.getAction(), request.getResource(),
                request.getSubject(), request.getEnvironment());
        DecisionResponseBooleanModel decisionResponseModel = new DecisionResponseBooleanModel();
        decisionResponseModel.setResponseBoolean(response.contains(EntitlementEndpointConstants.PERMIT));
        return Response.status(Response.Status.OK).entity(decisionResponseModel).build();

    }
}
