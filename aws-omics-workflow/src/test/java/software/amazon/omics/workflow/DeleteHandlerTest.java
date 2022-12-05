package software.amazon.omics.workflow;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.omics.model.DeleteWorkflowRequest;
import software.amazon.awssdk.services.omics.model.DeleteWorkflowResponse;
import software.amazon.awssdk.services.omics.model.GetWorkflowRequest;
import software.amazon.awssdk.services.omics.model.GetWorkflowResponse;
import software.amazon.awssdk.services.omics.model.ResourceNotFoundException;
import software.amazon.awssdk.services.omics.model.WorkflowStatus;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.omics.common.AbstractTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends AbstractTestBase {

    @Test
    public void handleRequest_SimpleSuccess() {

        final DeleteHandler handler = new DeleteHandler();

        final DeleteWorkflowResponse mockResponse = DeleteWorkflowResponse.builder().build();
        when(proxyClient.client().deleteWorkflow(any(DeleteWorkflowRequest.class)))
                .thenReturn(mockResponse);

        when(proxyClient.client().getWorkflow(any(GetWorkflowRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceModel model = ResourceModel.builder().build();
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
        Assertions.assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

    }
}
