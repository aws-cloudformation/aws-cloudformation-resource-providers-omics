package software.amazon.omics.workflow;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.omics.model.CreateWorkflowRequest;
import software.amazon.awssdk.services.omics.model.CreateWorkflowResponse;
import software.amazon.awssdk.services.omics.model.GetWorkflowRequest;
import software.amazon.awssdk.services.omics.model.GetWorkflowResponse;
import software.amazon.awssdk.services.omics.model.WorkflowStatus;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.omics.common.AbstractTestBase;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    @Test
    public void handleRequest_SimpleSuccess() {
        final CreateHandler handler = new CreateHandler();

        final CreateWorkflowResponse mockResponse = CreateWorkflowResponse.builder()
                .id("testId")
                .arn("testArn")
                .status(WorkflowStatus.ACTIVE)
                .tags(new HashMap<>())
                .build();
        when(proxyClient.client().createWorkflow(any(CreateWorkflowRequest.class)))
                .thenReturn(mockResponse);

        final GetWorkflowResponse mockGetResponse = GetWorkflowResponse.builder()
                        .id(mockResponse.id())
                        .arn(mockResponse.arn())
                        .status(mockResponse.status())
                        .build();
        when(proxyClient.client().getWorkflow(any(GetWorkflowRequest.class)))
                .thenReturn(mockGetResponse);

        final ResourceModel model = ResourceModel.builder()
                .id(mockResponse.id())
                .arn(mockResponse.arn())
                .status(mockResponse.statusAsString())
                .tags(mockResponse.tags())
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
