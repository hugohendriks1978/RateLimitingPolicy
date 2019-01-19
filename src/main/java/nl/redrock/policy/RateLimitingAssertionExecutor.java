package nl.redrock.policy;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import oracle.wsm.common.sdk.IContext;
import oracle.wsm.common.sdk.IMessageContext;
import oracle.wsm.common.sdk.IResult;
import oracle.wsm.common.sdk.Result;
import oracle.wsm.common.sdk.WSMException;
import oracle.wsm.policy.model.IAssertion;
import oracle.wsm.policyengine.IExecutionContext;

/**
 * A Rate limiting policy which can be configured for the amount of calls per unique ip.
 * @author Hugo Hendriks
 */
public class RateLimitingAssertionExecutor extends BaseAssertionExecutor {

    private int limit;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Map<String, RateLimitSetting> rateLimitSettings = new HashMap();

    public RateLimitingAssertionExecutor() {
        super("ServiceExecutionReporter");
    }

    @Override
    public void init(IAssertion iAssertion, IExecutionContext iExecutionContext, IContext iContext) {
        super.init(iAssertion, iExecutionContext, iContext);
        this.limit = Integer.parseInt(super.getPolicyBindingProperty("limit"));
    }

    @Override
    public IResult execute(IContext iContext) throws WSMException {
        IResult result = new Result();

        try {
 
            //als we in het request stage zitten dan zetten we de start tijd
            IMessageContext.STAGE stage = ((IMessageContext) iContext).getStage();
            if (stage == IMessageContext.STAGE.request) {
                
                Map<String, Object> map = ((IMessageContext) iContext).getAllProperties();
            
                //get the ip
                String ip = (String) map.get("com.bea.contextelement.alsb.router.inbound.request.metadata.http.client-address");

                //get the settings belonging to this ip
                RateLimitSetting setting = rateLimitSettings.get(ip);
                //if no settings found....must be the first call....creat them
                if(setting == null){
                    setting = new RateLimitSetting();
                    System.out.println("Calling ip = " + ip);
                    rateLimitSettings.put(ip, setting);
                }
                
                //eerste keer dat de service wordt aangeroepen....zet de tijd en hoog het nummer op.
                if (setting.getCallMoment() == null) {
                    setting.setCallMoment(Calendar.getInstance().getTime());
                    setting.setAmountOfActuallCalls(setting.getAmountOfActuallCalls() + 1);
                    System.out.println(DEBUG_START);
                    System.out.println("First time! " + format.format(setting.getCallMoment()));
                    System.out.println(DEBUG_END);
                } //de service is al een keer aangeroepen. 
                else {
                    Date now = Calendar.getInstance().getTime();
                    //als de tijd tussen de laatste call en nu groter is dan 1 minuut, dan reset de waardes
                    if (now.getTime() - setting.getCallMoment().getTime() > 60000) {
                        setting.setAmountOfActuallCalls(0);
                        setting.setCallMoment(now);
                        System.out.println(DEBUG_START);
                        System.out.println("Reset!" + format.format(now));
                        System.out.println(DEBUG_END);
                        setting.setAmountOfActuallCalls(setting.getAmountOfActuallCalls() + 1);
                        result.setStatus(IResult.SUCCEEDED);
                    } //als deze binnen de minuut valt, tel dan 1 op bij het aantal
                    else {
                        //als we ook nog binnen de limiet zitten, dan mag het nog
                        if (setting.getAmountOfActuallCalls() < limit) {
                            setting.setAmountOfActuallCalls(setting.getAmountOfActuallCalls() + 1);
                            System.out.println(DEBUG_START);
                            System.out.println(setting.getAmountOfActuallCalls());
                            System.out.println(DEBUG_END);
                            result.setStatus(IResult.SUCCEEDED);
                        } //het aantal calls per minuut is bereikt
                        else {
                            System.out.println(DEBUG_START);
                            System.out.println("Limit reached");
                            System.out.println(DEBUG_END);
                            result.setStatus(IResult.FAILED);
                            throw new WSMException("Rate limit reached in 1 minute");
                        }
                    }
                }
            }
        } catch (Exception e) {
            WSMException wsmException = new WSMException(e);
            generateFault(wsmException);
        }
        return result;
    }
}
