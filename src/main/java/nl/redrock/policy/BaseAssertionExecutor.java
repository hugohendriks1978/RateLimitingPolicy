package nl.redrock.policy;

import java.util.HashMap;
import java.util.logging.Logger;

import oracle.wsm.common.sdk.IContext;
import oracle.wsm.policy.model.IAssertion;
import oracle.wsm.policy.model.IAssertionBindings;
import oracle.wsm.policy.model.IConfig;
import oracle.wsm.policy.model.IPropertySet;
import oracle.wsm.policy.model.impl.SimpleAssertion;
import oracle.wsm.policyengine.IExecutionContext;
import oracle.wsm.policyengine.impl.AssertionExecutor;

/**
 * A base class for OWSM custom assertions that specific classes can extend. It
 * contains common code and variables used across all custom assertion executors.
 *
 * All custom assertions must extend the BaseAssertionExecutor class.
 */
public abstract class BaseAssertionExecutor extends AssertionExecutor {
    
    private static final Logger LOGGER = Logger.getLogger(RateLimitingAssertionExecutor.class.getName());
    protected final static String PROP_DEBUG = "debugFlag";
    protected final static String DEBUG_START = "===========================================================>>>";
    protected final static String DEBUG_END = "<<<===========================================================";
    protected IAssertion mAssertion = null;
    protected IExecutionContext mEcontext = null;
    protected IContext mIcontext = null;
    protected String tag;

    /**
     * Constructor
     */
    public BaseAssertionExecutor(String aTag) {
        super();
        this.tag = aTag;
    }

    /**
     * Implemented from parent class
     */
    public void init(IAssertion iAssertion, IExecutionContext iExecutionContext, IContext iContext) {
        mAssertion = iAssertion;
        mEcontext = iExecutionContext;
        mIcontext = iContext;
    }

    /**
     * Implemented from parent class
     */
    public void destroy() {
        // Nothing to do.
    }

    /**
     * retrieve a parameters of the policy
     *
     * @param propertyName the name of the property
     * @return the value
     */
    protected String getPolicyBindingProperty(String propertyName) {
        String propertyValue = null;
        try {
            IAssertionBindings bindings = ((SimpleAssertion) (this.mAssertion)).getBindings();
            IConfig config = bindings.getConfigs().get(0);
            IPropertySet propertyset = config.getPropertySets().get(0);
            propertyValue = propertyset.getPropertyByName(propertyName).getValue();
        } catch (Exception e) {
            LOGGER.severe("Exception in getPolicyBindingProperty" + e.getMessage());
        }
        return propertyValue;
    }

}
