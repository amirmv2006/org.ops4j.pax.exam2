package org.ops4j.pax.exam.spi.intern;

import org.ops4j.pax.exam.IStepProbeProvider;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.store.Handle;
import org.ops4j.store.Store;

import java.io.InputStream;
import java.util.Set;

public class StepProbeProviderImpl
        extends DefaultTestProbeProvider
        implements IStepProbeProvider {

    public StepProbeProviderImpl(Set<TestAddress> tests, Store<InputStream> store, Handle probe) {
        super(tests, store, probe);
    }
}
