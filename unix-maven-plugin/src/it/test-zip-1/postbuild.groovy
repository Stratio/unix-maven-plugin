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

import static com.stratio.mojo.unix.FileAttributes.EMPTY
import static com.stratio.mojo.unix.UnixFsObject.directory
import static com.stratio.mojo.unix.UnixFsObject.regularFile
import static com.stratio.mojo.unix.maven.plugin.ShittyUtil.*

boolean success = true

def timestamps = new Timestamps(basedir).zip1

File zip = findArtifact("bar", "project-zip-1", "1.1-2", "zip")

success &= assertZipEntries(zip, [
        directory(r("/opt"), START_OF_TIME, EMPTY),
        directory(r("/opt/hudson"), START_OF_TIME, EMPTY),
        regularFile(r("/opt/hudson/hudson.war"), timestamps.hudsonWarTimestamp.timestamp, 20623413, EMPTY),

        directory(r("/opt/hudson/etc"), START_OF_TIME, EMPTY),
        regularFile(r("/opt/hudson/etc/filter-1.conf"), timestamps.filter1.timestamp, 237, EMPTY),
        regularFile(r("/opt/hudson/etc/filter-2.conf"), timestamps.filter2.timestamp, 88, EMPTY),
        regularFile(r("/opt/hudson/etc/filter-all.conf"), timestamps.filterAll.timestamp, 157, EMPTY),
        regularFile(r("/opt/hudson/etc/unfiltered.properties"), timestamps.unfiltered.timestamp, 27, EMPTY),
])

return success
