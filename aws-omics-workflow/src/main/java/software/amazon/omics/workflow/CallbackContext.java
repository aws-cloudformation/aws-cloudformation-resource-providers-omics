package software.amazon.omics.workflow;

import software.amazon.cloudformation.proxy.StdCallbackContext;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode(callSuper = true)
public class CallbackContext extends StdCallbackContext {
    private long attemptNumber;

    public CallbackContext() {
        this.attemptNumber = 0L;
    }

    public void incrementAttempt() {
        this.attemptNumber += 1;
    }

    public long attempts() {
        return this.attemptNumber;
    }

    CallbackContext(long attemptNumber) {
        this.attemptNumber = attemptNumber;
    }
}
