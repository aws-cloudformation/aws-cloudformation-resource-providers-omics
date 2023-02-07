package software.amazon.omics.sequencestore;

import org.assertj.core.api.Assertions;
import software.amazon.awssdk.services.omics.model.EncryptionType;
import software.amazon.awssdk.services.omics.model.GetSequenceStoreRequest;
import software.amazon.awssdk.services.omics.model.GetSequenceStoreResponse;
import software.amazon.awssdk.services.omics.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.omics.model.ListTagsForResourceResponse;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.omics.common.AbstractTestBase;

import java.time.Instant;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {
    @Test
    public void handleRequest_SimpleSuccess() {
        final ReadHandler handler = new ReadHandler();

        final GetSequenceStoreResponse mockResponse = GetSequenceStoreResponse.builder()
                .id("testId")
                .arn("testArn")
                .name("testName")
                .description("testDescription")
                .creationTime(Instant.now())
                .sseConfig(software.amazon.awssdk.services.omics.model.SseConfig.builder().type(EncryptionType.KMS).keyArn("testArn").build())
                .build();
        when(proxyClient.client().getSequenceStore(any(GetSequenceStoreRequest.class)))
                .thenReturn(mockResponse);

        final ResourceModel model = ResourceModel.builder()
                .sequenceStoreId(mockResponse.id())
                .arn(mockResponse.arn())
                .name(mockResponse.name())
                .description(mockResponse.description())
                .creationTime(mockResponse.creationTime().toString())
                .sseConfig(SseConfig.builder()
                        .type(mockResponse.sseConfig().typeAsString())
                        .keyArn(mockResponse.sseConfig().keyArn())
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
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        Assertions.assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_WorksWithNullSseConfig() {
        final ReadHandler handler = new ReadHandler();

        final GetSequenceStoreResponse mockResponse = GetSequenceStoreResponse.builder()
                .id("testId")
                .arn("testArn")
                .name("testName")
                .description("testDescription")
                .creationTime(Instant.now())
                .build();
        when(proxyClient.client().getSequenceStore(any(GetSequenceStoreRequest.class)))
                .thenReturn(mockResponse);

        final ResourceModel model = ResourceModel.builder()
                .sequenceStoreId(mockResponse.id())
                .arn(mockResponse.arn())
                .name(mockResponse.name())
                .description(mockResponse.description())
                .creationTime(mockResponse.creationTime().toString())
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, proxyClient, request, new CallbackContext(), logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
    }
}
