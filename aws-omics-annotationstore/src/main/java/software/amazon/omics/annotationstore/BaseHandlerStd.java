package software.amazon.omics.annotationstore;

import software.amazon.awssdk.services.omics.OmicsClient;
import software.amazon.cloudformation.proxy.delay.Constant;
import software.amazon.omics.common.ClientBuilder;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

    protected static long STABILIZATION_TIMEOUT_IN_MINUTES = 20L;
    protected static long STABILIZATION_DELAY_IN_SECONDS = 20L;
    protected static long MAX_ATTEMPTS = STABILIZATION_TIMEOUT_IN_MINUTES * 60 / STABILIZATION_DELAY_IN_SECONDS;

    protected static Constant STABILIZATION_CONSTANT = Constant.of().timeout(Duration.ofMinutes(STABILIZATION_TIMEOUT_IN_MINUTES))
            .delay(Duration.ofSeconds(STABILIZATION_DELAY_IN_SECONDS)).build();

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