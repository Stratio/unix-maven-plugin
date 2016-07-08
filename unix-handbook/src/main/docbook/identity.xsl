<!--

    Copyright (C) 2008 Stratio (http://stratio.com)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:s6hl="java:net.sf.xslthl.ConnectorSaxon6"
                xmlns:xslthl="http://xslthl.sf.net"
                xmlns:db="http://docbook.org/ns/docbook"
                extension-element-prefixes="s6hl xslthl">

  <!--
   |
   | Transformation that does a one-to-one mapping of all elements.
   |
   | Used to resolve all xi:include elements before later processing.
   |
   |-->

  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Not needed. Was used to make sure all textdata references was absolute
  <xsl:template match="db:textdata">
    <xsl:message>basedir: <xsl:value-of select="system-property('basedir')"/></xsl:message>
    <xsl:variable name="basedir" select="system-property('basedir')"/>
    <db:textdata>
      <xsl:attribute name="fileref">
        <xsl:value-of select="concat($basedir, '/',@fileref)"/>
      </xsl:attribute>
    </db:textdata>
  </xsl:template>
  -->

</xsl:stylesheet>
