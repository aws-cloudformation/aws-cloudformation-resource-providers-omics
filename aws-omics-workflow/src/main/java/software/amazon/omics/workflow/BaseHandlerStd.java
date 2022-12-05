package software.amazon.omics.workflow;

import software.amazon.awssdk.services.omics.OmicsClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.omics.common.ClientBuilder;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

    protected static long STABILIZATION_TIMEOUT_IN_MINUTES = 10L;
    protected static long STABILIZATION_DELAY_IN_SECONDS = 60L;
    protected static long MAX_ATTEMPTS = STABILIZATION_TIMEOUT_IN_MINUTES * 60 / STABILIZATION_DELAY_IN_SECONDS;

    @Override
    public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        return handleRequest(
                proxy,
                proxy.newProxy(ClientBuilder::getClient),
                request,
                callbackContext != null ? callbackContext : new CallbackContext(),
                logger
        );
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<OmicsClient> proxyClient,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger);
}
