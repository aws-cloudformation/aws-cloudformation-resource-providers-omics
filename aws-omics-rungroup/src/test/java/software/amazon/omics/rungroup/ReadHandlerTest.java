package software.amazon.omics.rungroup;

import java.time.Instant;
import java.util.HashMap;

import software.amazon.awssdk.services.omics.model.GetRunGroupRequest;
import software.amazon.awssdk.services.omics.model.GetRunGroupResponse;
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
public class ReadHandlerTest extends AbstractTestBase {
    @Test
    public void handleRequest_SimpleSuccess() {
        final ReadHandler handler = new ReadHandler();
        final Instant testInstant = Instant.now();

        final ResourceModel model = ResourceModel.builder()
                .id("TestId")
                .arn("TestArn")
                .name("GetRunGroupTest")
                .maxCpus(1.0)
                .maxRuns(1.0)
                .maxDuration(1.0)
                .creationTime(testInstant.toString())
                .tags(new HashMap<>())
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final GetRunGroupResponse mockResponse = GetRunGroupResponse.builder()
                .id("TestId")
                .arn("TestArn")
                .name("GetRunGroupTest")
                .maxCpus(1)
                .maxRuns(1)
                .maxDuration(1)
                .creationTime(testInstant)
                .tags(new HashMap<>())
                .build();

        when(proxyClient.client().getRunGroup(any(GetRunGroupRequest.class))).thenReturn(mockResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
