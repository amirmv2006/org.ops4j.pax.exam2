package org.ops4j.pax.exam;

import java.io.File;
import java.util.List;

public interface IStepProbeBuilder {

    TestAddress addStepDef(Class<?> stepDefClass, String methodName, Object... args);

    void addExtraClasses(Class<?>... classes);

    IStepProbeBuilder addHeader(String key, String value);

    IStepProbeProvider build();

    File getTempDir();

    void setTempDir(File tempDir);

    List<Class<?>> getAllRemoteClasses();
}