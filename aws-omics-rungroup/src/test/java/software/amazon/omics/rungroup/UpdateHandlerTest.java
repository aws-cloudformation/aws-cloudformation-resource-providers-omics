package software.amazon.omics.rungroup;

import java.time.Instant;
import java.util.HashMap;

import software.amazon.awssdk.services.omics.model.GetRunGroupRequest;
import software.amazon.awssdk.services.omics.model.GetRunGroupResponse;
import software.amazon.awssdk.services.omics.model.UpdateRunGroupRequest;
import software.amazon.awssdk.services.omics.model.UpdateRunGroupResponse;
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
public class UpdateHandlerTest extends AbstractTestBase {
    @Test
    public void handleRequest_SimpleSuccess() {
        final UpdateHandler handler = new UpdateHandler();
        final Instant now = Instant.now();

        final ResourceModel model = ResourceModel.builder()
                .arn("testArn")
                .id("testId")
                .name("testName")
                .maxDuration(1.0)
                .maxRuns(1.0)
                .maxCpus(1.0)
                .creationTime(now.toString())
                .tags(new HashMap<>())
                .build();

        final UpdateRunGroupResponse mockUpdateResponse = UpdateRunGroupResponse.builder()
                .build();
        when(proxyClient.client().updateRunGroup(any(UpdateRunGroupRequest.class))).thenReturn(mockUpdateResponse);

        final GetRunGroupResponse mockReadResponse = GetRunGroupResponse.builder()
                .id(model.getId())
                .arn(model.getArn())
                .name(model.getName())
                .maxCpus(model.getMaxCpus().intValue())
                .maxRuns(model.getMaxRuns().intValue())
                .maxDuration(model.getMaxDuration().intValue())
                .creationTime(now)
                .tags(model.getTags())
                .build();

        when(proxyClient.client().getRunGroup(any(GetRunGroupRequest.class))).thenReturn(mockReadResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
