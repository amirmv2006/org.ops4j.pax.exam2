package ir.amv.snippets.pax.exam.karaf;

import org.ops4j.pax.exam.*;
import org.ops4j.pax.exam.container.remote.RBCRemoteTarget;
import org.ops4j.pax.exam.karaf.container.internal.KarafTestContainer;
import org.ops4j.pax.exam.karaf.container.internal.KarafTestContainerFactory;
import org.ops4j.pax.exam.options.extra.WorkingDirectoryOption;
import org.ops4j.pax.exam.rbc.client.RemoteBundleContextClient;
import org.ops4j.pax.exam.spi.DefaultExamSystem;
import org.ops4j.pax.exam.spi.intern.StepProbeBuilderImpl;
import org.ops4j.store.intern.TemporaryStore;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.ops4j.pax.exam.spi.DefaultExamSystem.createTempDir;

public class MyTestKarafRunner {

    private TestAddress address;

    public static void main(String[] args) throws Exception {
        new MyTestKarafRunner().runKaraf();
    }

    public void runKaraf() throws Exception {
        KarafTestContainerFactory containerFactory = new KarafTestContainerFactory();
        Option[] config = new AmirTests().config();
        ExamSystem examSystem = DefaultExamSystem.create(config);
        TestContainer[] testContainers = containerFactory.create(examSystem);
        final ClassInOsgi testClassInstance = new ClassInOsgi();

        WorkingDirectoryOption work = new WorkingDirectoryOption(createTemp(null).getAbsolutePath());
        File workingDirectory = createTemp(new File(work.getWorkingDirectory()));
        TemporaryStore store = new TemporaryStore(workingDirectory, false);
        IStepProbeBuilder probe = new StepProbeBuilderImpl(workingDirectory, store);

        // org.ops4j.pax.exam.rbc [58]
//        probe.addExtraClasses(IStepInvokerService.class);
//        probe.addExtraClasses(StepInvokerServiceImpl.class);
//        probe.addExtraClasses(StepInvokerActivator.class);
//        probe.addHeader("Bundle-Activator", StepInvokerActivator.class.getName());

        address = probe.addStepDef(ClassInOsgi.class, "print");
        for (TestContainer testContainer : testContainers) {
            testContainer.start();
            testContainer.installProbe(probe.build().getStream());
            if (testContainer instanceof KarafTestContainer) {
                KarafTestContainer karafTestContainer = (KarafTestContainer) testContainer;
                Field target = KarafTestContainer.class.getDeclaredField("target");
                target.setAccessible(true);
                RBCRemoteTarget o = (RBCRemoteTarget) target.get(karafTestContainer);
                RemoteBundleContextClient clientRBC = o.getClientRBC();
                Object result = clientRBC.callStep(address);
                System.out.println("result = " + result);
            }
        }
    }

    private synchronized File createTemp(File workingDirectory) throws IOException {
        if (workingDirectory == null) {
            return createTempDir();
        } else {
            workingDirectory.mkdirs();
            return workingDirectory;
        }
    }

}
