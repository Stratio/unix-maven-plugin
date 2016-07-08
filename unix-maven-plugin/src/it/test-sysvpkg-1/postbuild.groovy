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
import static fj.data.Option.*
import fj.data.Option
import org.joda.time.LocalDateTime

Option<LocalDateTime> ldtNone = Option.none()

boolean success = true

File pkg = new File((File) basedir, "target/project-sysvpkg-1-1.1-2.pkg")

pkginfo = new Pkginfo("all", "application", "Hudson", "project-sysvpkg-1", "1.1-2").
        email(some("trygvis@inamo.no"))

success &= assertRelaxed(pkginfo, PkginfoUtil.getPackageInfoForDevice(pkg).some(), packageInfoEqual);

// Ignore dates for now
success &= assertSysvPkgEntries(pkg, [
        directory("/opt", "17777777777", "?", "?", ldtNone),
        directory("/opt/hudson", "0755", "nobody", "nogroup", ldtNone),
        regularFile("/opt/hudson/hudson.war", "0666", "hudson", "hudson", 20623413, 3301, ldtNone),
        directory("/usr", "17777777777", "?", "?", ldtNone),
        directory("/usr/share", "0755", "nobody", "nogroup", ldtNone),
        directory("/usr/share/hudson", "0755", "nobody", "nogroup", ldtNone),
        regularFile("/usr/share/hudson/README.txt", "0644", "nobody", "nogroup", 41, 3746, ldtNone),
        directory("/var", "17777777777", "?", "?", ldtNone),
        directory("/var/log", "0755", "nobody", "nogroup", ldtNone),
        symlink("/var/log/hudson", "/var/opt/hudson/log"),
        installationFile("checkinstall", 28, 2563, ldtNone),
        installationFile("compver", 0, 0, ldtNone),
        installationFile("copyright", 24, 2150, ldtNone),
        installationFile("depend", 0, 0, ldtNone),
        installationFile("pkginfo", 157, 0, ldtNone),
        installationFile("request", 46, 4055, ldtNone),
        installationFile("space", 0, 0, ldtNone),
])

return success
