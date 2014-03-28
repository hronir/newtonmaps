<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:n="urn:newtonpaths"
	version="1.0">

	<xsl:output method="xml" indent="yes" version="1.0" />

	<xsl:template match="//n:orbits">
		<xsl:element name="tasks">
			<xsl:apply-templates />
		</xsl:element>
	</xsl:template>

	<xsl:template match="n:result">
		<xsl:element name="task">
		
		<xsl:element name="orbit">
			<xsl:copy-of select="self::*"/>
		</xsl:element>
		
		<xsl:element name="format">
			<xsl:apply-templates select="n:description" />
		</xsl:element>
		
		</xsl:element>
	</xsl:template>

</xsl:stylesheet>