package software.amazon.omics.sequencestore;

import software.amazon.awssdk.services.omics.OmicsClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.omics.common.CallWrapper;

public class DeleteHandler extends BaseHandlerStd {
    private static final String CALL_GRAPH = "AWS-Omics-SequenceStore::Delete";

    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<OmicsClient> proxyClient,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress ->
                        proxy.initiate(CALL_GRAPH, proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator::toDeleteRequest)
                                .makeServiceCall(CallWrapper.wrap(proxyClient.client()::deleteSequenceStore, logger))
                                .done(Translator::fromDeleteRequest));
    }
}
