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

File rpm = findArtifact("bar", "project-rpm-2", "1.1-2", "rpm")

success &= assertRpmEntries(rpm, [
        new RpmUtil.FileInfo("/usr", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/lib", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/lib/slave.jar", "hudson", "hudson", "-r--r--r--", 158615, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license", "root", "root", "drwxr-xr-x", 0, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license/atom-license.txt", "nobody", "nobody", "-r--r--r--", 49, null),
        new RpmUtil.FileInfo("/usr/share/hudson/license/dc-license.txt", "nobody", "nobody", "-r--r--r--", 1544, null),
])

specFile = new SpecFile()
specFile.name = "project-rpm-2"
specFile.version = "1.1_2"
specFile.release = 1
specFile.summary = "RPM 2"
specFile.license = "BSD"
specFile.group = "Application/Collectors"

success &= assertRelaxed(specFile, RpmUtil.getSpecFileFromRpm(rpm), specFileEqual);

return success
