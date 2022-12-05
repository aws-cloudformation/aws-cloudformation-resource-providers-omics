package software.amazon.omics.sequencestore;

import org.assertj.core.api.Assertions;
import software.amazon.awssdk.services.omics.model.CreateSequenceStoreRequest;
import software.amazon.awssdk.services.omics.model.CreateSequenceStoreResponse;
import software.amazon.awssdk.services.omics.model.EncryptionType;
import software.amazon.awssdk.services.omics.model.SseConfig;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.Test;
import software.amazon.omics.common.AbstractTestBase;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

public class CreateHandlerTest extends AbstractTestBase {
    @Test
    public void handleRequest_SimpleSuccess() {
        final CreateHandler handler = new CreateHandler();

        final CreateSequenceStoreResponse mockResponse = CreateSequenceStoreResponse.builder()
                .id("testId")
                .arn("testArn")
                .name("testName")
                .description("testDescription")
                .creationTime(Instant.now())
                .sseConfig(SseConfig.builder().type(EncryptionType.KMS).keyArn("testArn").build())
                .build();
        when(proxyClient.client().createSequenceStore(any(CreateSequenceStoreRequest.class)))
                .thenReturn(mockResponse);

        final ResourceModel model = ResourceModel.builder()
                .sequenceStoreId(mockResponse.id())
                .arn(mockResponse.arn())
                .name(mockResponse.name())
                .description(mockResponse.description())
                .creationTime(mockResponse.creationTime().toString())
                .sseConfig(software.amazon.omics.sequencestore.SseConfig.builder()
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
        final CreateHandler handler = new CreateHandler();

        final CreateSequenceStoreResponse mockResponse = CreateSequenceStoreResponse.builder()
                .id("testId")
                .arn("testArn")
                .name("testName")
                .description("testDescription")
                .creationTime(Instant.now())
                .build();
        when(proxyClient.client().createSequenceStore(any(CreateSequenceStoreRequest.class)))
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
