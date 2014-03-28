<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:n="urn:newtonpaths" xmlns:nw="newton" version="1.0">
	
	<xsl:preserve-space elements="*" />
	
	<xsl:output method="xml" indent="yes" version="1.0" />

	<xsl:template match="/nw:tasks">
		<xsl:copy-of select="self::*" />
	</xsl:template>

	<xsl:template match="//n:orbits" xmlns="urn:newtonpaths">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="n:result">
		<xsl:copy>
			<xsl:attribute name="operation"><xsl:value-of select="@operation" /></xsl:attribute>
			<xsl:copy-of select="*/n:description" />
			<xsl:element name="orbit" xmlns="urn:newtonpaths">
				<xsl:copy-of select="*/n:parameter" />
			</xsl:element>
			<xsl:copy-of select="*/n:comment" />
		</xsl:copy>
	</xsl:template>


</xsl:stylesheet>