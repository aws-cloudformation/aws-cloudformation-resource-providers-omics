package software.amazon.omics.common;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.client.config.SdkAdvancedClientOption;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.omics.OmicsClient;
import software.amazon.cloudformation.LambdaWrapper;

import java.net.URI;

public final class ClientBuilder {

    private static final String OMICS_CLIENT_URI = System.getenv("OmicsClientURI");

    public static OmicsClient getClient() {
        return OMICS_CLIENT_URI != null ?
                getDevClient() :
                OmicsClient.builder()
                    .httpClient(LambdaWrapper.HTTP_CLIENT)
                    .build();
    }

    private static OmicsClient getDevClient() {
        return OmicsClient.builder()
                .region(Region.US_WEST_2)
                .endpointOverride(URI.create(OMICS_CLIENT_URI))
                .overrideConfiguration(
                        ClientOverrideConfiguration.builder()
                                .putAdvancedOption(SdkAdvancedClientOption.USER_AGENT_SUFFIX, "dev-generated")
                                .putAdvancedOption(SdkAdvancedClientOption.DISABLE_HOST_PREFIX_INJECTION, true)
                                .build()
                )
                .httpClient(LambdaWrapper.HTTP_CLIENT)
                .build();
    }
}
