package org.ops4j.pax.exam;

public interface ICucumberStepInvoker {

    <R> R invokeStep(String caption, Object... args) throws Exception;
}
