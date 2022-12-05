package software.amazon.omics.workflow;

import com.google.common.collect.Sets;
import software.amazon.awssdk.services.omics.OmicsClient;
import software.amazon.awssdk.services.omics.model.GetWorkflowResponse;
import software.amazon.awssdk.services.omics.model.TagResourceRequest;
import software.amazon.awssdk.services.omics.model.UntagResourceRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UpdateHandler extends BaseHandlerStd {
    private static final String CALL_GRAPH = "AWS-Omics-Workflow::Update";

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<OmicsClient> proxyClient,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> updateResource(proxyClient, progress, request))
                .then(progress -> fetchResource(proxyClient, progress, request, false))
                .then(progress -> updateTags(proxyClient, progress, request))
                .then(progress -> fetchResource(proxyClient, progress, request, true));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateResource(ProxyClient<OmicsClient> proxyClient,
                                                                        ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                        ResourceHandlerRequest<ResourceModel> request) {
        proxyClient.injectCredentialsAndInvokeV2(
                Translator.translateToUpdateRequest(request.getDesiredResourceState()),
                proxyClient.client()::updateWorkflow
        );

        return ProgressEvent.progress(
                progress.getResourceModel(),
                progress.getCallbackContext()
        );
    }

    private ProgressEvent<ResourceModel, CallbackContext> fetchResource(ProxyClient<OmicsClient> proxyClient,
                                                                        ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                        ResourceHandlerRequest<ResourceModel> request,
                                                                        boolean returnSuccess) {

        GetWorkflowResponse getWorkflowResponse
                = proxyClient.injectCredentialsAndInvokeV2(
                        Translator.translateToReadRequest(request.getDesiredResourceState()),
                        proxyClient.client()::getWorkflow
                );

        // The update API returns an empty payload. So fetch the existing resource from service and
        // pass it on to the next event. The ResourceModel would now have "arn", "tags", etc. fields populated
        // which are used in the tag update step.
        ResourceModel modelBasedOnExistingResource
                = Translator.fromReadResponse(getWorkflowResponse).getResourceModel();
        return returnSuccess ?
                ProgressEvent.success(
                        modelBasedOnExistingResource,
                        progress.getCallbackContext()
                ) : ProgressEvent.progress(
                    modelBasedOnExistingResource,
                    progress.getCallbackContext()
                );
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateTags(ProxyClient<OmicsClient> proxyClient,
                                                                     ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                     ResourceHandlerRequest<ResourceModel> request) {

        Map<String, String> existingTags = progress.getResourceModel().getTags();
        Set<String> existingTagKeys
                = existingTags != null && existingTags.keySet() != null ? existingTags.keySet() : Collections.emptySet();

        Map<String, String> updatedTags = TagHelper.getNewDesiredTags(request);
        Set<String> updatedTagKeys
                = updatedTags != null && updatedTags.keySet() != null ? updatedTags.keySet() : Collections.emptySet();

        // There can be cases where tag key hasn't changed but tag value changed.
        // Since there is no update tag API, we remove the tag and add it back with the same key but updated value
        Set<String> keysValueUpdated
                = existingTagKeys.stream()
                    // Key is in both updated and existing tags - but values don't match.
                    .filter(k -> updatedTagKeys.contains(k) && existingTagKeys.contains(k)
                                    && !existingTags.get(k).equals(updatedTags.get(k))
                            )
                    .collect(Collectors.toSet());

        Set<String> keysToRemove = combine(keysValueUpdated, Sets.difference(existingTagKeys, updatedTagKeys));
        Set<String> keysToAdd = combine(keysValueUpdated, Sets.difference(updatedTagKeys, existingTagKeys));

        if (!keysToRemove.isEmpty()) {
            unTagResource(progress.getResourceModel().getArn(), keysToRemove, proxyClient);
        }

        if (!keysToAdd.isEmpty()) {
            tagResource(progress.getResourceModel().getArn(), keysToAdd, updatedTags, proxyClient);
        }

        return ProgressEvent.progress(progress.getResourceModel(), progress.getCallbackContext());
    }

    private void unTagResource(String arn, Set<String> keys, ProxyClient<OmicsClient> proxyClient) {
        UntagResourceRequest req = UntagResourceRequest.builder()
                .resourceArn(arn)
                .tagKeys(keys)
                .build();
        proxyClient.injectCredentialsAndInvokeV2(req, proxyClient.client()::untagResource);
    }

    private void tagResource(String arn, Set<String> keys, Map<String, String> tags, ProxyClient<OmicsClient> proxyClient) {
        Map<String, String> tagsToAdd
                = tags.entrySet().stream()
                    .filter(e -> keys.contains(e.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        TagResourceRequest req = TagResourceRequest.builder()
                .resourceArn(arn)
                .tags(tagsToAdd)
                .build();
        proxyClient.injectCredentialsAndInvokeV2(req, proxyClient.client()::tagResource);
    }

    private Set<String> combine(Set<String> s1, Set<String> s2) {
        return Stream.concat(s1.stream(),s2.stream()).collect(Collectors.toSet());
    }
}
