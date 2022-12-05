package software.amazon.omics.workflow;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.omics.model.GetWorkflowRequest;
import software.amazon.awssdk.services.omics.model.GetWorkflowResponse;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.omics.common.AbstractTestBase;

import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

    @Test
    public void handleRequest_SimpleSuccess() {

        final ReadHandler handler = new ReadHandler();

        final GetWorkflowResponse mockResponse = GetWorkflowResponse.builder()
                .id("testId")
                .arn("testArn")
                .name("testName")
                .description("testDescription")
                .storageCapacity(1)
                .creationTime(Instant.now())
                .tags(Collections.emptyMap())
                .parameterTemplate(Collections.emptyMap())
                .build();
        when(proxyClient.client().getWorkflow(any(GetWorkflowRequest.class)))
                .thenReturn(mockResponse);

        final ResourceModel model = ResourceModel.builder()
                .arn(mockResponse.arn())
                .id(mockResponse.id())
                .storageCapacity(mockResponse.storageCapacity().doubleValue())
                .tags(mockResponse.tags())
                .name(mockResponse.name())
                .description(mockResponse.description())
                .creationTime(mockResponse.creationTime().toString())
                .parameterTemplate(Translator.getParameterTemplate(mockResponse.parameterTemplate()))
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
}
