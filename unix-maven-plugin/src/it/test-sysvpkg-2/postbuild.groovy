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
import com.stratio.mojo.unix.sysvpkg.Pkginfo
import com.stratio.mojo.unix.sysvpkg.PkginfoUtil
import fj.data.Option
import org.joda.time.LocalDateTime

Option<LocalDateTime> ldtNone = Option.none()

boolean success = true

File pkg = new File((File) basedir, "target/project-sysvpkg-2-1.1-2.pkg")

success &= assertRelaxed(
        new Pkginfo( "all", "application", "Hudson Slave", "project-sysvpkg-2", "1.1-2"),
        PkginfoUtil.getPackageInfoForDevice(pkg).some(), packageInfoEqual);

success &= assertSysvPkgEntries(pkg, [
        directory("/usr", "17777777777", "?", "?", ldtNone),
        directory("/usr/share", "0755", "nobody", "nogroup", ldtNone),
        directory("/usr/share/hudson", "0755", "nobody", "nogroup", ldtNone),
        directory("/usr/share/hudson/lib", "0755", "nobody", "nogroup", ldtNone),
        regularFile("/usr/share/hudson/lib/slave.jar", "0644", "nobody", "nogroup", 158615, 48565, ldtNone),
        directory("/usr/share/hudson/license", "0755", "nobody", "nogroup", ldtNone),
        regularFile("/usr/share/hudson/license/atom-license.txt", "0644", "nobody", "nogroup", 49, 4473, ldtNone),
        regularFile("/usr/share/hudson/license/dc-license.txt", "0644", "nobody", "nogroup", 1544, 59072, ldtNone),
        installationFile("pkginfo", 159, 0, ldtNone),
])
return success
