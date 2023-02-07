package software.amazon.omics.annotationstore;

import software.amazon.awssdk.services.omics.model.CreateAnnotationStoreRequest;
import software.amazon.awssdk.services.omics.model.CreateAnnotationStoreResponse;
import software.amazon.awssdk.services.omics.model.GetAnnotationStoreRequest;
import software.amazon.awssdk.services.omics.model.GetAnnotationStoreResponse;
import software.amazon.awssdk.services.omics.model.StoreStatus;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.omics.common.AbstractTestBase;

import java.time.Instant;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    private String testName;
    private String testReferenceArn;
    private Instant testCreationTime;
    private String testId;
    private ReferenceItem testReferenceItem;
    private ResourceModel requestModel;
    private ResourceModel responseModel;


    @BeforeEach
    public void setup() {
        super.setup();

        testName = "test_annotation_store";
        testReferenceArn = "arn:aws::omics:test_id::test_reference_store/test_reference";
        testCreationTime = Instant.now();
        testId = "test_id";

        testReferenceItem = ReferenceItem.builder()
                .referenceArn(testReferenceArn)
                .build();

        requestModel = ResourceModel.builder()
                .name(testName)
                .reference(testReferenceItem)
                .creationTime(testCreationTime.toString())
                .id(testId)
                .build();

        CreateAnnotationStoreResponse mockCreateResponse = CreateAnnotationStoreResponse.builder()
                .name(testName)
                .reference(software.amazon.awssdk.services.omics.model.ReferenceItem
                        .fromReferenceArn(testReferenceArn))
                .creationTime(testCreationTime)
                .id(testId)
                .status(StoreStatus.ACTIVE)
                .build();

        when(proxyClient.client().createAnnotationStore(any(CreateAnnotationStoreRequest.class))).thenReturn(mockCreateResponse);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final CreateHandler handler = new CreateHandler();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        GetAnnotationStoreResponse mockGetResponse = GetAnnotationStoreResponse.builder()
                .id(testId)
                .name(testName)
                .reference(software.amazon.awssdk.services.omics.model.ReferenceItem
                        .fromReferenceArn(testReferenceArn))
                .status(StoreStatus.ACTIVE)
                .storeFormat("GFF")
                .creationTime(testCreationTime)
                .updateTime(Instant.now())
                .statusMessage("")
                .storeArn("arn:aws:omics::test_account:annotationStore/test_annotation_store")
                .storeSizeBytes(0L)
                .tags(new HashMap<>())
                .build();

        when(proxyClient.client().getAnnotationStore(any(GetAnnotationStoreRequest.class))).thenReturn(mockGetResponse);

        responseModel = Translator.fromReadResponse(mockGetResponse).getResourceModel();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, proxyClient, request, new CallbackContext(), logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(responseModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_MaxAttemptsReached() {
        final CreateHandler handler = new CreateHandler();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        GetAnnotationStoreResponse mockGetResponse = GetAnnotationStoreResponse.builder()
                .id(testId)
                .name(testName)
                .reference(software.amazon.awssdk.services.omics.model.ReferenceItem
                        .fromReferenceArn(testReferenceArn))
                .status(StoreStatus.CREATING)
                .storeFormat("VCF")
                .creationTime(testCreationTime)
                .updateTime(Instant.now())
                .statusMessage("")
                .storeArn("arn:aws:omics::test_account:annotationStore/test_annotation_store")
                .storeSizeBytes(0L)
                .tags(new HashMap<>())
                .build();

        when(proxyClient.client().getAnnotationStore(any(GetAnnotationStoreRequest.class))).thenReturn(mockGetResponse);

        responseModel = Translator.fromReadResponse(mockGetResponse).getResourceModel();

        assertThrows(CfnNotStabilizedException.class, ()-> {
            final ProgressEvent<ResourceModel, CallbackContext> response
                    = handler.handleRequest(proxy, proxyClient, request, new CallbackContext(1000L), logger);
        });
    }
}
