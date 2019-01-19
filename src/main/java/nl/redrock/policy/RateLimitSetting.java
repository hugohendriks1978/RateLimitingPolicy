package nl.redrock.policy;

import java.util.Date;

/**
 *
 * @author Hugo
 */
public class RateLimitSetting {
    
    private long amountOfActuallCalls = 0;
    private Date callMoment;

    /**
     * @return the amountOfActuallCalls
     */
    public long getAmountOfActuallCalls() {
        return amountOfActuallCalls;
    }

    /**
     * @param amountOfActuallCalls the amountOfActuallCalls to set
     */
    public void setAmountOfActuallCalls(long amountOfActuallCalls) {
        this.amountOfActuallCalls = amountOfActuallCalls;
    }

    /**
     * @return the callMoment
     */
    public Date getCallMoment() {
        return callMoment;
    }

    /**
     * @param callMoment the callMoment to set
     */
    public void setCallMoment(Date callMoment) {
        this.callMoment = callMoment;
    }
}
