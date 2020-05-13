package online.prostobank.clients.utils.aspects.aj;


import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.annotation.Nonnull;

@Getter
@EqualsAndHashCode
public class CallCounter {
    private String  callSignature;

    private Long    averageExecutionTimeMS = 0L;
    private Integer success                = 0;
    private Integer failure                = 0;
    private Long    totalExecutionTimeMS   = 0L;

    public CallCounter(@Nonnull String callSignature) {
        this.callSignature = callSignature;
    }

    public void success() {
        success++;
    }

    public void failure() {
        failure++;
    }

    public void appendExecutionTime(long time) {
        this.totalExecutionTimeMS += time;
    }

    public void setCallSignature(String callSignature) {
        this.callSignature = callSignature;
    }

    public void setAverageExecutionTimeMS(Long averageExecutionTimeMS) {
        this.averageExecutionTimeMS = averageExecutionTimeMS;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public void setFailure(Integer failure) {
        this.failure = failure;
    }
}
