package org.ops4j.pax.exam;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public interface IStepProbeProvider {

    Set<TestAddress> getTests();

    InputStream getStream() throws IOException;
}
