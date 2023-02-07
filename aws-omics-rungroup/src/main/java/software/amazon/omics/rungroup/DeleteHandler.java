package software.amazon.omics.rungroup;

import software.amazon.awssdk.services.omics.OmicsClient;
import software.amazon.awssdk.services.omics.model.DeleteRunGroupRequest;
import software.amazon.awssdk.services.omics.model.DeleteRunGroupResponse;
import software.amazon.awssdk.services.omics.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.omics.common.CallWrapper;

import java.util.Objects;

public class DeleteHandler extends BaseHandlerStd {
    private static final String CALL_GRAPH = "AWS-Omics-RunGroup::Delete";

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<OmicsClient> proxyClient,
        final Logger logger) {
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress ->
                        proxy.initiate(CALL_GRAPH, proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator::toDeleteRequest)
                                .makeServiceCall(CallWrapper.wrap(proxyClient.client()::deleteRunGroup, logger))
                                .stabilize(this::stabilizeOnDelete)
                                .done(Translator::fromDeleteResponse)
                );
    }

    private Boolean stabilizeOnDelete(
            DeleteRunGroupRequest request,
            DeleteRunGroupResponse response,
            ProxyClient<OmicsClient> proxyClient,
            ResourceModel model,
            CallbackContext callbackContext) {

        if (Objects.isNull(callbackContext)) {
            callbackContext = new CallbackContext();
        } else {
            callbackContext.incrementAttempt();
        }

        if (callbackContext.attempts() >= MAX_ATTEMPTS) {
            throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME,
                    model.getArn(),
                    new Throwable("Max attempts reached, determining as Timeout")
            );
        }

        try {
            proxyClient.injectCredentialsAndInvokeV2(Translator.toReadRequest(model),
                    proxyClient.client()::getRunGroup);
        } catch (ResourceNotFoundException e) {
            // Resource deleted - so exception is expected
            return true;
        }

        return false;
    }
}
