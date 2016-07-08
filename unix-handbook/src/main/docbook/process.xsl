<?xml version="1.0"?>
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
                xmlns:unix="http://mojo.codehaus.org/unix/conditional">

  <xsl:output method="xml" indent="yes"/>
  <!--
  <xsl:strip-space elements="*"/>
  -->

  <xsl:param name="format"/>
  <xsl:param name="version"/>

  <xsl:template match="*">
    <xsl:element name="{name()}" namespace="{namespace-uri()}">
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="//unix:version">
    <xsl:message>in version
      <xsl:value-of select="$version"/>
    </xsl:message>
    <xsl:element name="version">
      <xsl:value-of select="$version"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="*[@unix:format]">
    <!--
    <xsl:message>
      $format: <xsl:value-of select="$format"/>
      @format <xsl:value-of select="@format"/>
      @unix:format <xsl:value-of select="@unix:format"/>
    </xsl:message>
    -->
    <!--
    <xsl:message>in format: <xsl:value-of select="$format"/></xsl:message>
    -->
    <xsl:if test="@unix:format=$format">
      <!--
        <xsl:apply-templates/>
      -->
      <xsl:element name="{name()}" namespace="{namespace-uri()}">
        <xsl:apply-templates/>
      </xsl:element>
    </xsl:if>
    <!--
    <xsl:choose>
      <xsl:when test="@format = 'pkg'">
        <xsl:message>pkg!</xsl:message>
      </xsl:when>
      <xsl:when test="@format = 'rpm'">
        <xsl:message>rpm!</xsl:message>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">Unknown format <xsl:value-of select="@format"/></xsl:message>
      </xsl:otherwise>
    </xsl:choose>
    -->
  </xsl:template>

</xsl:stylesheet>
