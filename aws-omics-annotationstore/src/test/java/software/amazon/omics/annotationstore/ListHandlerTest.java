package software.amazon.omics.annotationstore;

import software.amazon.awssdk.services.omics.model.ListAnnotationStoresRequest;
import software.amazon.awssdk.services.omics.model.ListAnnotationStoresResponse;
import software.amazon.awssdk.services.omics.model.ReferenceItem;
import software.amazon.awssdk.services.omics.model.StoreStatus;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
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

        final String testName = "test_annotation_store";
        final String testReferenceArn = "arn:aws::omics:test_id::test_reference_store/test_reference";
        final Instant testCreationTime = Instant.now();
        final String testId = "test_id";

        ListAnnotationStoresResponse mockResponse = ListAnnotationStoresResponse.builder()
                .annotationStores(store -> store
                        .id(testId)
                        .name(testName)
                        .reference(ReferenceItem.fromReferenceArn(testReferenceArn))
                        .creationTime(testCreationTime)
                        .updateTime(Instant.now())
                        .status(StoreStatus.ACTIVE)
                        .storeFormat("GFF")
                        .statusMessage("")
                        .storeSizeBytes(0L)
                        .storeArn("arn:aws:omics::test_account:annotationStore/test_annotation_store")
                        .build())
                .nextToken("testToken")
                .build();

        when(proxyClient.client().listAnnotationStores(any(ListAnnotationStoresRequest.class))).thenReturn(mockResponse);

        ResourceModel responseModel = Translator.fromListResponse(mockResponse).getResourceModels().get(0);

        final ResourceModel requestModel = ResourceModel.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, proxyClient, request, new CallbackContext(), logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels().size()).isEqualTo(1);
        assertThat(response.getResourceModels().get(0)).isEqualTo(responseModel);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
