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
import static com.stratio.mojo.unix.UnixFileMode.fromString
import com.stratio.mojo.unix.UnixFsObject
import static com.stratio.mojo.unix.UnixFsObject.directory
import static com.stratio.mojo.unix.UnixFsObject.regularFile
import static com.stratio.mojo.unix.maven.plugin.ShittyUtil.START_OF_TIME
import static com.stratio.mojo.unix.maven.plugin.ShittyUtil.assertDebEntries
import static com.stratio.mojo.unix.maven.plugin.ShittyUtil.assertFormat
import static com.stratio.mojo.unix.maven.plugin.ShittyUtil.r

boolean success = true

assertFormat "deb", "dpkg-deb", true, {
  File deb = new File(System.getProperty("user.home"), ".m2/repository/bar/project-deb-2/1.1-2/project-deb-2-1.1-2.deb")

  FileAttributes dirAttributes = new FileAttributes(none(), none(), some(fromString("rwxr-xr-x")));
  FileAttributes hudsonWarAttributes = new FileAttributes(none(), none(), some(fromString("rw-r--r--")));

  success &= assertDebEntries(deb, (List<UnixFsObject>)[
          directory(r("."), START_OF_TIME, dirAttributes),
          directory(r("usr"), START_OF_TIME, dirAttributes),
          directory(r("usr/share"), START_OF_TIME, dirAttributes),
          directory(r("usr/share/hudson"), START_OF_TIME, dirAttributes),
          directory(r("usr/share/hudson/lib"), START_OF_TIME, dirAttributes),
          regularFile(r("usr/share/hudson/lib/slave.jar"), START_OF_TIME, 158615, hudsonWarAttributes),
          directory(r("usr/share/hudson/license"), START_OF_TIME, dirAttributes),
          regularFile(r("usr/share/hudson/license/atom-license.txt"), START_OF_TIME, 49, hudsonWarAttributes),
          regularFile(r("usr/share/hudson/license/dc-license.txt"), START_OF_TIME, 1544, hudsonWarAttributes),
  ])
}

return success
