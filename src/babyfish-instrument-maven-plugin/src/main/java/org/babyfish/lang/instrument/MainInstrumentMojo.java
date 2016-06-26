/*
 * BabyFish, Object Model Framework for Java and JPA.
 * https://github.com/babyfish-ct/babyfish
 *
 * Copyright (c) 2008-2016, Tao Chen
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * Please visit "http://opensource.org/licenses/LGPL-3.0" to know more.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 */
package org.babyfish.lang.instrument;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * @author Tao Chen
 */
@Mojo(
        name = "instrument",
        defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        requiresProject = false,
        threadSafe = true,
        requiresDependencyResolution = ResolutionScope.RUNTIME
)
@Execute(
        goal = "instrument", 
        phase = LifecyclePhase.PROCESS_CLASSES
)
public class MainInstrumentMojo extends AbstractInstrumentMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.doExecute();
    }

    @Override
    protected File getInstrumentDirectory() {
        return this.classesDirectory;
    }

    @Override
    protected String getType() {
        return "main-classes";
    }
    
    @Override
    protected ClassLoader createClassLoader() {
        URL[] urls = generateClassPaths().stream().map(
                AbstractInstrumentMojo::fileNameToURL
        ).toArray(len -> new URL[len]);
        return new URLClassLoader(urls);
    }

    private List<String> generateClassPaths() {
        Set<Artifact> artifacts = this.project.getArtifacts();
        List<String> classpaths = new ArrayList<>(2 + artifacts.size());
        classpaths.add(this.classesDirectory.getAbsolutePath());
        for (Artifact artifact : artifacts) {
            if (artifact.getArtifactHandler().isAddedToClasspath() && 
                    !"test".equalsIgnoreCase(artifact.getScope())) {
                File file = artifact.getFile();
                if (file != null) {
                    classpaths.add(file.getPath());
                }
            }
        }
        return classpaths;
    }
}
