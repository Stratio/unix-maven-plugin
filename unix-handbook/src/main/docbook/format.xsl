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
                xmlns:mvn="http://maven.apache.org/POM/4.0.0">
  <xsl:output method="xml" indent="no" encoding="UTF-8" omit-xml-declaration="yes"/>
  <xsl:strip-space elements="*"/>
  <xsl:preserve-space elements="xsl:text"/>

  <xsl:template match="*">
    <xsl:param name="indent"/>
    <xsl:value-of select="$indent"/>

    <!-- If there is only one node, it is text so it should be indented with the current indent and written out -->
    <xsl:choose>
      <xsl:when test="count(descendant::node()) = 0">
        <xsl:element name="{name()}" namespace="{namespace-uri()}">
          <xsl:value-of select="child::text()"/>
        </xsl:element>
        <xsl:text>
</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:element name="{name()}" namespace="{namespace-uri()}">
          <xsl:text>
</xsl:text>
          <xsl:apply-templates>
            <xsl:with-param name="indent" select="concat($indent, '  ')"/>
          </xsl:apply-templates>
          <xsl:value-of select="$indent"/>
        </xsl:element>
        <xsl:text>
</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
