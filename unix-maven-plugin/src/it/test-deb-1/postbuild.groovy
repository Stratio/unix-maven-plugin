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
import static fj.data.Option.some
import com.stratio.mojo.unix.FileAttributes
import static com.stratio.mojo.unix.FileAttributes.EMPTY
import static com.stratio.mojo.unix.UnixFileMode.fromString
import com.stratio.mojo.unix.UnixFsObject
import static com.stratio.mojo.unix.UnixFsObject.directory
import static com.stratio.mojo.unix.UnixFsObject.regularFile
import static com.stratio.mojo.unix.UnixFsObject.symlink
import static com.stratio.mojo.unix.maven.plugin.ShittyUtil.START_OF_TIME
import static com.stratio.mojo.unix.maven.plugin.ShittyUtil.assertDebEntries
import static com.stratio.mojo.unix.maven.plugin.ShittyUtil.assertFormat
import static com.stratio.mojo.unix.maven.plugin.ShittyUtil.r
import com.stratio.mojo.unix.maven.plugin.Timestamps

boolean success = true

def timestamps = new Timestamps(basedir).deb1

assertFormat "deb", "dpkg-deb", true, {
  File deb = new File(System.getProperty("user.home"), ".m2/repository/bar/project-deb-1/1.1-2/project-deb-1-1.1-2.deb")

  FileAttributes dirAttributes = new FileAttributes(none(), none(), some(fromString("rwxr-xr-x")));
  FileAttributes hudsonWarAttributes = new FileAttributes(none(), none(), some(fromString("rw-r--r--")));
  FileAttributes logAttributes = new FileAttributes(some("root"), some("root"), some(fromString("rw-r--r--")));

  // Ignore dates for now
  success &= assertDebEntries(deb, (List<UnixFsObject>)[
          directory(r("."), START_OF_TIME, dirAttributes),
          directory(r("opt",), START_OF_TIME, dirAttributes),
          directory(r("opt/hudson",), START_OF_TIME, dirAttributes),
          regularFile(r("opt/hudson/hudson.war"), START_OF_TIME, 20623413, hudsonWarAttributes),
          directory(r("opt/hudson/etc",), START_OF_TIME, dirAttributes),
          regularFile(r("/opt/hudson/etc/config.properties"), timestamps.configProperties.timestamp, 14, EMPTY),
          directory(r("var",), START_OF_TIME, dirAttributes),
          directory(r("var/log",), START_OF_TIME, logAttributes),
          symlink(r("var/log/hudson"), START_OF_TIME, some("root"), some("root"), "/var/opt/hudson/log"),
  ])
}

return success
