package software.amazon.omics.referencestore;

import software.amazon.awssdk.services.omics.model.CreateReferenceStoreRequest;
import software.amazon.awssdk.services.omics.model.CreateReferenceStoreResponse;
import software.amazon.awssdk.services.omics.model.DeleteReferenceStoreRequest;
import software.amazon.awssdk.services.omics.model.DeleteReferenceStoreResponse;
import software.amazon.awssdk.services.omics.model.GetReferenceStoreRequest;
import software.amazon.awssdk.services.omics.model.GetReferenceStoreResponse;
import software.amazon.awssdk.services.omics.model.ListReferenceStoresRequest;
import software.amazon.awssdk.services.omics.model.ListReferenceStoresResponse;
import software.amazon.awssdk.services.omics.model.SseConfig;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.function.Function;
import java.util.stream.Collectors;

public class Translator {
    /**
     * Returns a function to translate model into a request to create a resource
     * @param request The CloudFormation Request
     * @return createReferenceStoreRequest The request to create the resource
     */
    static Function<ResourceModel, CreateReferenceStoreRequest> toCreateRequest(final ResourceHandlerRequest<ResourceModel> request) {
        return (final ResourceModel model) -> {
            final CreateReferenceStoreRequest.Builder builder = CreateReferenceStoreRequest.builder()
                    .name(model.getName())
                    .description(model.getDescription())
                    .clientToken(request.getClientRequestToken())
                    .tags(TagHelper.getNewDesiredTags(request));

            if (model.getSseConfig() != null) {
                builder.sseConfig(SseConfig.builder()
                        .type(model.getSseConfig().getType())
                        .keyArn(model.getSseConfig().getKeyArn())
                        .build()
                );
            }

            return builder.build();
        };
    }

    /**
     * Translates the response into a resource model
     * @param response The create resource response
     * @return model The resource model
     */
    static ProgressEvent<ResourceModel, CallbackContext> fromCreateResponse(final CreateReferenceStoreResponse response) {
        final ResourceModel.ResourceModelBuilder builder = ResourceModel.builder()
                .arn(response.arn())
                .description(response.description())
                .creationTime(response.creationTime().toString())
                .name(response.name())
                .referenceStoreId(response.id());

        if (response.sseConfig() != null) {
            builder.sseConfig(software.amazon.omics.referencestore.SseConfig.builder()
                    .keyArn(response.sseConfig().keyArn())
                    .type(response.sseConfig().typeAsString()).build());
        }

        return ProgressEvent.defaultSuccessHandler(builder.build());
    }

    /**
     * Returns a function to translate model into a request to read a resource
     * @param model The resource model
     * @return createReferenceStoreRequest The request to read the resource
     */
    static GetReferenceStoreRequest toReadRequest(final ResourceModel model) {
        return GetReferenceStoreRequest.builder()
                .id(model.getReferenceStoreId())
                .build();
    }

    /**
     * Translates the response into a resource model
     * @param response The read resource response
     * @return model The resource model
     */
    static ProgressEvent<ResourceModel, CallbackContext> fromReadResponse(final GetReferenceStoreResponse response) {
        final ResourceModel.ResourceModelBuilder builder = ResourceModel.builder()
                .arn(response.arn())
                .description(response.description())
                .creationTime(response.creationTime().toString())
                .name(response.name())
                .referenceStoreId(response.id());

        if (response.sseConfig() != null) {
            builder.sseConfig(software.amazon.omics.referencestore.SseConfig.builder()
                    .keyArn(response.sseConfig().keyArn())
                    .type(response.sseConfig().typeAsString()).build());
        }

        return ProgressEvent.defaultSuccessHandler(builder.build());
    }

    /**
     * Returns a function to translate model into a request to list resources
     * @param request The CloudFormation Request
     * @return listReferenceStoresRequest The request to list resources
     */
    static Function<ResourceModel, ListReferenceStoresRequest> toListRequest(final ResourceHandlerRequest<ResourceModel> request) {
        return (final ResourceModel model) -> ListReferenceStoresRequest.builder()
                .nextToken(request.getNextToken())
                .build();
    }

    /**
     * Translates the response into a list of resource models
     * @param response The list resources response
     * @return model The resource model
     */
    static ProgressEvent<ResourceModel, CallbackContext> fromListResponse(final ListReferenceStoresResponse response) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(response.referenceStores().stream().map(store -> {
                    final ResourceModel.ResourceModelBuilder builder = ResourceModel.builder()
                            .arn(store.arn())
                            .description(store.description())
                            .creationTime(store.creationTime().toString())
                            .name(store.name())
                            .referenceStoreId(store.id());

                    if (store.sseConfig() != null) {
                        builder.sseConfig(software.amazon.omics.referencestore.SseConfig.builder()
                                .keyArn(store.sseConfig().keyArn())
                                .type(store.sseConfig().typeAsString()).build());
                    }

                    return builder.build();
                }).collect(Collectors.toList()))
                .nextToken(response.nextToken())
                .status(OperationStatus.SUCCESS)
                .build();
    }

    /**
     * Returns a function to translate model into a request to delete a resource
     * @param model The resource model
     * @return createReferenceStoreRequest The request to delete the resource
     */
    static DeleteReferenceStoreRequest toDeleteRequest(final ResourceModel model) {
        return DeleteReferenceStoreRequest.builder()
                .id(model.getReferenceStoreId())
                .build();
    }

    /**
     * Translates the response into a (null) resource model
     * @param response The delete resource response
     * @return model The resource model
     */
    static ProgressEvent<ResourceModel, CallbackContext> fromDeleteRequest(final DeleteReferenceStoreResponse response) {
        return ProgressEvent.defaultSuccessHandler(null);
    }
}
