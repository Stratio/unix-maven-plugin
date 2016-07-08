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
import com.stratio.mojo.unix.rpm.RpmUtil
import com.stratio.mojo.unix.rpm.SpecFile

boolean success = true

File hudsonWar = findArtifact("org.jvnet.hudson.main", "hudson-war", "1.255", "war")
// Set this as the rpm command will spit out dates in a different format than what the RpmUtil can handle right now
hudsonWar.setLastModified(System.currentTimeMillis())

File rpm = findArtifact("bar", "project-rpm-1", "1.1-2", "rpm")

success &= assertRpmEntries(rpm, [
        new RpmUtil.FileInfo("/opt", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/opt/hudson", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/opt/hudson/hudson.war", "hudson", "hudson", "-rw-r--r--", 20623413, null),
        new RpmUtil.FileInfo("/opt/hudson/etc", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/opt/hudson/etc/config.properties", "root", "root", "-rw-r--r--", 14, null),
        new RpmUtil.FileInfo("/usr", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/README.txt", "root", "root", "-rw-r--r--", 41, null),
        new RpmUtil.FileInfo("/var", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/var/log", "root", "root", "drwxr-xr-x", 0, null),
        // TODO: This should assert the target
        new RpmUtil.FileInfo("/var/log/hudson", "root", "root", "lrwxrwxrwx", 19, null),
])

specFile = new SpecFile()
specFile.name = "project-rpm-1"
specFile.version = "1.1_2"
specFile.release = 1
specFile.summary = "RPM 1"
specFile.license = "BSD"
specFile.group = "Application/Collectors"

success &= assertRelaxed(specFile, RpmUtil.getSpecFileFromRpm(rpm), specFileEqual);

return success
