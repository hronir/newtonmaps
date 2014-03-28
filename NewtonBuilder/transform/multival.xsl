<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:n="urn:newtonpaths" version="1.0">

	<xsl:preserve-space elements="*" />

	<xsl:output method="xml" indent="yes" version="1.0" />

	<xsl:template match="/n:tasks">
		<xsl:copy>
			<xsl:copy-of select="@*" />
			<xsl:for-each select="*//n:value">
				<xsl:apply-templates select="ancestor::n:task"
					mode="subst">
					<xsl:with-param name="value">
						<xsl:value-of select="@value" />
					</xsl:with-param>
				</xsl:apply-templates>

			</xsl:for-each>
		</xsl:copy>
	</xsl:template>


	<xsl:template match="//n:*" mode="subst">
		<xsl:param name="value" />
		<xsl:copy>
			<xsl:copy-of select="@*" />
			<xsl:apply-templates mode="subst">
				<xsl:with-param name="value">
					<xsl:value-of select="$value" />
				</xsl:with-param>
			</xsl:apply-templates>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="//n:parameter[n:value]" mode="subst">
		<xsl:param name="value" />
		<xsl:copy>
			<xsl:copy-of select="@*" />
			<xsl:attribute name="value"><xsl:value-of select="$value" /></xsl:attribute>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>