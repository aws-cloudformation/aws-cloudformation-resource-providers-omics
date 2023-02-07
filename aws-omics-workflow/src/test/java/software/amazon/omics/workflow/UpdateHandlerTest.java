package software.amazon.omics.workflow;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.omics.model.GetWorkflowRequest;
import software.amazon.awssdk.services.omics.model.GetWorkflowResponse;
import software.amazon.awssdk.services.omics.model.UpdateWorkflowRequest;
import software.amazon.awssdk.services.omics.model.UpdateWorkflowResponse;
import software.amazon.awssdk.services.omics.model.WorkflowStatus;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.omics.common.AbstractTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    @Test
    public void handleRequest_SimpleSuccess() {

        final UpdateHandler handler = new UpdateHandler();

        final UpdateWorkflowResponse mockResponse = UpdateWorkflowResponse.builder().build();
        when(proxyClient.client().updateWorkflow(any(UpdateWorkflowRequest.class)))
                .thenReturn(mockResponse);

        final GetWorkflowResponse mockGetResponse = GetWorkflowResponse.builder()
                .id("testId")
                .arn("testArn")
                .status(WorkflowStatus.ACTIVE)
                .build();
        when(proxyClient.client().getWorkflow(any(GetWorkflowRequest.class)))
                .thenReturn(mockGetResponse);

        final ResourceModel model = ResourceModel.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, proxyClient, request, new CallbackContext(), logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNotNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNotNull();
        assertEquals(response.getResourceModel().getId(), mockGetResponse.id());
        assertEquals(response.getResourceModel().getArn(), mockGetResponse.arn());
        assertEquals(response.getResourceModel().getStatus(), mockGetResponse.statusAsString());
        Assertions.assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
