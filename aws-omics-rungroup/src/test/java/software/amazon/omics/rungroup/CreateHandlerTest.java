package software.amazon.omics.rungroup;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.omics.model.CreateRunGroupRequest;
import software.amazon.awssdk.services.omics.model.CreateRunGroupResponse;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.omics.common.AbstractTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class CreateHandlerTest extends AbstractTestBase {
    @Test
    public void handleRequest_SimpleSuccess() {
        final CreateHandler handler = new CreateHandler();

        final CreateRunGroupResponse mockResponse = CreateRunGroupResponse.builder()
                .id("testId")
                .arn("testArn")
                .build();

        when(proxyClient.client().createRunGroup(any(CreateRunGroupRequest.class)))
                .thenReturn(mockResponse);

        final ResourceModel model = ResourceModel.builder()
                .name("TestRunGroupName")
                .maxCpus(1.0)
                .maxDuration(100.0)
                .maxRuns(2.0)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel().getId()).isEqualTo(mockResponse.id());
        assertThat(response.getResourceModel().getArn()).isEqualTo(mockResponse.arn());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
