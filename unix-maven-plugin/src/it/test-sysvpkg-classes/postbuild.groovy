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
import static com.stratio.mojo.unix.maven.plugin.ShittyUtil.*
import static com.stratio.mojo.unix.sysvpkg.PkgchkUtil.*
import com.stratio.mojo.unix.sysvpkg.PkginfoUtil
import fj.data.Option
import org.joda.time.LocalDateTime
import com.stratio.mojo.unix.sysvpkg.Pkginfo;

Option<LocalDateTime> ldtNone = Option.none()

boolean success = true

File pkg = new File((File) basedir, "target/project-sysvpkg-classes-1.1.pkg")

success &= assertRelaxed(
        new Pkginfo("all", "application", "Hudson", "project-sysvpkg-classes", "1.1"),
        PkginfoUtil.getPackageInfoForDevice(pkg).some(), packageInfoEqual);

// Ignore dates for now
success &= assertSysvPkgEntries(pkg, [
        directory("/opt", "17777777777", "?", "?", ldtNone),
        directory("/opt/hudson", "0755", "nobody", "nogroup", ldtNone),
        regularFile("/opt/hudson/hudson.war", "0644", "nobody", "nogroup", 20623413, 3301, ldtNone),
        directory("/var", "17777777777", "?", "?", ldtNone),
        directory("/var/lib", "0755", "nobody", "nogroup", ldtNone),
        regularFile("/var/lib/app-method", "0644", "nobody", "nogroup", 30, 2290, ldtNone),
        regularFile("/var/lib/app-manifest.xml", "0644", "nobody", "nogroup", 36, 3285, ldtNone),
        installationFile("pkginfo", 161, 0, ldtNone),
])

return success
