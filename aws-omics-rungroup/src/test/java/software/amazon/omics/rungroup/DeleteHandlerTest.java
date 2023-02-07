package software.amazon.omics.rungroup;

import java.time.Instant;

import software.amazon.awssdk.services.omics.model.DeleteRunGroupRequest;
import software.amazon.awssdk.services.omics.model.DeleteRunGroupResponse;
import software.amazon.awssdk.services.omics.model.GetRunGroupRequest;
import software.amazon.awssdk.services.omics.model.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.omics.common.AbstractTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends AbstractTestBase {

    @Test
    public void handleRequest_SimpleSuccess() {
        final DeleteHandler handler = new DeleteHandler();

        final Instant now = Instant.now();

        final ResourceModel model = ResourceModel.builder()
                .id("testId")
                .arn("testArn")
                .name("testName")
                .maxDuration(1.0)
                .maxRuns(1.0)
                .maxCpus(1.0)
                .creationTime(now.toString())
                .build();

        final DeleteRunGroupResponse mockResponse = DeleteRunGroupResponse.builder()
                .build();

        when(proxyClient.client().deleteRunGroup(any(DeleteRunGroupRequest.class))).thenReturn(mockResponse);
        when(proxyClient.client().getRunGroup(any(GetRunGroupRequest.class))).thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
