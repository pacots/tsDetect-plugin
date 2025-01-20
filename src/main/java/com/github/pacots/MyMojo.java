package com.github.pacots;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 * 
 * @phase process-sources
 */
@Mojo(name="tsDetect", defaultPhase = LifecyclePhase.TEST)
public class MyMojo
    extends AbstractMojo
{
    /**
     * Location of the file.
     * @parameter expression="${project.build.directory}"
     * @required
     */
    @Parameter(property = "sourcePaths", required = true)
    private String[] sourcePaths;

    @Parameter(property = "testSourcePaths", required = true)
    private String[] testSourcePaths;

    @Parameter(property = "projectTitle", required = true)
    private String projectTitle;

    @Override
    public void execute() throws MojoExecutionException {

        //create csv file
        File csvFile = new File("./", "tests.csv");

        try (FileWriter fileWriter = new FileWriter(csvFile)) {
            for (int index = 0; index < testSourcePaths.length; index++) {
                String testPath = testSourcePaths[index];
                String mainPath = index < sourcePaths.length ? sourcePaths[index] : "";

                fileWriter.write(String.join(",", projectTitle, testPath, mainPath) + "\n");
            }

            getLog().info("CSV creado");
        } catch (IOException e) {
            throw new MojoExecutionException("Error al crear el CSV", e);
        }

        try {
            File workingDir = new File("./");

            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", "./TestSmellDetector.jar", "./tests.csv");
            processBuilder.directory(workingDir);
            processBuilder.inheritIO();

            Process process = processBuilder.start();
            int exitStatus = process.waitFor();

            if (exitStatus != 0) {
                throw new MojoExecutionException("Error en tsDetect " + exitStatus);
            } else {
                getLog().info("Ejecución terminada " + exitStatus);
            }

        } catch (IOException | InterruptedException ex) {
            throw new MojoExecutionException("Error en tsDetect", ex);
        }
    }
}
