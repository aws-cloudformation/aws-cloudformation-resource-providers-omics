package software.amazon.omics.rungroup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.omics.model.ListRunGroupsRequest;
import software.amazon.awssdk.services.omics.model.ListRunGroupsResponse;
import software.amazon.awssdk.services.omics.model.RunGroupListItem;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.omics.common.AbstractTestBase;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {
    @Test
    public void handleRequest_SimpleSuccess() {
        final ListHandler handler = new ListHandler();
        final Instant now = Instant.now();

        final ListRunGroupsResponse mockResponse = ListRunGroupsResponse.builder()
                .nextToken("testToken")
                .items(RunGroupListItem.builder()
                        .id("testId")
                        .arn("testArn")
                        .name("testName")
                        .creationTime(now)
                        .maxCpus(1)
                        .maxDuration(1)
                        .maxRuns(1)
                        .build())
                .build();

        when(proxyClient.client().listRunGroups(any(ListRunGroupsRequest.class)))
                .thenReturn(mockResponse);

        final ResourceModel model = ResourceModel.builder()
                .id("testId")
                .arn("testArn")
                .name("testName")
                .creationTime(now.toString())
                .maxCpus(1.0)
                .maxDuration(1.0)
                .maxRuns(1.0)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
