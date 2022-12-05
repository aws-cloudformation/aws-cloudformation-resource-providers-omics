package software.amazon.omics.common;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.omics.model.ResourceNotFoundException;
import software.amazon.awssdk.services.omics.model.InternalServerException;
import software.amazon.awssdk.services.omics.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.omics.model.AccessDeniedException;
import software.amazon.awssdk.services.omics.model.ValidationException;
import software.amazon.awssdk.services.omics.model.ConflictException;
import software.amazon.awssdk.services.omics.model.RangeNotSatisfiableException;
import software.amazon.awssdk.services.omics.model.ThrottlingException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class CallWrapper {
    /**
     * Returns a function that wraps a given request to make a service call with proper error translation
     * @param requestFunction The API call to make
     * @param logger Logger
     * @return A function to process the given request
     * @param <RequestT>  AwsRequest being made
     * @param <ResultT> AwsResponse to be returned
     * @param <ClientT> SdkClient to make the request
     */
    public static <RequestT extends AwsRequest, ResultT extends AwsResponse, ClientT extends SdkClient> BiFunction<RequestT, ProxyClient<ClientT>, ResultT> wrap(
            final Function<RequestT, ResultT> requestFunction,
            final Logger logger) {
        return (final RequestT request, final ProxyClient<ClientT> proxyClient) -> {
            try {
                logger.log("Invoking with request: " + request.toString());
                return proxyClient.injectCredentialsAndInvokeV2(request, requestFunction);
            }  catch (ResourceNotFoundException e) {
                logger.log("ResourceNotFoundException: " + e.getMessage());
                throw new CfnNotFoundException(e);
            } catch (InternalServerException e) {
                logger.log("InternalServerException: " + e.getMessage());
                throw new CfnInternalFailureException(e);
            } catch (ServiceQuotaExceededException e) {
                logger.log("ServiceQuotaExceededException: " + e.getMessage());
                throw new CfnServiceLimitExceededException(e);
            } catch (ValidationException e) {
                logger.log("ValidationException: " + e.getMessage());
                throw new CfnInvalidRequestException(e);
            }  catch (ConflictException e) {
                logger.log("ConflictException: " + e.getMessage());
                throw new CfnInvalidRequestException(e);
            } catch (RangeNotSatisfiableException e) {
                logger.log("RangeNotSatisfiableException: " + e.getMessage());
                throw new CfnInvalidRequestException(e);
            } catch (ThrottlingException e) {
                logger.log("ThrottlingException: " + e.getMessage());
                throw new CfnThrottlingException(e);
            } catch (AccessDeniedException e) {
                logger.log("AccessDeniedException: " + e.getMessage());
                throw new CfnAccessDeniedException(e);
            } catch (AwsServiceException e) {
                logger.log("GenericException: " + e.getMessage());
                throw new CfnGeneralServiceException(e);
            }
        };
    }
}
