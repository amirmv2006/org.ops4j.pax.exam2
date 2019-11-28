package ir.amv.snippets.pax.exam.karaf;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;

import java.io.File;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

/**
 * @author Amir
 */
public class AmirTests {

    protected String[] getTestFeatures() {
        return new String[0];
    }

    @Configuration
    public Option[] config() {
        MavenArtifactUrlReference karafUrl = maven()
                .groupId(getKarafGroupId())
                .artifactId(getKarafArtifactId())
                .version(getKarafVersion())
                .type("zip");

        MavenUrlReference karafStandardRepo = maven()
                .groupId(getFeaturesGroupId())
                .artifactId(getFeaturesArtifactId())
                .version(getFeaturesVersion())
                .classifier("features")
                .type("xml");
        File unpackDirectory = new File("target", "exam");
        return new Option[]{
                // KarafDistributionOption.debugConfiguration("5005", true),
                karafDistributionConfiguration()
                        .frameworkUrl(karafUrl)
                        .unpackDirectory(unpackDirectory)
                        .useDeployFolder(false),
                keepRuntimeFolder(),
                configureConsole().ignoreLocalConsole(),
                mavenBundle()
                        .groupId("org.apache.geronimo.specs")
                        .artifactId("geronimo-jpa_2.1_spec")
                        .version("1.0-alpha-1")
                        .start(),
                mavenBundle()
                        .groupId("com.github.amirmv2006.testing.integration")
                        .artifactId("ir.amv.os.vaseline.testing.integration.bundle.checker")
                        .version("1.0-SNAPSHOT")
                        .start(),
                features(karafStandardRepo, getTestFeatures()),
                logLevel(LogLevelOption.LogLevel.INFO),
                debugConfiguration("5555", false)
        };
    }

    protected String getFeaturesVersion() {
        return System.getProperty("vasline.feature.under.test.version", "1.0-SNAPSHOT");
    }

    protected String getFeaturesArtifactId() {
        return System.getProperty("vasline.feature.under.test.artifactId", "vaseline-basics-karaf-feature");
    }

    protected String getFeaturesGroupId() {
        return System.getProperty("vasline.feature.under.test.groupId", "com.github.amirmv2006.basics.osgi");
    }

    protected String getKarafVersion() {
        return System.getProperty("org.apache.karaf.version", "4.2.3");
    }

    protected String getKarafArtifactId() {
        return System.getProperty("org.apache.karaf.artifactId", "apache-karaf");
    }

    protected String getKarafGroupId() {
        return System.getProperty("org.apache.karaf.groupId", "org.apache.karaf");
    }

}