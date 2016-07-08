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
import com.stratio.mojo.unix.maven.plugin.Timestamps

import static com.stratio.mojo.unix.maven.plugin.ShittyUtil.*
import static com.stratio.mojo.unix.FileAttributes.*
import static com.stratio.mojo.unix.UnixFsObject.*
import org.joda.time.*;

boolean success = true

def timestamps = new Timestamps(basedir).zip3

File main = findArtifact("bar", "project-zip-3", "1.1-2", "zip")

ts1 = new LocalDateTime(2008, 10, 2, 2, 7, 36)

licenseDownstream = regularFile(r("/usr/share/hudson/LICENSE-downstream.txt"), timestamps.licenseDownstream.timestamp, 15, EMPTY)

println "************************************************************************"
println "Hudson Master"

success &= assertZipEntries(main, [
        directory(r("/usr"), START_OF_TIME, EMPTY),
        directory(r("/usr/share"), START_OF_TIME, EMPTY),
        directory(r("/usr/share/hudson"), START_OF_TIME, EMPTY),
        licenseDownstream,
        directory(r("/usr/share/hudson/lib"), START_OF_TIME, EMPTY),
        directory(r("/usr/share/hudson/license"), START_OF_TIME, EMPTY),
        regularFile(r("/usr/share/hudson/lib/hudson.war"), timestamps.hudsonWarTimestamp.timestamp, 20623413, EMPTY),
        regularFile(r("/usr/share/hudson/license/atom-license.txt"), ts1, 49, EMPTY),
        regularFile(r("/usr/share/hudson/license/dc-license.txt"), ts1, 1544, EMPTY),
        directory(r("usr/share/hudson/server"), START_OF_TIME, EMPTY),
        regularFile(r("usr/share/hudson/server/README.txt"), timestamps.readme_default.timestamp, 35, EMPTY),
])

println "************************************************************************"
println "Hudson Slave"

File slave = findArtifact("bar", "project-zip-3", "1.1-2", "zip", "slave")
success &= assertZipEntries(slave, [
        directory(r("/usr"), START_OF_TIME, EMPTY),
        directory(r("/usr/share"), START_OF_TIME, EMPTY),
        directory(r("/usr/share/hudson"), START_OF_TIME, EMPTY),
        licenseDownstream,
        directory(r("/usr/share/hudson/lib"), START_OF_TIME, EMPTY),
        regularFile(r("/usr/share/hudson/lib/slave.jar"), ts1, 158615, EMPTY),
        directory(r("/usr/share/hudson/license"), START_OF_TIME, EMPTY),
        regularFile(r("/usr/share/hudson/license/atom-license.txt"), ts1, 49, EMPTY),
        regularFile(r("/usr/share/hudson/license/dc-license.txt"), ts1, 1544, EMPTY),
        directory(r("usr/share/hudson/slave"), START_OF_TIME, EMPTY),
        regularFile(r("usr/share/hudson/slave/README.txt"), timestamps.readme_slave.timestamp, 34, EMPTY),
])

return success
