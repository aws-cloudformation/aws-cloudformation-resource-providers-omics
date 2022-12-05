package software.amazon.omics.rungroup;

import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import java.util.HashMap;
import java.util.Map;

public class TagHelper {

    /**
     * getNewDesiredTags
     *
     * If stack tags and resource tags are not merged together in Configuration class,
     * we will get new desired system (with `aws:cloudformation` prefix) and user defined tags from
     * handlerRequest.getSystemTags() (system tags),
     * handlerRequest.getDesiredResourceTags() (stack tags),
     * handlerRequest.getDesiredResourceState().getTags() (resource tags).
     *
     * System tags are an optional feature. Merge them to your tags if you have enabled them for your resource.
     * System tags can change on resource update if the resource is imported to the stack.
     */
    public static Map<String, String> getNewDesiredTags(final ResourceHandlerRequest<ResourceModel> handlerRequest) {
        final Map<String, String> desiredTags = new HashMap<>();

        // merge system tags with desired resource tags if your service supports CloudFormation system tags
         if (handlerRequest.getSystemTags() != null) {
             // Due to bug can't support system tags for now.

             // desiredTags.putAll(handlerRequest.getSystemTags());
         }

        // get desired stack level tags from handlerRequest
        if (handlerRequest.getDesiredResourceTags() != null) {
            desiredTags.putAll(handlerRequest.getDesiredResourceTags());
        }

        // get resource level tags from resource model based on tag property name
        if (handlerRequest.getDesiredResourceState() != null && handlerRequest.getDesiredResourceState().getTags() != null) {
            desiredTags.putAll(handlerRequest.getDesiredResourceState().getTags());
        }

        return desiredTags;
    }
}
