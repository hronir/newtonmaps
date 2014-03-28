<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">

	<xsl:template match="article" mode="topbar">
		<xsl:comment>
			Topbar
			<xsl:value-of select="@topbarid" />
			==================================================
		</xsl:comment>
		<div class="topbar">
			<div class="fill">
				<div class="container">
					<xsl:apply-templates select="document('topbar.xml')/topbarcollection">
						<xsl:with-param name="topbarid">
							<xsl:choose>
								<xsl:when test="@topbarid">
									<xsl:value-of select="@topbarid" />
								</xsl:when>
								<xsl:otherwise>main</xsl:otherwise>
							</xsl:choose>
						</xsl:with-param>
						<xsl:with-param name="current">
							<xsl:value-of select="@current" />
						</xsl:with-param>
					</xsl:apply-templates>
				</div>
			</div>
		</div>
	</xsl:template>

	<xsl:template match="topbarcollection">
		<xsl:param name="topbarid">main</xsl:param>
		<xsl:param name="current">main</xsl:param>
		<xsl:apply-templates select="topbar[@id=$topbarid]">
			<xsl:with-param name="current">
				<xsl:value-of select="$current" />
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="topbar">
		<xsl:param name="current">main</xsl:param>
		<h3><a href="@root@index/">Newton Maps</a></h3>
		<xsl:apply-templates select="main">
			<xsl:with-param name="current">
				<xsl:value-of select="$current" />
			</xsl:with-param>
		</xsl:apply-templates>
		<ul>
			<xsl:apply-templates select="item">
				<xsl:with-param name="current">
					<xsl:value-of select="$current" />
				</xsl:with-param>
			</xsl:apply-templates>
		</ul>
	</xsl:template>

	<xsl:template match="topbar/main">
		<xsl:param name="current">main</xsl:param>
		<xsl:element name="h3">
			<xsl:if test="@href=$current">
				<xsl:attribute name="class">active</xsl:attribute>
			</xsl:if>
			<xsl:element name="a">
				<xsl:attribute name="href">@root@<xsl:value-of select="@href" />/</xsl:attribute>
				<xsl:apply-templates />
			</xsl:element>
		</xsl:element>
	</xsl:template>

	<xsl:template match="topbar/item">
		<xsl:param name="current">main</xsl:param>
		<xsl:element name="li">
			<xsl:if test="@id=$current">
				<xsl:attribute name="class">active</xsl:attribute>
			</xsl:if>
			<xsl:element name="a">
				<xsl:attribute name="href">@root@<xsl:value-of select="@href" />/</xsl:attribute>
				<xsl:apply-templates />
			</xsl:element>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>

