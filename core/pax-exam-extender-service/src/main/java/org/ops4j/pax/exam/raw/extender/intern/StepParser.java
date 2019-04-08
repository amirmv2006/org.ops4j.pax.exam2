/*
 * Copyright 2009 Toni Menzel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.raw.extender.intern;

import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.ICucumberStepInvoker;
import org.ops4j.pax.swissbox.extender.ManifestEntry;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Toni Menzel
 * @since Jan 10, 2010
 */
public class StepParser {

    private static final Logger LOG = LoggerFactory.getLogger(StepParser.class);

    private final Step[] steps;

    public StepParser(BundleContext ctx, String sigs, List<ManifestEntry> manifestEntries) {
        List<String> signatures = new ArrayList<String>();
        List<Step> stepList = new ArrayList<>();
        signatures.addAll(Arrays.asList(sigs.split(",")));

        for (ManifestEntry manifestEntry : manifestEntries) {
            LOG.debug("Test " + manifestEntry.getKey() + " to be in " + sigs);
            if (signatures.contains(manifestEntry.getKey())) {
                stepList.add(make(ctx, manifestEntry.getKey(), manifestEntry.getValue()));
            }
        }

        this.steps = stepList.toArray(new Step[stepList.size()]);
    }

    private Step make(BundleContext ctx, String sig, String expr) {
        // should be a service really
        // turn this expression into a service detail later
        LOG.debug("Registering Service: " + ICucumberStepInvoker.class.getName()
            + " with " + Constants.STEP_SIGNATURE_KEY + "=\"" + sig + "\" and expression=\"" + expr + "\"");
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put(Constants.STEP_SIGNATURE_KEY, sig);
        return new Step(ICucumberStepInvoker.class.getName(), createInvoker(ctx, expr), props);
    }

    private ICucumberStepInvoker createInvoker(BundleContext ctx, String expr) {
        String invokerType = System.getProperty("pax.exam.invoker");
//        if (invokerType == null) {
            return new CucumberStepInvokerImpl(expr, ctx);
//        }
//        else {
//            Map<String, String> props = new HashMap<String, String>();
//            props.put("driver", invokerType);
//            ProbeInvokerFactory factory = ServiceLookup.getService(ctx, ProbeInvokerFactory.class,
//                props);
//            return factory.createProbeInvoker(ctx, expr);
//            return null;
//        }
    }

    public Step[] getSteps() {
        return steps;
    }
}
