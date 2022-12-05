package software.amazon.omics.annotationstore;

import software.amazon.awssdk.services.omics.model.AnnotationType;
import software.amazon.awssdk.services.omics.model.FormatToHeaderKey;
import software.amazon.awssdk.services.omics.model.GetAnnotationStoreRequest;
import software.amazon.awssdk.services.omics.model.GetAnnotationStoreResponse;
import software.amazon.awssdk.services.omics.model.ReferenceItem;
import software.amazon.awssdk.services.omics.model.StoreOptions;
import software.amazon.awssdk.services.omics.model.StoreStatus;
import software.amazon.awssdk.services.omics.model.TsvStoreOptions;
import software.amazon.awssdk.services.omics.model.UpdateAnnotationStoreRequest;
import software.amazon.awssdk.services.omics.model.UpdateAnnotationStoreResponse;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.omics.common.AbstractTestBase;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    @Test
    public void handleRequest_SimpleSuccess() {
        final UpdateHandler handler = new UpdateHandler();

        final String testName = "test_annotation_store";
        final String testReferenceArn = "arn:aws::omics:test_id::test_reference_store/test_reference";
        final Instant testCreationTime = Instant.now();
        final String testId = "test_id";
        final String testDescription = "test description for the store";

        UpdateAnnotationStoreResponse mockResponse = UpdateAnnotationStoreResponse.builder().build();

        when(proxyClient.client().updateAnnotationStore(any(UpdateAnnotationStoreRequest.class))).thenReturn(mockResponse);

        final ResourceModel requestModel = ResourceModel.builder()
                .name(testName)
                .description(testDescription)
                .build();

        GetAnnotationStoreResponse mockGetResponse = GetAnnotationStoreResponse.builder()
                .id(testId)
                .name(testName)
                .reference(ReferenceItem
                        .fromReferenceArn(testReferenceArn))
                .status(StoreStatus.ACTIVE)
                .storeFormat("TSV")
                .storeOptions(StoreOptions.builder()
                        .tsvStoreOptions(TsvStoreOptions.builder()
                                .formatToHeader(Map.of(FormatToHeaderKey.CHR, "CHROM",
                                        FormatToHeaderKey.POS, "POS"))
                                .schemaWithStrings(List.of(
                                        Map.of("CHROM", "STRING"),
                                        Map.of("POS", "LONG")
                                ))
                                .annotationType(AnnotationType.CHR_POS)
                                .build())
                        .build())
                .creationTime(testCreationTime)
                .updateTime(Instant.now())
                .statusMessage("")
                .storeArn("arn:aws:omics::test_account:annotationStore/test_annotation_store")
                .storeSizeBytes(0L)
                .tags(new HashMap<>())
                .build();

        when(proxyClient.client().getAnnotationStore(any(GetAnnotationStoreRequest.class))).thenReturn(mockGetResponse);

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
