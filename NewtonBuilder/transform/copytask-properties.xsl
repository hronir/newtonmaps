<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0" xmlns:n="urn:newtonpaths" >
	<xsl:output method="text"/>
	
	<xsl:param name="val"></xsl:param>
	
	<xsl:template match="//n:export">
		<xsl:apply-templates select="n:exportvalue[text()=$val]"/>
	</xsl:template>
		
	<xsl:template match="//n:exportvalue">
		<xsl:value-of select="parent::*/@parameter"/>
		<xsl:text>=</xsl:text>
		<xsl:value-of select="text()"/> 
	</xsl:template>
</xsl:stylesheet>