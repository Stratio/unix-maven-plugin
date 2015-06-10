package com.stratio.mojo.unix.maven.rpm;

/*
 * The MIT License
 *
 * Copyright 2009 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import fj.*;

import static fj.P.*;

import fj.data.*;
import com.stratio.mojo.unix.*;

import static com.stratio.mojo.unix.UnixFsObject.*;

import com.stratio.mojo.unix.core.*;
import com.stratio.mojo.unix.io.fs.*;
import com.stratio.mojo.unix.rpm.*;

import static com.stratio.mojo.unix.util.RelativePath.*;

import com.stratio.mojo.unix.util.*;
import com.stratio.mojo.unix.util.line.*;

import static org.codehaus.plexus.util.FileUtils.forceMkdir;

import org.joda.time.*;

import java.io.*;
import java.util.Arrays;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class RpmUnixPackage
        extends UnixPackage<RpmUnixPackage, RpmUnixPackage.RpmPreparedPackage> {
    private SpecFile specFile;

    private FsFileCollector fileCollector;

    private Option<String> rpmbuild;

    private boolean debug;

    private final static ScriptUtil scriptUtil = new ScriptUtil("pre", "post", "preun", "postun");

    public RpmUnixPackage() {
        super("rpm");
    }

    public RpmUnixPackage parameters(PackageParameters parameters) {
        if (parameters.license.isNone()) {
            throw new MissingSettingException("The project has to specify a license.");
        }

        specFile = new SpecFile();
        specFile.name = parameters.id;
        specFile.summary = parameters.name;
        specFile.description = parameters.description.orSome(""); // TODO: This is not right
        specFile.license = parameters.license.some();
        specFile.buildArch = parameters.architecture.orSome("noarch");
        P2<String, String> rpmVersion = getRpmVersion(parameters.version);
        specFile.version = rpmVersion._1();
        specFile.release = rpmVersion._2();

        //todo
        SpecFile.excludedSysPaths.addAll(parameters.excludeDirs.toCollection());


        return this;
    }

    public RpmUnixPackage rpmParameters(String group, Option<String> rpmbuild, String requires) {
        if (requires != null) {
            specFile.requires = List.single(requires);
        }

        specFile.group = group;
        this.rpmbuild = rpmbuild;
        return this;
    }

    public RpmUnixPackage debug(boolean debug) {
        this.specFile.dump = debug;
        this.debug = debug;
        return this;
    }

    public void beforeAssembly(FileAttributes defaultDirectoryAttributes, LocalDateTime timestamp)
            throws IOException {
        specFile.beforeAssembly(directory(BASE, new LocalDateTime(), defaultDirectoryAttributes));
        fileCollector = new FsFileCollector(workingDirectory.resolve(relativePath("assembly")));
    }

    public void addDirectory(UnixFsObject.Directory directory)
            throws IOException {
        specFile.addDirectory(directory);
        fileCollector.addDirectory(directory);
    }

    public void addFile(Fs<?> fromFile, RegularFile file)
            throws IOException {
        specFile.addFile(file);
        fileCollector.addFile(fromFile, file);
    }

    public void addSymlink(UnixFsObject.Symlink symlink)
            throws IOException {
        specFile.addSymlink(symlink);
        fileCollector.addSymlink(symlink);
    }

    public void apply(F<UnixFsObject, Option<UnixFsObject>> f) {
        specFile.apply(f);
        fileCollector.apply(f);
    }

    public RpmPreparedPackage prepare(ScriptUtil.Strategy strategy)
            throws Exception {
        File rpms = new File(workingDirectory.file, "RPMS");
        File specsDir = new File(workingDirectory.file, "SPECS");
        File tmp = new File(workingDirectory.file, "tmp");

        File specFilePath = new File(specsDir, specFile.name + ".spec");

        forceMkdir(new File(workingDirectory.file, "BUILD"));
        forceMkdir(rpms);
        forceMkdir(new File(workingDirectory.file, "SOURCES"));
        forceMkdir(specsDir);
        forceMkdir(new File(workingDirectory.file, "SRPMS"));
        forceMkdir(tmp);

        fileCollector.collect();

        ScriptUtil.Result result = scriptUtil.
                createExecution(specFile.name, "rpm", getScripts(), workingDirectory.file, strategy).
                execute();

        specFile.includePre = result.preInstall;
        specFile.includePost = result.postInstall;
        specFile.includePreun = result.preRemove;
        specFile.includePostun = result.postRemove;
        specFile.buildRoot = fileCollector.root.file;

        LineStreamUtil.toFile(specFile, specFilePath);

        return new RpmPreparedPackage(tmp, specFilePath);
    }

    public class RpmPreparedPackage
            extends UnixPackage.PreparedPackage {
        private final File tmp;

        private final File specFilePath;

        RpmPreparedPackage(File tmp, File specFilePath) {
            this.tmp = tmp;
            this.specFilePath = specFilePath;
        }

        public void packageToFile(File packageFile)
                throws Exception {
            new Rpmbuild().
                    setDebug(debug).
                    setBuildroot(specFile.buildRoot).
                    define("_tmppath " + tmp.getAbsolutePath()).
                    define("_topdir " + workingDirectory.file.getAbsolutePath()).
                    define("_rpmdir " + packageFile.getParentFile().getAbsolutePath()).
                    define("_rpmfilename " + packageFile.getName()).
                    setSpecFile(specFilePath).
                    setRpmbuild(rpmbuild).
                    buildBinary();
        }
    }

    // -----------------------------------------------------------------------
    // Static
    // -----------------------------------------------------------------------

    public static P2<String, String> getRpmVersion(PackageVersion version) {
        String v = version.version;

        String calculatedRevision = "1";

        int index = v.indexOf("-");
        if (index != -1 && index < v.length() - 1 && Character.isLetter(v.charAt(index + 1))) {
           // calculatedRevision = "0.1." + v.substring(index + 1).replace("-", "_");
           // v = v.substring(0, index);
        } else if (version.snapshot) {
            calculatedRevision = "0.1." + version.timestamp;
        }

        String r = calculatedRevision;
        if (version.revision.isSome()) {
            if (calculatedRevision.equals("1")) {
                r = version.revision.some();
            } else {
                r = calculatedRevision + "." + version.revision.some();
            }
        }

        return p(v.replace('-', '_'), r);
    }

    @Override
    public String getArchitecture() {
        return this.specFile.buildArch;
    }

    @Override
    public String getOutputFileName() {
        return this.specFile.name;
    }
}
