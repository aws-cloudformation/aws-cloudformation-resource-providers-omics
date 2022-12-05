package software.amazon.omics.rungroup;

import software.amazon.awssdk.services.omics.model.CreateRunGroupRequest;
import software.amazon.awssdk.services.omics.model.CreateRunGroupResponse;
import software.amazon.awssdk.services.omics.model.DeleteRunGroupRequest;
import software.amazon.awssdk.services.omics.model.DeleteRunGroupResponse;
import software.amazon.awssdk.services.omics.model.GetRunGroupRequest;
import software.amazon.awssdk.services.omics.model.GetRunGroupResponse;
import software.amazon.awssdk.services.omics.model.ListRunGroupsRequest;
import software.amazon.awssdk.services.omics.model.ListRunGroupsResponse;
import software.amazon.awssdk.services.omics.model.UpdateRunGroupRequest;
import software.amazon.awssdk.services.omics.model.UpdateRunGroupResponse;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class is a centralized placeholder for
 * - api request construction
 * - object translation to/from aws sdk
 * - resource model construction for read/list handlers
 */

public class Translator {

    /**
     * Returns a function to translate model into a request to create a resource
     *
     * @param request The CloudFormation Request
     * @return createRunGroupRequest The request to create the resource
     */
    static Function<ResourceModel, CreateRunGroupRequest> toCreateRequest(final ResourceHandlerRequest<ResourceModel> request) {
        return (final ResourceModel model) -> CreateRunGroupRequest.builder()
                .name(model.getName())
                .maxCpus(Objects.isNull(model.getMaxCpus()) ? null : model.getMaxCpus().intValue())
                .maxDuration(Objects.isNull(model.getMaxDuration()) ? null : model.getMaxDuration().intValue())
                .maxRuns(Objects.isNull(model.getMaxRuns()) ? null : model.getMaxRuns().intValue())
                .tags(TagHelper.getNewDesiredTags(request))
                .build();
    }

    /**
     * Translates the response into a resource model
     *
     * @param response The create rungroup response
     * @return model The resource model
     */
    static ProgressEvent<ResourceModel, CallbackContext> fromCreateResponse(final CreateRunGroupResponse response) {
        return ProgressEvent.defaultSuccessHandler(ResourceModel.builder()
                .arn(response.arn())
                .id(response.id())
                .tags(response.tags())
                .build());
    }

    /**
     * Returns a get run group request
     *
     * @param model resource model
     * @return the GetRunGroupRequest
     */
    static GetRunGroupRequest toReadRequest(final ResourceModel model) {
        return GetRunGroupRequest.builder()
                .id(model.getId())
                .build();
    }

    /**
     * Translates the response into a resource model
     *
     * @param response The read resource response
     * @return model The resource model
     */
    static ProgressEvent<ResourceModel, CallbackContext> fromReadResponse(final GetRunGroupResponse response) {
        return ProgressEvent.defaultSuccessHandler(ResourceModel.builder()
                .arn(response.arn())
                .id(response.id())
                .tags(response.tags())
                .name(response.name())
                .maxRuns(Objects.isNull(response.maxRuns()) ? null : response.maxRuns().doubleValue())
                .maxDuration(Objects.isNull(response.maxDuration()) ? null : response.maxDuration().doubleValue())
                .maxCpus(Objects.isNull(response.maxCpus()) ? null : response.maxCpus().doubleValue())
                .creationTime(response.creationTime().toString())
                .build());
    }

    /**
     * Request to delete a resource
     *
     * @param model resource model
     * @return DeleteRunGroupRequest
     */
    static DeleteRunGroupRequest toDeleteRequest(final ResourceModel model) {
        return DeleteRunGroupRequest.builder()
                .id(model.getId())
                .build();
    }

    /**
     * Request to update properties of a previously created resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to modify a resource
     */
    static UpdateRunGroupRequest toUpdateRequest(final ResourceModel model) {
        return UpdateRunGroupRequest.builder()
                .name(model.getName())
                .id(model.getId())
                .maxCpus(Objects.isNull(model.getMaxCpus()) ? null : model.getMaxCpus().intValue())
                .maxDuration(Objects.isNull(model.getMaxDuration()) ? null : model.getMaxDuration().intValue())
                .maxRuns(Objects.isNull(model.getMaxRuns()) ? null : model.getMaxRuns().intValue())
                .build();
    }

    public static ProgressEvent<ResourceModel, CallbackContext> fromUpdateResponse(UpdateRunGroupResponse updateRunGroupResponse) {
        return ProgressEvent.defaultSuccessHandler(null);
    }

    /**
     * Request to list run group resources
     *
     * @param request resource model request
     * @return request for omics list run group request
     */
    static Function<ResourceModel, ListRunGroupsRequest> toListRequest(final ResourceHandlerRequest<ResourceModel> request) {
        return (final ResourceModel model) -> ListRunGroupsRequest.builder()
                .startingToken(request.getNextToken())
                .build();
    }

    /**
     * Translates resource objects from sdk into a resource model (primary identifier only)
     *
     * @param response the run group service list resource response
     * @return list of resource models
     */
    static ProgressEvent<ResourceModel, CallbackContext> fromListResponse(final ListRunGroupsResponse response) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(response.items().stream().map(runGroup -> ResourceModel.builder()
                        .arn(runGroup.arn())
                        .id(runGroup.id())
                        .creationTime(runGroup.creationTime().toString())
                        .name(runGroup.name())
                        .maxCpus(Objects.isNull(runGroup.maxCpus()) ? null : runGroup.maxCpus().doubleValue())
                        .maxRuns(Objects.isNull(runGroup.maxRuns()) ? null : runGroup.maxRuns().doubleValue())
                        .maxDuration(Objects.isNull(runGroup.maxDuration()) ? null : runGroup.maxDuration().doubleValue())
                        .build()).collect(Collectors.toList()))
                .nextToken(response.nextToken())
                .status(OperationStatus.SUCCESS)
                .build();
    }

    public static ProgressEvent<ResourceModel, CallbackContext> fromDeleteResponse(DeleteRunGroupResponse deleteRunGroupResponse) {
        return ProgressEvent.defaultSuccessHandler(null);
    }
}
