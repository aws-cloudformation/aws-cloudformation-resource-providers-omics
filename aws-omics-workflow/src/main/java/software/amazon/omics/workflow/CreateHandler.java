package software.amazon.omics.workflow;

import software.amazon.awssdk.services.omics.OmicsClient;
import software.amazon.awssdk.services.omics.model.CreateWorkflowRequest;
import software.amazon.awssdk.services.omics.model.CreateWorkflowResponse;
import software.amazon.awssdk.services.omics.model.WorkflowStatus;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.omics.common.CallWrapper;

import java.util.Objects;

public class CreateHandler extends BaseHandlerStd {

    private static final String CALL_GRAPH = "AWS-Omics-Workflow::Create";

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<OmicsClient> proxyClient,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress ->
                        proxy.initiate(CALL_GRAPH, proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator.toCreateRequest(request))
                                .makeServiceCall(CallWrapper.wrap(proxyClient.client()::createWorkflow, logger))
                                .stabilize(this::stabilizeOnCreate)
                                .done(Translator::fromCreateResponse));
    }

    private Boolean stabilizeOnCreate(
            CreateWorkflowRequest request,
            CreateWorkflowResponse response,
            ProxyClient<OmicsClient> proxyClient,
            ResourceModel model,
            CallbackContext callbackContext) {

        model.setId(response.id());
        if (Objects.isNull(callbackContext)) {
            callbackContext = new CallbackContext();
        } else {
            callbackContext.incrementAttempt();
        }

        WorkflowStatus status =
                proxyClient.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model),
                        proxyClient.client()::getWorkflow).status();

        if (Objects.nonNull(status) && status != WorkflowStatus.ACTIVE
                && callbackContext.attempts() >= MAX_ATTEMPTS) {
            throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME,
                    model.getArn(),
                    new Throwable("Max attempts reached, determining as Timeout")
            );
        }

        model.setStatus(status == null ? null: status.toString());
        return WorkflowStatus.ACTIVE == status;
    }
}
