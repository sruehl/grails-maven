/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.grails.maven.plugin.tools.GrailsPluginProject;

/**
 * Validate consistency between Grails and Maven settings.
 *
 * @author <a href="mailto:aheritier@gmail.com">Arnaud HERITIER</a>
 * @version $Id$
 * @description Validate consistency between Grails (*GrailsPlugin.groovy) and Maven (pom.xml) settings.
 * @since 1.0
 */
@Mojo(name = "validate-plugin", requiresDependencyResolution = ResolutionScope.RUNTIME, defaultPhase = LifecyclePhase.VALIDATE)
public class MvnPluginValidateMojo extends AbstractGrailsMojo {

    /**
     * The artifact id of the project.
     */
    @Parameter(name = "project.artifactId", required = true, readonly = true)
    private String artifactId;

    /**
     * The version id of the project.
     */
    @Parameter(name = "project.version", required = true, readonly = true)
    private String version;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // Make sure that the artifactId starts with "grails-".
        //
        // As of Grails 1.3, plugins can be deployed to Maven repositories
        // without a 'grails-' prefix, so it doesn't make sense to keep this
        // limitation in this Maven plugin.
        /*
        if (!artifactId.startsWith(PLUGIN_PREFIX)) {
            throw new MojoFailureException("Grails plugin artifact IDs must start with '" + PLUGIN_PREFIX +
                "' to avoid confusion when the artifact is installed in the Maven repository.");
        }
        */

        try {
            getGrailsServices().readProjectDescriptor();
        } catch (final MojoExecutionException e) {
            getLog().info("No Grails project found - skipping validation.");
            return;
        }

        final GrailsPluginProject grailsProject = getGrailsServices().readGrailsPluginProject();
        final String pluginName = grailsProject.getPluginName();

        /*
        if (artifactId.equals(pluginName)) {
            throw new MojoFailureException("The artifact id in pom.xml has to be the same as in " +
                grailsProject.getFileName() + " prefixed with '" + PLUGIN_PREFIX + "'. This is to avoid confusion when " +
                "the artifact is installed in the Maven repository.");
        }
        */

        if (!artifactId.equals(pluginName) && !artifactId.equals(PLUGIN_PREFIX + pluginName)) {
            throw new MojoFailureException("The plugin name in the pom.xml [" + artifactId + "]" +
                " is not the expected '" + pluginName + "' or '" + PLUGIN_PREFIX + pluginName + "'. " +
                "Please correct the pom.xml or the plugin " +
                "descriptor.");
        }

        final String pomVersion = version.trim();
        final String grailsVersion = grailsProject.getVersion();

        if (!grailsVersion.equals(pomVersion)) {
            throw new MojoFailureException("The version specified in the plugin descriptor " +
                "[" + grailsVersion + "] is different from the version in the pom.xml [" + pomVersion + "] ");
        }
    }
}
