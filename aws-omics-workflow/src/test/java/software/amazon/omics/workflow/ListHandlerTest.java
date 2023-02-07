package software.amazon.omics.workflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.omics.model.ListWorkflowsRequest;
import software.amazon.awssdk.services.omics.model.ListWorkflowsResponse;
import software.amazon.awssdk.services.omics.model.WorkflowListItem;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.omics.common.AbstractTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {

    @Test
    public void handleRequest_SimpleSuccess() {

        final ListHandler handler = new ListHandler();

        final ListWorkflowsResponse mockResponse = ListWorkflowsResponse.builder()
                .nextToken("testToken")
                .items(WorkflowListItem.builder()
                        .arn("testArn")
                        .id("testId")
                        .status("testStatus")
                        .type("testType")
                        .build())
                .build();
        when(proxyClient.client().listWorkflows(any(ListWorkflowsRequest.class)))
                .thenReturn(mockResponse);

        final ResourceModel model = ResourceModel.builder()
                .arn(mockResponse.items().get(0).arn())
                .id(mockResponse.items().get(0).id())
                .status(mockResponse.items().get(0).statusAsString())
                .type(mockResponse.items().get(0).typeAsString())
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
}
