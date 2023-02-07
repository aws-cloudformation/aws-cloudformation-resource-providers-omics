package software.amazon.omics.variantstore;

import software.amazon.awssdk.services.omics.model.DeleteVariantStoreRequest;
import software.amazon.awssdk.services.omics.model.DeleteVariantStoreResponse;
import software.amazon.awssdk.services.omics.model.ListVariantStoresFilter;
import software.amazon.awssdk.services.omics.model.ListVariantStoresRequest;
import software.amazon.awssdk.services.omics.model.ListVariantStoresResponse;
import software.amazon.awssdk.services.omics.model.StoreStatus;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.omics.OmicsClient;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.omics.common.CallWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DeleteHandler extends BaseHandlerStd {
    private static final String CALL_GRAPH = "AWS-Omics-VariantStore::Delete";

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
                                .translateToServiceRequest(Translator::toDeleteRequest)
                                .backoffDelay(STABILIZATION_CONSTANT)
                                .makeServiceCall(CallWrapper.wrap(proxyClient.client()::deleteVariantStore, logger))
                                .stabilize(this::stabilizeOnDelete)
                                .progress())
                .then(progress ->
                        ProgressEvent.defaultSuccessHandler(null));
    }

    private Boolean stabilizeOnDelete(
            DeleteVariantStoreRequest request,
            DeleteVariantStoreResponse response,
            ProxyClient<OmicsClient> proxyClient,
            ResourceModel model,
            CallbackContext callbackContext) {

        if(Objects.isNull(callbackContext)) {
            callbackContext = new CallbackContext();
        } else {
            callbackContext.incrementAttempt();
        }

        List<String> storesBeingDeleted = listOfDeletingStores(proxyClient);

        if(storesBeingDeleted.contains(request.name()) && callbackContext.attempts() >= MAX_ATTEMPTS) {
            throw new CfnNotStabilizedException(new Throwable("Max attempts reached, determining as Timeout"));
        }

        return !storesBeingDeleted.contains(request.name());
    }

    private List<String> listOfDeletingStores(ProxyClient<OmicsClient> proxyClient) {
        List<String> storesBeingDeleted = new ArrayList<>();

        String nextToken = addStoresToListAndGetNextToken(proxyClient, storesBeingDeleted, null);
        while(nextToken != null) {
            nextToken = addStoresToListAndGetNextToken(proxyClient, storesBeingDeleted, nextToken);
        }

        return storesBeingDeleted;
    }

    private String addStoresToListAndGetNextToken(
            ProxyClient<OmicsClient> proxyClient,
            List<String> storesBeingDeleted,
            String token) {

        ListVariantStoresResponse initialResponse = makeListCall(proxyClient, null);

        String nextToken = initialResponse.nextToken();

        storesBeingDeleted.addAll(initialResponse.variantStores().stream()
                .map(store -> store.name()).collect(Collectors.toList()));

        return nextToken;
    }

    private ListVariantStoresResponse makeListCall(ProxyClient<OmicsClient> proxyClient, String token) {
        ListVariantStoresRequest listRequest = ListVariantStoresRequest.builder()
                .filter(ListVariantStoresFilter.builder()
                        .status(StoreStatus.DELETING)
                        .build())
                .nextToken(token)
                .build();
        return proxyClient.injectCredentialsAndInvokeV2(listRequest,
                proxyClient.client()::listVariantStores);
    }
}
