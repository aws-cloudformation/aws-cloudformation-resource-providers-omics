package software.amazon.omics.variantstore;

import software.amazon.awssdk.services.omics.model.CreateVariantStoreRequest;
import software.amazon.awssdk.services.omics.model.CreateVariantStoreResponse;
import software.amazon.awssdk.services.omics.model.DeleteVariantStoreRequest;
import software.amazon.awssdk.services.omics.model.DeleteVariantStoreResponse;
import software.amazon.awssdk.services.omics.model.GetVariantStoreRequest;
import software.amazon.awssdk.services.omics.model.GetVariantStoreResponse;
import software.amazon.awssdk.services.omics.model.ListVariantStoresRequest;
import software.amazon.awssdk.services.omics.model.ListVariantStoresResponse;
import software.amazon.awssdk.services.omics.model.ReferenceItem;
import software.amazon.awssdk.services.omics.model.SseConfig;
import software.amazon.awssdk.services.omics.model.UpdateVariantStoreRequest;
import software.amazon.awssdk.services.omics.model.UpdateVariantStoreResponse;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Translator {

    public static Function<ResourceModel, CreateVariantStoreRequest> toCreateRequest(final ResourceHandlerRequest<ResourceModel> request) {
        return (ResourceModel model) -> CreateVariantStoreRequest.builder()
                .name(model.getName())
                .reference(ReferenceItem.builder()
                        .referenceArn(model.getReference().getReferenceArn())
                        .build())
                .description(model.getDescription())
                .sseConfig(Objects.isNull(model.getSseConfig()) ? null :
                        SseConfig.builder()
                        .keyArn(model.getSseConfig().getKeyArn())
                        .type(model.getSseConfig().getType())
                        .build())
                .tags(TagHelper.getNewDesiredTags(request))
                .build();
    }

    public static ProgressEvent<ResourceModel, CallbackContext> fromCreateResponse(final CreateVariantStoreResponse response) {
        return ProgressEvent.defaultSuccessHandler(ResourceModel.builder()
                .creationTime(response.creationTime().toString())
                .id(response.id())
                .name(response.name())
                .reference(software.amazon.omics.variantstore.ReferenceItem.builder()
                        .referenceArn(response.reference().referenceArn())
                        .build())
                .build());
    }

    public static GetVariantStoreRequest toReadRequest(final ResourceModel model) {
        return GetVariantStoreRequest.builder()
                .name(model.getName())
                .build();
    }

    public static ProgressEvent<ResourceModel, CallbackContext> fromReadResponse(final GetVariantStoreResponse response) {
        return ProgressEvent.defaultSuccessHandler(ResourceModel.builder()
                .name(response.name())
                .reference(software.amazon.omics.variantstore.ReferenceItem.builder()
                        .referenceArn(response.reference().referenceArn())
                        .build())
                .creationTime(response.creationTime().toString())
                .updateTime(response.updateTime().toString())
                .sseConfig(Objects.isNull(response.sseConfig()) ? null :
                        software.amazon.omics.variantstore.SseConfig.builder()
                                .keyArn(response.sseConfig().keyArn())
                                .type(response.sseConfig().typeAsString())
                                .build())
                .description(Objects.isNull(response.description()) ? "" : response.description())
                .id(response.id())
                .status(response.statusAsString())
                .statusMessage(response.statusMessage())
                .storeArn(response.storeArn())
                .storeSizeBytes(response.storeSizeBytes().doubleValue())
                .tags(response.tags())
                .build());
    }

    public static UpdateVariantStoreRequest toUpdateRequest(final ResourceModel model) {
        return UpdateVariantStoreRequest.builder()
                .name(model.getName())
                .description(model.getDescription())
                .build();
    }

    public static ProgressEvent<ResourceModel, CallbackContext> fromUpdateResponse(final UpdateVariantStoreResponse response) {
        return ProgressEvent.defaultSuccessHandler(null);
    }

    public static DeleteVariantStoreRequest toDeleteRequest(final ResourceModel model) {
        return DeleteVariantStoreRequest.builder()
                .name(model.getName())
                .build();
    }

    public static ProgressEvent<ResourceModel, CallbackContext> fromDeleteResponse(final DeleteVariantStoreResponse response) {
        return ProgressEvent.defaultSuccessHandler(null);
    }

    public static Function<ResourceModel, ListVariantStoresRequest> toListRequest(final ResourceHandlerRequest<ResourceModel> request) {
        return (ResourceModel model) -> ListVariantStoresRequest.builder()
                .nextToken(request.getNextToken())
                .build();
    }

    public static ProgressEvent<ResourceModel, CallbackContext> fromListResponse(final ListVariantStoresResponse response) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(response.variantStores().stream().map(variantStore -> ResourceModel.builder()
                        .name(variantStore.name())
                        .reference(software.amazon.omics.variantstore.ReferenceItem.builder()
                                .referenceArn(variantStore.reference().referenceArn())
                                .build())
                        .creationTime(variantStore.creationTime().toString())
                        .updateTime(variantStore.updateTime().toString())
                        .sseConfig(Objects.isNull(variantStore.sseConfig()) ? null :
                                software.amazon.omics.variantstore.SseConfig.builder()
                                        .keyArn(variantStore.sseConfig().keyArn())
                                        .type(variantStore.sseConfig().typeAsString())
                                        .build())
                        .description(Objects.isNull(variantStore.description()) ? "" : variantStore.description())
                        .id(variantStore.id())
                        .status(variantStore.statusAsString())
                        .statusMessage(variantStore.statusMessage())
                        .storeArn(variantStore.storeArn())
                        .storeSizeBytes(variantStore.storeSizeBytes().doubleValue())
                        .build()).collect(Collectors.toList()))
                .nextToken(response.nextToken())
                .status(OperationStatus.SUCCESS)
                .build();
    }

}
