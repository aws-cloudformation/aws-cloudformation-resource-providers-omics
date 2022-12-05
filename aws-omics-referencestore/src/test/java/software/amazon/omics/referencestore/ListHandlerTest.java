package software.amazon.omics.referencestore;

import software.amazon.awssdk.services.omics.model.EncryptionType;
import software.amazon.awssdk.services.omics.model.ListReferenceStoresRequest;
import software.amazon.awssdk.services.omics.model.ListReferenceStoresResponse;
import software.amazon.awssdk.services.omics.model.ReferenceStoreDetail;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.omics.common.AbstractTestBase;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {
    @Test
    public void handleRequest_SimpleSuccess() {
        final ListHandler handler = new ListHandler();

        final ListReferenceStoresResponse mockResponse = ListReferenceStoresResponse.builder()
                .nextToken("testToken")
                .referenceStores(ReferenceStoreDetail.builder()
                        .id("testId")
                        .arn("testArn")
                        .name("testName")
                        .description("testDescription")
                        .creationTime(Instant.now())
                        .sseConfig(software.amazon.awssdk.services.omics.model.SseConfig.builder().type(EncryptionType.KMS).keyArn("testArn").build())
                        .build())
                .build();
        when(proxyClient.client().listReferenceStores(any(ListReferenceStoresRequest.class)))
                .thenReturn(mockResponse);

        final ResourceModel model = ResourceModel.builder()
                .referenceStoreId(mockResponse.referenceStores().get(0).id())
                .arn(mockResponse.referenceStores().get(0).arn())
                .name(mockResponse.referenceStores().get(0).name())
                .description(mockResponse.referenceStores().get(0).description())
                .creationTime(mockResponse.referenceStores().get(0).creationTime().toString())
                .sseConfig(software.amazon.omics.referencestore.SseConfig.builder()
                        .type(mockResponse.referenceStores().get(0).sseConfig().typeAsString())
                        .keyArn(mockResponse.referenceStores().get(0).sseConfig().keyArn())
                        .build())
                .build();

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
        assertThat(response.getResourceModels().size()).isEqualTo(1);
        assertThat(response.getResourceModels().get(0)).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_WorksWithNullSseConfig() {
        final ListHandler handler = new ListHandler();

        final ListReferenceStoresResponse mockResponse = ListReferenceStoresResponse.builder()
                .nextToken("testToken")
                .referenceStores(ReferenceStoreDetail.builder()
                        .id("testId")
                        .arn("testArn")
                        .name("testName")
                        .description("testDescription")
                        .creationTime(Instant.now())
                        .build())
                .build();
        when(proxyClient.client().listReferenceStores(any(ListReferenceStoresRequest.class)))
                .thenReturn(mockResponse);

        final ResourceModel model = ResourceModel.builder()
                .referenceStoreId(mockResponse.referenceStores().get(0).id())
                .arn(mockResponse.referenceStores().get(0).arn())
                .name(mockResponse.referenceStores().get(0).name())
                .description(mockResponse.referenceStores().get(0).description())
                .creationTime(mockResponse.referenceStores().get(0).creationTime().toString())
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, proxyClient, request, new CallbackContext(), logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModels().size()).isEqualTo(1);
        assertThat(response.getResourceModels().get(0)).isEqualTo(request.getDesiredResourceState());
    }
}
