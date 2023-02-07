package software.amazon.omics.rungroup;

import software.amazon.awssdk.services.omics.OmicsClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.omics.common.CallWrapper;

public class ListHandler extends BaseHandlerStd {

    private static final String CALL_GRAPH = "AWS-Omics-RunGroup::List";

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            AmazonWebServicesClientProxy proxy,
            ResourceHandlerRequest<ResourceModel> request,
            CallbackContext callbackContext,
            ProxyClient<OmicsClient> proxyClient,
            Logger logger) {
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress ->
                        proxy.initiate(CALL_GRAPH, proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator.toListRequest(request))
                                .makeServiceCall(CallWrapper.wrap(proxyClient.client()::listRunGroups, logger))
                                .done(Translator::fromListResponse));
    }
}
