package software.amazon.omics.variantstore;

import software.amazon.awssdk.services.omics.OmicsClient;
import software.amazon.awssdk.services.omics.model.CreateVariantStoreRequest;
import software.amazon.awssdk.services.omics.model.CreateVariantStoreResponse;
import software.amazon.awssdk.services.omics.model.StoreStatus;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.omics.common.CallWrapper;
import java.util.Objects;

public class CreateHandler extends BaseHandlerStd {

    private static final String CALL_GRAPH = "AWS-Omics-VariantStore::Create";

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
                                .backoffDelay(STABILIZATION_CONSTANT)
                                .makeServiceCall(CallWrapper.wrap(proxyClient.client()::createVariantStore, logger))
                                .stabilize(this::stabilizeOnCreate)
                                .progress())
                .then(progress -> new ReadHandler().handleRequest(proxy, proxyClient, request, callbackContext, logger));
    }

    private Boolean stabilizeOnCreate(
            CreateVariantStoreRequest request,
            CreateVariantStoreResponse response,
            ProxyClient<OmicsClient> proxyClient,
            ResourceModel model,
            CallbackContext callbackContext) {

        model.setId(response.id());

        if(Objects.isNull(callbackContext)) {
            callbackContext = new CallbackContext();
        } else {
            callbackContext.incrementAttempt();
        }

        StoreStatus status =
                proxyClient.injectCredentialsAndInvokeV2(Translator.toReadRequest(model),
                        proxyClient.client()::getVariantStore).status();

        if(Objects.nonNull(status) && status != StoreStatus.ACTIVE && callbackContext.attempts() >= MAX_ATTEMPTS) {
            throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getStoreArn(), new Throwable("Max attempts reached, determining as Timeout"));
        }

        model.setStatus(Objects.isNull(status) ? null: status.toString());

        return status == StoreStatus.ACTIVE;
    }
}
