/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.maven.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;

/**
 * Packages the Grails plugin.
 *
 * @author <a href="mailto:aheritier@gmail.com">Arnaud HERITIER</a>
 * @version $Id$
 * @description Packages the Grails plugin.
 * @since 0.4
 */
@Mojo(name = "package-plugin", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GrailsPackagePluginMojo extends AbstractGrailsMojo {

    /**
     * The artifact that this project produces.
     *
     */
    @Parameter(defaultValue = "${project.artifact}", readonly = true)
    private Artifact artifact;

    /**
     * The artifact handler.
     */
    @Parameter(defaultValue = "${component.org.apache.maven.artifact.handler.ArtifactHandler#grails-plugin}", readonly = true)
    protected ArtifactHandler artifactHandler;

    /**
     * The artifact handler.
     */
    @Parameter(defaultValue = "${component.org.apache.maven.artifact.handler.ArtifactHandler#grails-binary-plugin}")
    protected ArtifactHandler binaryArtifactHandler;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // First package the plugin using the Grails script.
        runGrails("PackagePlugin");

        // Now move the ZIP from the project directory to the build
        // output directory.
        String zipFileName = project.getArtifactId() + "-" + project.getVersion() + ".zip";
        if (!zipFileName.startsWith(PLUGIN_PREFIX)) zipFileName = PLUGIN_PREFIX + zipFileName;

        File zipGeneratedByGrails = new File(getBasedir(), zipFileName);
        ArtifactHandler handler = artifactHandler;

        if(!zipGeneratedByGrails.exists()) {
            // try binary jar
            final String targetDir = this.project.getBuild().getDirectory();
            File jarFile = new File(targetDir, "grails-plugin-" + project.getArtifactId() + "-" + project.getVersion() + ".jar");
            if(jarFile.exists()) {

                zipGeneratedByGrails = jarFile;
                zipFileName = project.getArtifactId() + "-" + project.getVersion() + ".jar";
                handler = binaryArtifactHandler;
            }

        }

        File mavenZipFile = new File(project.getBuild().getDirectory(), zipFileName);
        mavenZipFile.delete();
        if (!zipGeneratedByGrails.renameTo(mavenZipFile)) {
            throw new MojoExecutionException("Unable to copy the plugin ZIP to the target directory");
        } else {
            getLog().info("Moved plugin ZIP to '" + mavenZipFile + "'.");
        }

        // Attach the zip file to the "grails-plugin" artifact, otherwise
        // the "install" and "deploy" phases won't work.
        artifact.setFile(mavenZipFile);
        artifact.setArtifactHandler(handler);
    }
}
