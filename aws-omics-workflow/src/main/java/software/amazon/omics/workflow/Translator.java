package software.amazon.omics.workflow;

import software.amazon.awssdk.services.omics.model.CreateWorkflowRequest;
import software.amazon.awssdk.services.omics.model.CreateWorkflowResponse;
import software.amazon.awssdk.services.omics.model.DeleteWorkflowRequest;
import software.amazon.awssdk.services.omics.model.DeleteWorkflowResponse;
import software.amazon.awssdk.services.omics.model.GetWorkflowRequest;
import software.amazon.awssdk.services.omics.model.GetWorkflowResponse;
import software.amazon.awssdk.services.omics.model.ListWorkflowsRequest;
import software.amazon.awssdk.services.omics.model.ListWorkflowsResponse;
import software.amazon.awssdk.services.omics.model.UpdateWorkflowRequest;
import software.amazon.awssdk.services.omics.model.UpdateWorkflowResponse;
import software.amazon.awssdk.services.omics.model.WorkflowParameter;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Translator {

    static Function<ResourceModel, CreateWorkflowRequest> toCreateRequest(final ResourceHandlerRequest<ResourceModel> request) {
        return (final ResourceModel model) -> CreateWorkflowRequest.builder()
                .name(model.getName())
                .description(model.getDescription())
                .engine(model.getEngine())
                .definitionUri(model.getDefinitionUri())
                .main(model.getMain())
                .parameterTemplate(getParameterTemplate(model))
                .storageCapacity(Objects.isNull(model.getStorageCapacity()) ? null : model.getStorageCapacity().intValue())
                .tags(TagHelper.getNewDesiredTags(request))
                .build();
    }

    static ProgressEvent<ResourceModel, CallbackContext> fromCreateResponse(final CreateWorkflowResponse response) {
        return ProgressEvent.defaultSuccessHandler(ResourceModel.builder()
                .arn(response.arn())
                .id(response.id())
                .status(response.statusAsString())
                .tags(response.tags())
                .build());
    }

    static GetWorkflowRequest translateToReadRequest(final ResourceModel model) {
        return GetWorkflowRequest.builder()
                .id(model.getId())
                .type(model.getType())
                .build();
    }

    static ProgressEvent<ResourceModel, CallbackContext> fromReadResponse(final GetWorkflowResponse response) {
        return ProgressEvent.defaultSuccessHandler(ResourceModel.builder()
                .arn(response.arn())
                .id(response.id())
                .status(response.statusAsString())
                .type(response.typeAsString())
                .name(response.name())
                .description(response.description())
                .engine(response.engineAsString())
                .main(response.main())
                .parameterTemplate(getParameterTemplate(response.parameterTemplate()))
                .storageCapacity(Objects.isNull(response.storageCapacity()) ? null : response.storageCapacity().doubleValue())
                .creationTime(Objects.isNull(response.creationTime()) ? null : response.creationTime().toString())
                .tags(response.tags())
                .build());
    }

    static DeleteWorkflowRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteWorkflowRequest
                .builder()
                .id(model.getId())
                .build();
    }

    static ProgressEvent<ResourceModel, CallbackContext> fromDeleteResponse(final DeleteWorkflowResponse response) {
        return ProgressEvent.defaultSuccessHandler(null);
    }

    static UpdateWorkflowRequest translateToUpdateRequest(final ResourceModel model) {
        return UpdateWorkflowRequest.builder()
                .id(model.getId())
                .name(model.getName())
                .description(model.getDescription())
                .build();
    }

    static ProgressEvent<ResourceModel, CallbackContext> fromUpdateResponse(final UpdateWorkflowResponse response) {
        return ProgressEvent.defaultSuccessHandler(null);
    }

    static Function<ResourceModel, ListWorkflowsRequest> translateToListRequest(final ResourceHandlerRequest<ResourceModel> request) {
        return (final ResourceModel model) -> ListWorkflowsRequest.builder()
                .type(model.getType())
                .name(model.getName())
                .startingToken(request.getNextToken())
                .build();
    }

    static ProgressEvent<ResourceModel, CallbackContext> fromListResponse(final ListWorkflowsResponse response) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(response.items().stream().map(workflowListItem -> ResourceModel.builder()
                        .arn(workflowListItem.arn())
                        .id(workflowListItem.id())
                        .type(workflowListItem.typeAsString())
                        .name(workflowListItem.name())
                        .status(workflowListItem.statusAsString())
                        .creationTime(Objects.isNull(workflowListItem.creationTime()) ? null : workflowListItem.creationTime().toString())
                        .build()).collect(Collectors.toList()))
                .nextToken(response.nextToken())
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private static Map<String, WorkflowParameter> getParameterTemplate(ResourceModel model) {
        if (model.getParameterTemplate() == null || model.getParameterTemplate().isEmpty()) {
            return null;
        }

        return model.getParameterTemplate()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> WorkflowParameter.builder()
                                .description(e.getValue().getDescription())
                                .optional(e.getValue().getOptional())
                                .build())
                );
    }

    public static Map<String, software.amazon.omics.workflow.WorkflowParameter> getParameterTemplate(
            Map<String, WorkflowParameter> parameterTemplate) {

        return Optional.ofNullable(parameterTemplate)
                .orElse(Collections.emptyMap())
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> software.amazon.omics.workflow.WorkflowParameter.builder()
                                .description(e.getValue().description())
                                .optional(e.getValue().optional())
                                .build())
                );
    }
}
