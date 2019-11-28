package org.ops4j.pax.exam.spi.intern;

import org.ops4j.pax.exam.*;
import org.ops4j.pax.exam.spi.ContentCollector;
import org.ops4j.pax.tinybundles.core.TinyBundle;
import org.ops4j.store.Store;
import org.osgi.framework.Constants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.ops4j.pax.exam.Constants.STEP_EXECUTABLE;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;
import static org.ops4j.pax.tinybundles.core.TinyBundles.withClassicBuilder;

public class StepProbeBuilderImpl
        implements IStepProbeBuilder {

    private final List<Class<?>> stepClasses;
    private final Set<Class<?>> extraClasses;
    private final Map<TestAddress, TestInstantiationInstruction> addresses;
    private final Properties extraProperties;
    private final Set<String> ignorePackages = new HashSet<String>();
    private final Store<InputStream> store;
    private File tempDir;

    public StepProbeBuilderImpl(File tempDir, Store<InputStream> store) {
        this.tempDir = tempDir;
        this.store = store;
        addresses = new HashMap<>();
        stepClasses = new ArrayList<>();
        extraProperties = new Properties();
        extraClasses = new HashSet<>();
    }

    @Override
    public TestAddress addStepDef(Class<?> stepDefClass, String methodName, Object... args) {
        TestAddress address = new DefaultTestAddress(stepDefClass.getName() + "." + methodName, args);
        stepClasses.add(stepDefClass);
        addresses.put(address, new TestInstantiationInstruction(stepDefClass.getName() + ";" + methodName));
        return address;
    }

    @Override
    public void addExtraClasses(Class<?>... classes) {
        if (classes != null) {
            extraClasses.addAll(Arrays.asList(classes));
        }
    }

    @Override
    public IStepProbeBuilder addHeader(String key, String value) {
        extraProperties.put(key, value);
        return this;
    }

    @Override
    public IStepProbeProvider build() {
        if (stepClasses.size() == 0) {
            throw new TestContainerException("No tests added to setup!");
        }

        constructProbeTag(extraProperties);
        try {
            TinyBundle bundle = prepareProbeBundle(createExtraIgnores());
            return new StepProbeProviderImpl(getTests(), store, store.store(bundle
                    .build(withClassicBuilder())));

        }
        catch (IOException e) {
            throw new TestContainerException(e);
        }
    }

    private TinyBundle prepareProbeBundle(Properties p) throws IOException {
        TinyBundle bundle = bundle(store).set(Constants.DYNAMICIMPORT_PACKAGE, "*");

        bundle.set(Constants.BUNDLE_SYMBOLICNAME, "");
        bundle.set(Constants.BUNDLE_MANIFESTVERSION, "2");
        for (Object key : extraProperties.keySet()) {
            bundle.set((String) key, (String) extraProperties.get(key));
        }
        for (Object key : p.keySet()) {
            bundle.set((String) key, (String) p.get(key));
        }

        Map<String, URL> map = collectResources();
        for (String item : map.keySet()) {
            bundle.add(item, map.get(item));
        }
        return bundle;
    }

    private Map<String, URL> collectResources() throws IOException {
        ContentCollector collector = selectCollector();
        Map<String, URL> map = new HashMap<String, URL>();
        collector.collect(map);
        return map;
    }

    static String convertClassToPath(Class<?> c) {
        return c.getName().replace(".", File.separator) + ".class";
    }

    /**
     * @param clazz
     *            to find the root classes folder for.
     *
     * @return A File instance being the exact folder on disk or null, if it hasn't been found.
     *
     * @throws java.io.IOException
     *             if a problem occurs (method crawls folders on disk..)
     */
    public static File findClassesFolder(Class<?> clazz) throws IOException {
        ClassLoader classLoader = clazz.getClassLoader();
        String clazzPath = convertClassToPath(clazz);
        URL url = classLoader.getResource(clazzPath);
        if (url == null || !"file".equals(url.getProtocol())) {
            return null;
        }
        else {
            try {
                File file = new File(url.toURI());
                String fullPath = file.getCanonicalPath();
                String parentDirPath = fullPath
                        .substring(0, fullPath.length() - clazzPath.length());
                return new File(parentDirPath);
            }
            catch (URISyntaxException e) {
                // this should not happen as the uri was obtained from getResource
                throw new TestContainerException(e);
            }
        }
    }

    private ContentCollector selectCollector() throws IOException {
        File root = findClassesFolder(stepClasses.get(0));
        List<Class<?>> allClasses = getAllRemoteClasses();
        if (root != null) {
            return new CompositeCollector(new CollectFromBase(root), new CollectFromItems(allClasses));
        }
        else {
            return new CollectFromItems(allClasses);
        }
    }

    private void constructProbeTag(Properties p) {
        StringBuilder sbKeyChain = new StringBuilder();

        for (TestAddress address : addresses.keySet()) {
            sbKeyChain.append(address.identifier());
            sbKeyChain.append(",");
            p.put(address.identifier(), addresses.get(address).toString());
        }
        p.put(STEP_EXECUTABLE, sbKeyChain.toString());
    }

    private Properties createExtraIgnores() {
        Properties properties = new Properties();
        StringBuilder sb = new StringBuilder();
        for (String p : ignorePackages) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(p);
        }
        properties.put("Ignore-Package", sb.toString());
        return properties;
    }

    public Set<TestAddress> getTests() {
        return addresses.keySet();
    }

    @Override
    public File getTempDir() {
        return tempDir;
    }

    @Override
    public void setTempDir(File tempDir) {
        this.tempDir = tempDir;
    }

    @Override
    public List<Class<?>> getAllRemoteClasses() {
        List<Class<?>> allClasses = new ArrayList<>(stepClasses);
        allClasses.addAll(extraClasses);
        return allClasses;
    }
}
