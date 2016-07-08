#!/bin/sh
#
# Copyright (C) 2008 Stratio (http://stratio.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


set -x

cd hudson-jetty-pkg

mvn -Dmaven.unix.debug=true clean install > mvn.txt
cmd="pkgchk -l -d target/hudson-jetty-pkg-*.pkg all"
echo "$ $cmd" > pkgchk.txt
$cmd | \
  sed "s,\(.*> from <\).*\(/target/.*\),\1..\2,g" \
  >> pkgchk.txt

cmd="pkginfo -l -d target/hudson-jetty-pkg-*.pkg"
echo "$ $cmd" > pkginfo.txt
$cmd >> pkginfo.txt
