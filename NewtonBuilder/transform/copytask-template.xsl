<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0" xmlns:n="urn:newtonpaths" >

	<xsl:output method="xml" indent="yes" version="1.0" />

	<xsl:template match="//n:export">
		<xsl:element name="project">
			<xsl:attribute name="name">newtonpath-batch</xsl:attribute>
			<xsl:attribute name="default">copy</xsl:attribute>

			<xsl:element name="target">
				<xsl:attribute name="name">copy</xsl:attribute>

				<xsl:apply-templates/>

			</xsl:element>

		</xsl:element>
	</xsl:template>
	
	<xsl:template match="//n:export/n:exportvalue">
		<xsl:element name="ant">
			<xsl:attribute name="antfile">buildfiles/exec-batch.xml</xsl:attribute>
			<xsl:attribute name="target">-copy-task</xsl:attribute>

			<xsl:element name="property">
				<xsl:attribute name="name">taskfile</xsl:attribute>
				<xsl:attribute name="value">${taskfile}</xsl:attribute>
			</xsl:element>

			<xsl:element name="property">
				<xsl:attribute name="name">value</xsl:attribute>
				<xsl:attribute name="value" ><xsl:value-of select="text()"/>
				</xsl:attribute>
			</xsl:element>

		</xsl:element>
	</xsl:template>
</xsl:stylesheet>