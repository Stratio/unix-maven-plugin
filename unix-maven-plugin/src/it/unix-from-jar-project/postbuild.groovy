/**
 * Copyright (C) 2003 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import static fj.data.Option.none
import com.stratio.mojo.unix.maven.*
import com.stratio.mojo.unix.sysvpkg.*
import com.stratio.mojo.unix.sysvpkg.PkginfoUtil.PackageInfo

String userHome = System.getProperty("user.home")
File jar = new File(userHome, ".m2/repository/bar/project-uber-1/1.1-2/project-uber-1-1.1-2.jar")
File deb = new File(userHome, ".m2/repository/bar/project-uber-1/1.1-2/project-uber-1-1.1-2.deb")
File pkg = new File(userHome, ".m2/repository/bar/project-uber-1/1.1-2/project-uber-1-1.1-2.pkg")
File rpm = new File(userHome, ".m2/repository/bar/project-uber-1/1.1-2/project-uber-1-1.1-2.rpm")
File zip = new File(userHome, ".m2/repository/bar/project-uber-1/1.1-2/project-uber-1-1.1-2.zip")

// return jar.canRead() && deb.canRead() && pkg.canRead() && rpm.canRead()

boolean success = true

success &= ShittyUtil.assertRelaxed(
        new PackageInfo( "project-uber-1", "Uber Project", "application", "all", "1.1-2", none(), none() ),
        PkginfoUtil.getPackageInfoForDevice(pkg).some());
success &= ShittyUtil.assertSysvPkgEntries(pkg, [
        PkgchkUtil.directory("/usr", "17777777777", "?", "?", none()),
        PkgchkUtil.directory("/usr/share", "0755", "nobody", "nogroup", none()),
        PkgchkUtil.directory("/usr/share/hello", "0755", "nobody", "nogroup", none()),
        PkgchkUtil.directory("/usr/share/hello/bin", "0755", "nobody", "nogroup", none()),
        PkgchkUtil.regularFile("/usr/share/hello/bin/hello", "0755", "bah", "bah", 3092, 32472, none()),
        PkgchkUtil.regularFile("/usr/share/hello/bin/hello.bat", "0755", "bah", "bah", 2764, 15679, none()),
        PkgchkUtil.directory("/usr/share/hello/repo", "0755", "nobody", "nogroup", none()),
        PkgchkUtil.directory("/usr/share/hello/repo/bar", "0755", "nobody", "nogroup", none()),
        PkgchkUtil.directory("/usr/share/hello/repo/bar/project-uber-1", "0755", "nobody", "nogroup", none()),
        PkgchkUtil.directory("/usr/share/hello/repo/bar/project-uber-1/1.1-2", "0755", "nobody", "nogroup", none()),
        // Do not assert size or checksum on the JAR file, it varied between each build (it is built at the same time as the package)
        PkgchkUtil.regularFile("/usr/share/hello/repo/bar/project-uber-1/1.1-2/project-uber-1-1.1-2.jar", "0044", "hudson", "hudson", 0, 0, none()),
        PkgchkUtil.regularFile("/usr/share/hello/repo/bar/project-uber-1/maven-metadata-appassembler.xml", "0044", "hudson", "hudson", 0, 0, none()),
        PkgchkUtil.directory("/usr/share/hudson", "0755", "nobody", "nogroup", none()),
        PkgchkUtil.directory("/usr/share/hudson/lib", "0755", "nobody", "nogroup", none()),
        PkgchkUtil.regularFile("/usr/share/hudson/lib/hudson.war", "0044", "hudson", "hudson", 20623413, 3301, none()),
        PkgchkUtil.regularFile("/usr/share/hudson/lib/my-native.so", "0044", "hudson", "hudson", 21, 1712, none()),
        PkgchkUtil.installationFile("pkginfo", 150, 0, none()),
])


return jar.canRead() && success
