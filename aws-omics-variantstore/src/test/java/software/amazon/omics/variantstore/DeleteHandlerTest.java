package software.amazon.omics.variantstore;

import software.amazon.awssdk.services.omics.model.*;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.omics.common.AbstractTestBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends AbstractTestBase {

    @Test
    public void handleRequest_SimpleSuccess() {
        final DeleteHandler handler = new DeleteHandler();

        final String testName = "test_variant_store";

        final ResourceModel model = ResourceModel.builder()
                .name(testName)
                .build();

        ListVariantStoresResponse mockListResponse =  ListVariantStoresResponse.builder()
                .variantStores(new ArrayList<>())
                .build();
        when(proxyClient.client().listVariantStores(any(ListVariantStoresRequest.class))).thenReturn(mockListResponse);

        DeleteVariantStoreResponse mockResponse = DeleteVariantStoreResponse.builder()
                .status(StoreStatus.DELETING)
                .build();
        when(proxyClient.client().deleteVariantStore(any(DeleteVariantStoreRequest.class))).thenReturn(mockResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, proxyClient, request, new CallbackContext(), logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_MaxAttemptsReached() {
        final DeleteHandler handler = new DeleteHandler();

        final String testName = "test_variant_store";

        final ResourceModel model = ResourceModel.builder()
                .name(testName)
                .build();

        ListVariantStoresResponse mockListResponse =  ListVariantStoresResponse.builder()
                .variantStores(Arrays.asList("test_variant_store")
                        .stream().map(name ->
                                VariantStoreItem.builder()
                                        .name(name)
                                        .status(StoreStatus.DELETING)
                                        .build())
                        .collect(Collectors.toList()))
                .build();
        when(proxyClient.client().listVariantStores(any(ListVariantStoresRequest.class))).thenReturn(mockListResponse);

        DeleteVariantStoreResponse mockResponse = DeleteVariantStoreResponse.builder()
                .status(StoreStatus.DELETING)
                .build();
        when(proxyClient.client().deleteVariantStore(any(DeleteVariantStoreRequest.class))).thenReturn(mockResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotStabilizedException.class, () -> {
            final ProgressEvent<ResourceModel, CallbackContext> response
                    = handler.handleRequest(proxy, proxyClient, request, new CallbackContext(1000L), logger);
        });

    }
}
