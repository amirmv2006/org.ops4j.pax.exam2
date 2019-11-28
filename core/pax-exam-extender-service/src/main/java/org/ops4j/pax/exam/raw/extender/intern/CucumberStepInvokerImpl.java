package org.ops4j.pax.exam.raw.extender.intern;

import org.ops4j.pax.exam.ICucumberStepInvoker;
import org.ops4j.pax.exam.util.Injector;
import org.ops4j.pax.swissbox.tracker.ServiceLookup;
import org.osgi.framework.BundleContext;

import java.lang.reflect.Method;
import java.util.Hashtable;

import static org.ops4j.pax.exam.Constants.EXAM_SERVICE_TIMEOUT_DEFAULT;
import static org.ops4j.pax.exam.Constants.EXAM_SERVICE_TIMEOUT_KEY;

public class CucumberStepInvokerImpl
        implements ICucumberStepInvoker {
    private BundleContext ctx;
    private Injector injector;
    private final Hashtable<Class<?>, Object> cachedInstances;

    public CucumberStepInvokerImpl(String expr, BundleContext ctx) {
        this.ctx = ctx;
        injector = ServiceLookup.getService(ctx, Injector.class);
        cachedInstances = new Hashtable<>();
    }

    @Override
    public <R> R invokeStep(String caption, Object[] args) throws Exception {
        System.setProperty(EXAM_SERVICE_TIMEOUT_KEY,
                "10000");
        String classAndMethodName = caption;
        int lastIndexOf = classAndMethodName.lastIndexOf(".");
        Class stepClass = ctx.getBundle().loadClass(classAndMethodName.substring(0, lastIndexOf));
        Method[] declaredMethods = stepClass.getDeclaredMethods();
        // what about method overloading?
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.getName().equals(classAndMethodName.substring(lastIndexOf + 1))) {
                // keep instances of steps
                Object newInstance = cachedInstances.computeIfAbsent(stepClass, clazz -> {
                    try {
                        Object o = clazz.newInstance();
                        injector.injectFields(o);
                        return o;
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException("can not instantiate " + clazz, e);
                    }
                });
                return (R) declaredMethod.invoke(newInstance, args);
            }
        }
        throw new RuntimeException("Can not find address " + caption);
    }
}
