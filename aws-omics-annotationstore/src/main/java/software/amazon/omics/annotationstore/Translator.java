package software.amazon.omics.annotationstore;

import software.amazon.awssdk.services.omics.model.CreateAnnotationStoreRequest;
import software.amazon.awssdk.services.omics.model.CreateAnnotationStoreResponse;
import software.amazon.awssdk.services.omics.model.DeleteAnnotationStoreRequest;
import software.amazon.awssdk.services.omics.model.DeleteAnnotationStoreResponse;
import software.amazon.awssdk.services.omics.model.GetAnnotationStoreRequest;
import software.amazon.awssdk.services.omics.model.GetAnnotationStoreResponse;
import software.amazon.awssdk.services.omics.model.ListAnnotationStoresRequest;
import software.amazon.awssdk.services.omics.model.ListAnnotationStoresResponse;
import software.amazon.awssdk.services.omics.model.ReferenceItem;
import software.amazon.awssdk.services.omics.model.SseConfig;
import software.amazon.awssdk.services.omics.model.StoreOptions;
import software.amazon.awssdk.services.omics.model.TsvStoreOptions;
import software.amazon.awssdk.services.omics.model.UpdateAnnotationStoreRequest;
import software.amazon.awssdk.services.omics.model.UpdateAnnotationStoreResponse;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Translator {

    public static Function<ResourceModel, CreateAnnotationStoreRequest> toCreateRequest(final ResourceHandlerRequest<ResourceModel> request) {
        return (ResourceModel model) -> CreateAnnotationStoreRequest.builder()
                .name(model.getName())
                .reference(Objects.isNull(model.getReference()) ? null : ReferenceItem.builder()
                        .referenceArn(model.getReference().getReferenceArn())
                        .build())
                .storeFormat(model.getStoreFormat())
                .storeOptions(Objects.isNull(model.getStoreOptions()) ? null : StoreOptions.builder()
                        .tsvStoreOptions(TsvStoreOptions.builder()
                                .formatToHeaderWithStrings(model.getStoreOptions().getTsvStoreOptions().getFormatToHeader())
                                .annotationType(model.getStoreOptions().getTsvStoreOptions().getAnnotationType())
                                .schemaWithStrings(model.getStoreOptions().getTsvStoreOptions().getSchema())
                                .build())
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

    public static ProgressEvent<ResourceModel, CallbackContext> fromCreateResponse(final CreateAnnotationStoreResponse response) {
        return ProgressEvent.defaultSuccessHandler(ResourceModel.builder()
                .creationTime(response.creationTime().toString())
                .id(response.id())
                .name(response.name())
                .reference(Objects.isNull(response.reference()) ? null :
                        software.amazon.omics.annotationstore.ReferenceItem.builder()
                                .referenceArn(response.reference().referenceArn())
                                .build())
                .storeFormat(response.storeFormatAsString())
                .storeOptions(Objects.isNull(response.storeOptions()) ? null :
                        software.amazon.omics.annotationstore.StoreOptions.builder()
                                .tsvStoreOptions(software.amazon.omics.annotationstore.TsvStoreOptions.builder()
                                        .annotationType(response.storeOptions().tsvStoreOptions().annotationTypeAsString())
                                        .formatToHeader(response.storeOptions().tsvStoreOptions().formatToHeaderAsStrings())
                                        .schema(response.storeOptions().tsvStoreOptions().schemaAsStrings())
                                        .build())
                                .build())
                .build());
    }

    public static GetAnnotationStoreRequest toReadRequest(final ResourceModel model) {
        return GetAnnotationStoreRequest.builder()
                .name(model.getName())
                .build();
    }

    public static ProgressEvent<ResourceModel, CallbackContext> fromReadResponse(final GetAnnotationStoreResponse response) {
        return ProgressEvent.defaultSuccessHandler(ResourceModel.builder()
                .name(response.name())
                .reference(Objects.isNull(response.reference()) ? null :
                        software.amazon.omics.annotationstore.ReferenceItem.builder()
                                .referenceArn(response.reference().referenceArn())
                                .build())
                .storeFormat(response.storeFormatAsString())
                .storeOptions(Objects.isNull(response.storeOptions()) ? null :
                        software.amazon.omics.annotationstore.StoreOptions.builder()
                                .tsvStoreOptions(software.amazon.omics.annotationstore.TsvStoreOptions.builder()
                                        .annotationType(response.storeOptions().tsvStoreOptions().annotationTypeAsString())
                                        .formatToHeader(response.storeOptions().tsvStoreOptions().formatToHeaderAsStrings())
                                        .schema(response.storeOptions().tsvStoreOptions().schemaAsStrings())
                                        .build())
                                .build())
                .creationTime(response.creationTime().toString())
                .updateTime(response.updateTime().toString())
                .sseConfig(Objects.isNull(response.sseConfig()) ? null :
                        software.amazon.omics.annotationstore.SseConfig.builder()
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

    public static UpdateAnnotationStoreRequest toUpdateRequest(final ResourceModel model) {
        return UpdateAnnotationStoreRequest.builder()
                .name(model.getName())
                .description(model.getDescription())
                .build();
    }

    public static ProgressEvent<ResourceModel, CallbackContext> fromUpdateResponse(final UpdateAnnotationStoreResponse response) {
        return ProgressEvent.defaultSuccessHandler(null);
    }

    public static DeleteAnnotationStoreRequest toDeleteRequest(final ResourceModel model) {
        return DeleteAnnotationStoreRequest.builder()
                .name(model.getName())
                .build();
    }

    public static ProgressEvent<ResourceModel, CallbackContext> fromDeleteResponse(final DeleteAnnotationStoreResponse response) {
        return ProgressEvent.defaultSuccessHandler(null);
    }

    public static Function<ResourceModel, ListAnnotationStoresRequest> toListRequest(final ResourceHandlerRequest<ResourceModel> request) {
        return (ResourceModel model) -> ListAnnotationStoresRequest.builder()
                .nextToken(request.getNextToken())
                .build();
    }

    public static ProgressEvent<ResourceModel, CallbackContext> fromListResponse(final ListAnnotationStoresResponse response) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(response.annotationStores().stream().map(annotationstore -> ResourceModel.builder()
                        .name(annotationstore.name())
                        .reference(Objects.isNull(annotationstore.reference()) ? null :
                                software.amazon.omics.annotationstore.ReferenceItem.builder()
                                        .referenceArn(annotationstore.reference().referenceArn())
                                        .build())
                        .storeFormat(annotationstore.storeFormatAsString())
                        .creationTime(annotationstore.creationTime().toString())
                        .updateTime(annotationstore.updateTime().toString())
                        .sseConfig(Objects.isNull(annotationstore.sseConfig()) ? null :
                                software.amazon.omics.annotationstore.SseConfig.builder()
                                        .keyArn(annotationstore.sseConfig().keyArn())
                                        .type(annotationstore.sseConfig().typeAsString())
                                        .build())
                        .description(Objects.isNull(annotationstore.description()) ? "" : annotationstore.description())
                        .id(annotationstore.id())
                        .status(annotationstore.statusAsString())
                        .statusMessage(annotationstore.statusMessage())
                        .storeArn(annotationstore.storeArn())
                        .storeSizeBytes(annotationstore.storeSizeBytes().doubleValue())
                        .build()).collect(Collectors.toList()))
                .nextToken(response.nextToken())
                .status(OperationStatus.SUCCESS)
                .build();
    }

}
