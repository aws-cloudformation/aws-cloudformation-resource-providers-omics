package software.amazon.omics.variantstore;

import software.amazon.awssdk.services.omics.model.GetVariantStoreRequest;
import software.amazon.awssdk.services.omics.model.GetVariantStoreResponse;
import software.amazon.awssdk.services.omics.model.StoreStatus;
import software.amazon.awssdk.services.omics.model.UpdateVariantStoreRequest;
import software.amazon.awssdk.services.omics.model.UpdateVariantStoreResponse;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.omics.common.AbstractTestBase;

import java.time.Instant;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    @Test
    public void handleRequest_SimpleSuccess() {
        final UpdateHandler handler = new UpdateHandler();

        final String testName = "test_variant_store";
        final String testDescription = "test description for the store";
        final String testReferenceArm = "arn:aws::omics:test_id::test_reference_store/test_reference";
        final Instant testCreationTime = Instant.now();
        final String testId = "test_id";

        UpdateVariantStoreResponse mockResponse = UpdateVariantStoreResponse.builder().build();

        when(proxyClient.client().updateVariantStore(any(UpdateVariantStoreRequest.class))).thenReturn(mockResponse);

        final ResourceModel requestModel = ResourceModel.builder()
                .name(testName)
                .description(testDescription)
                .build();

        GetVariantStoreResponse mockGetResponse = GetVariantStoreResponse.builder()
                .id(testId)
                .name(testName)
                .reference(software.amazon.awssdk.services.omics.model.ReferenceItem
                        .fromReferenceArn(testReferenceArm))
                .description(testDescription)
                .status(StoreStatus.ACTIVE)
                .creationTime(testCreationTime)
                .updateTime(Instant.now())
                .statusMessage("")
                .storeArn("arn:aws:omics::test_account:variantStore/test_name")
                .storeSizeBytes(0L)
                .tags(new HashMap<>())
                .build();

        when(proxyClient.client().getVariantStore(any(GetVariantStoreRequest.class))).thenReturn(mockGetResponse);

        ResourceModel responseModel = Translator.fromReadResponse(mockGetResponse).getResourceModel();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(requestModel)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, proxyClient, request, new CallbackContext(), logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNotNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(responseModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
