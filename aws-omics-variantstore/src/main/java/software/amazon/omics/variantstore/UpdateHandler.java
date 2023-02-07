package software.amazon.omics.variantstore;

import software.amazon.awssdk.services.omics.OmicsClient;
import software.amazon.awssdk.services.omics.model.GetVariantStoreResponse;
import software.amazon.awssdk.services.omics.model.TagResourceRequest;
import software.amazon.awssdk.services.omics.model.UntagResourceRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import java.util.Map;
import java.util.Set;

public class UpdateHandler extends BaseHandlerStd {

    private static final String CALL_GRAPH = "AWS-Omics-VariantStore::Update";
    @Override
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
                Translator.toUpdateRequest(request.getDesiredResourceState()),
                proxyClient.client()::updateVariantStore
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

        GetVariantStoreResponse getVariantStoreResponse
                = proxyClient.injectCredentialsAndInvokeV2(
                Translator.toReadRequest(request.getDesiredResourceState()),
                proxyClient.client()::getVariantStore
        );

        // The update API returns an empty payload. So fetch the existing resource from service and
        // pass it on to the next event. The ResourceModel would now have "arn", "tags", etc. fields populated
        // which are used in the tag update step.
        ResourceModel modelBasedOnExistingResource
                = Translator.fromReadResponse(getVariantStoreResponse).getResourceModel();
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

        Map<String, String> previousTags = TagHelper.getPreviouslyAttachedTags(request);

        Map<String, String> desiredTags = TagHelper.getNewDesiredTags(request);

        if(TagHelper.shouldUpdateTags(request)) {
            Map<String, String> tagsToAdd = TagHelper.generateTagsToAdd(previousTags, desiredTags);
            Set<String> tagKeysToRemove = TagHelper.generateTagsToRemove(previousTags, desiredTags);

            String resourceArn = progress.getResourceModel().getStoreArn();

            if(!tagKeysToRemove.isEmpty()) {
                untagResource(resourceArn, tagKeysToRemove, proxyClient);
            }

            if(!tagsToAdd.isEmpty()) {
                tagResource(resourceArn, tagsToAdd, proxyClient);
            }
        }

        return ProgressEvent.progress(progress.getResourceModel(), progress.getCallbackContext());
    }

    private void untagResource(String arn, Set<String> keys, ProxyClient<OmicsClient> proxyClient) {
        UntagResourceRequest req = UntagResourceRequest.builder()
                .resourceArn(arn)
                .tagKeys(keys)
                .build();
        proxyClient.injectCredentialsAndInvokeV2(req, proxyClient.client()::untagResource);
    }

    private void tagResource(String arn, Map<String, String> tagsToAdd, ProxyClient<OmicsClient> proxyClient) {
        TagResourceRequest req = TagResourceRequest.builder()
                .resourceArn(arn)
                .tags(tagsToAdd)
                .build();
        proxyClient.injectCredentialsAndInvokeV2(req, proxyClient.client()::tagResource);
    }
}
