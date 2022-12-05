package software.amazon.omics.sequencestore;

import software.amazon.awssdk.services.omics.model.CreateSequenceStoreRequest;
import software.amazon.awssdk.services.omics.model.CreateSequenceStoreResponse;
import software.amazon.awssdk.services.omics.model.DeleteSequenceStoreRequest;
import software.amazon.awssdk.services.omics.model.DeleteSequenceStoreResponse;
import software.amazon.awssdk.services.omics.model.GetSequenceStoreRequest;
import software.amazon.awssdk.services.omics.model.GetSequenceStoreResponse;
import software.amazon.awssdk.services.omics.model.ListSequenceStoresRequest;
import software.amazon.awssdk.services.omics.model.ListSequenceStoresResponse;
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
     * @return createSequenceStoreRequest The request to create the resource
     */
    static Function<ResourceModel, CreateSequenceStoreRequest> toCreateRequest(final ResourceHandlerRequest<ResourceModel> request) {
        return (final ResourceModel model) -> {
            final CreateSequenceStoreRequest.Builder builder = CreateSequenceStoreRequest.builder()
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
    static ProgressEvent<ResourceModel, CallbackContext> fromCreateResponse(final CreateSequenceStoreResponse response) {
        final ResourceModel.ResourceModelBuilder builder = ResourceModel.builder()
                .arn(response.arn())
                .description(response.description())
                .creationTime(response.creationTime().toString())
                .name(response.name())
                .sequenceStoreId(response.id());

        if (response.sseConfig() != null) {
            builder.sseConfig(software.amazon.omics.sequencestore.SseConfig.builder()
                    .keyArn(response.sseConfig().keyArn())
                    .type(response.sseConfig().typeAsString()).build());
        }

        return ProgressEvent.defaultSuccessHandler(builder.build());
    }

    /**
     * Returns a function to translate model into a request to read a resource
     * @param model The resource model
     * @return createSequenceStoreRequest The request to read the resource
     */
    static GetSequenceStoreRequest toReadRequest(final ResourceModel model) {
        return GetSequenceStoreRequest.builder()
                .id(model.getSequenceStoreId())
                .build();
    }

    /**
     * Translates the response into a resource model
     * @param response The read resource response
     * @return model The resource model
     */
    static ProgressEvent<ResourceModel, CallbackContext> fromReadResponse(final GetSequenceStoreResponse response) {
        final ResourceModel.ResourceModelBuilder builder = ResourceModel.builder()
                .arn(response.arn())
                .description(response.description())
                .creationTime(response.creationTime().toString())
                .name(response.name())
                .sequenceStoreId(response.id());

        if (response.sseConfig() != null) {
            builder.sseConfig(software.amazon.omics.sequencestore.SseConfig.builder()
                    .keyArn(response.sseConfig().keyArn())
                    .type(response.sseConfig().typeAsString()).build());
        }

        return ProgressEvent.defaultSuccessHandler(builder.build());
    }

    /**
     * Returns a function to translate model into a request to list resources
     * @param request The CloudFormation Request
     * @return listSequenceStoresRequest The request to list resources
     */
    static Function<ResourceModel, ListSequenceStoresRequest> toListRequest(final ResourceHandlerRequest<ResourceModel> request) {
        return (final ResourceModel model) -> ListSequenceStoresRequest.builder()
                .nextToken(request.getNextToken())
                .build();
    }

    /**
     * Translates the response into a list of resource models
     * @param response The list resources response
     * @return model The resource model
     */
    static ProgressEvent<ResourceModel, CallbackContext> fromListResponse(final ListSequenceStoresResponse response) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(response.sequenceStores().stream().map(store -> {
                    final ResourceModel.ResourceModelBuilder builder = ResourceModel.builder()
                            .arn(store.arn())
                            .description(store.description())
                            .creationTime(store.creationTime().toString())
                            .name(store.name())
                            .sequenceStoreId(store.id());

                    if (store.sseConfig() != null) {
                        builder.sseConfig(software.amazon.omics.sequencestore.SseConfig.builder()
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
     * @return createSequenceStoreRequest The request to delete the resource
     */
    static DeleteSequenceStoreRequest toDeleteRequest(final ResourceModel model) {
        return DeleteSequenceStoreRequest.builder()
                .id(model.getSequenceStoreId())
                .build();
    }

    /**
     * Translates the response into a (null) resource model
     * @param response The delete resource response
     * @return model The resource model
     */
    static ProgressEvent<ResourceModel, CallbackContext> fromDeleteRequest(final DeleteSequenceStoreResponse response) {
        return ProgressEvent.defaultSuccessHandler(null);
    }
}
