package software.amazon.omics.rungroup;

import software.amazon.awssdk.services.omics.OmicsClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.omics.common.CallWrapper;

public class CreateHandler extends BaseHandlerStd {
    private static final String CALL_GRAPH = "AWS-Omics-RunGroup::Create";

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<OmicsClient> proxyClient,
        final Logger logger) {
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress ->
                        proxy.initiate(CALL_GRAPH, proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator.toCreateRequest(request))
                                .makeServiceCall(CallWrapper.wrap(proxyClient.client()::createRunGroup, logger))
                                .done(Translator::fromCreateResponse));
    }
}
