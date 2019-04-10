package org.ops4j.pax.exam.raw.extender.intern;

import org.ops4j.pax.exam.ICucumberStepInvoker;
import org.ops4j.pax.exam.util.Injector;
import org.ops4j.pax.swissbox.tracker.ServiceLookup;
import org.osgi.framework.BundleContext;

import java.lang.reflect.Method;

public class CucumberStepInvokerImpl
        implements ICucumberStepInvoker {
    private BundleContext ctx;
    private Injector injector;

    public CucumberStepInvokerImpl(String expr, BundleContext ctx) {
        this.ctx = ctx;
        injector = ServiceLookup.getService(ctx, Injector.class);
    }

    @Override
    public <R> R invokeStep(String caption, Object[] args) throws Exception {
        String classAndMethodName = caption;
        int lastIndexOf = classAndMethodName.lastIndexOf(".");
        Class stepClass = ctx.getBundle().loadClass(classAndMethodName.substring(0, lastIndexOf));
        Method[] declaredMethods = stepClass.getDeclaredMethods();
        // what about method overloading?
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.getName().equals(classAndMethodName.substring(lastIndexOf + 1))) {
                // keep instances of steps
                Object newInstance = stepClass.newInstance();
                injector.injectFields(newInstance);
                return (R) declaredMethod.invoke(newInstance, args);
            }
        }
        throw new RuntimeException("Can not find address " + caption);
    }
}
