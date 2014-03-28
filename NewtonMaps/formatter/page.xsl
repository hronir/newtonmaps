<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	
	<xsl:import href="topbar.xsl" />

	<xsl:output method="html" indent="yes" version="4.0" />

	<xsl:template match="/article">
		<html lang="en">
			<!-- ===================== HEAD ============================= -->
			<head>
				<xsl:element name="meta">
					<xsl:attribute name="name">charset</xsl:attribute>
					<xsl:attribute name="content">utf-8</xsl:attribute>
				</xsl:element>
				<xsl:element name="meta">
					<xsl:attribute name="name">description</xsl:attribute>
					<xsl:attribute name="content">Introduction to interplanetary traveling</xsl:attribute>
				</xsl:element>
				<xsl:element name="meta">
					<xsl:attribute name="name">author</xsl:attribute>
					<xsl:attribute name="content">Oriol Alcaraz</xsl:attribute>
				</xsl:element>
				<title>
					Newton Maps -
					<xsl:value-of select="@title" />
				</title>
				<xsl:element name="link">
					<xsl:attribute name="rel">shortcut icon</xsl:attribute>
					<xsl:attribute name="href">@root@resources/img/icon.gif</xsl:attribute>
				</xsl:element>
				<xsl:element name="link">
					<xsl:attribute name="rel">stylesheet</xsl:attribute>
					<xsl:attribute name="href">@root@resources/css/bootstrap.css</xsl:attribute>
				</xsl:element>
				<xsl:element name="link">
					<xsl:attribute name="rel">stylesheet</xsl:attribute>
					<xsl:attribute name="href">@root@resources/css/test.css</xsl:attribute>
				</xsl:element>
				<xsl:if test="@fixed">
					<xsl:element name="link">
						<xsl:attribute name="rel">stylesheet</xsl:attribute>
						<xsl:attribute name="href">@root@resources/css/fixed.css</xsl:attribute>
					</xsl:element>
				</xsl:if>
				<script type="text/javascript" src="@root@resources/js/global.js"></script>
			</head>
			<!-- ===================== BODY ============================= -->
				<xsl:element name="body">
					<xsl:attribute name="class">
						<xsl:choose>
							<xsl:when test="quickstart">withqs</xsl:when>
							<xsl:otherwise>withoutqs</xsl:otherwise>
						</xsl:choose>
					</xsl:attribute>
				<xsl:apply-templates select="." mode="topbar" />
				<div id="masthead">
					<div class="inner">
						<div>
							<h1>Newton Maps</h1>
							<p>A passage to the solar system</p>
						</div>
						<!-- /container -->
					</div>
				</div>
				<xsl:apply-templates select="quickstart" />
				<xsl:choose>
					<xsl:when test="@fixed">
						<div id="app" class="app-normal">
							<xsl:apply-templates select="content" />
						</div>
					</xsl:when>
					<xsl:otherwise>
						<section id="content">
							<div class="container">
								<xsl:apply-templates select="heading" />
								<xsl:apply-templates select="content" />
							</div>
						</section>
					</xsl:otherwise>
				</xsl:choose>
				<!-- /container -->
				<div id="footer">
					<div class="inner">
						<p class="left">
							Author:
							<a href="mailto:hron.tlon@gmail.com">Oriol Alcaraz</a>
						</p>
						<xsl:if test="@fixed">
							<p class="right">
								<a href="javascript:zoom();">Full view</a>
							</p>
						</xsl:if>
					</div>
				</div>
			</xsl:element>
		</html>
	</xsl:template>

	<xsl:template match="article/heading">
		<div class="row">
			<div class="span4">
				<xsl:text>&#160;</xsl:text>
			</div>
			<div class="span12">
				<h1>
					<xsl:value-of select="." />
				</h1>
			</div>
		</div>
	</xsl:template>

	<xsl:template match="article/content/section">
		<div class="row">
			<div class="span4">
				<xsl:choose>
					<xsl:when test="side">
						<xsl:apply-templates select="side" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>&#160;</xsl:text>
					</xsl:otherwise>
				</xsl:choose>
			</div>
			<div class="span12">
				<xsl:apply-templates select="p" />
			</div>
		</div>
	</xsl:template>
	<xsl:template match="side/heading">
		<h3>
			<xsl:apply-templates />
		</h3>
	</xsl:template>
	<xsl:template match="side//list/list">
		<li>
			<ul>
				<xsl:apply-templates />
			</ul>
		</li>
	</xsl:template>
	<xsl:template match="side/list">
		<ul class="unstyled">
			<xsl:apply-templates />
		</ul>
	</xsl:template>
	<xsl:template match="side//list/p">
		<li>
			<xsl:apply-templates />
		</li>
	</xsl:template>
	<xsl:template match="p">
		<p>
			<xsl:apply-templates />
		</p>
	</xsl:template>
	<xsl:template match="a">
		<xsl:element name="a">
			<xsl:attribute name="href"><xsl:value-of select="@href" /></xsl:attribute>
			<xsl:apply-templates />
		</xsl:element>
	</xsl:template>
	<xsl:template match="wiki">
		<xsl:element name="a">
			<xsl:attribute name="href">http://en.wikipedia.org/wiki/<xsl:value-of
				select="@term" /></xsl:attribute>
			<xsl:apply-templates />
		</xsl:element>
	</xsl:template>
	<xsl:template match="pageref">
		<xsl:element name="a">
			<xsl:attribute name="href">@root@<xsl:value-of
				select="@id" />/</xsl:attribute>
			<xsl:apply-templates />
		</xsl:element>
	</xsl:template>
	<xsl:template match="content">
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="article/quickstart">
		<!-- Quickstart options ================================================== -->
		<div class="quickstart" id="quickstart">
			<div class="container">
				<div class="row">
					<xsl:apply-templates />
				</div>
			</div>
		</div>
	</xsl:template>

	<xsl:template match="article/quickstart/p">
		<div class="span5 columns">
			<h6>
				<xsl:value-of select="@title" />
			</h6>
			<p>
				<xsl:apply-templates />
			</p>
		</div>
	</xsl:template>

	<xsl:template match="article//nmapplet">
		<div class="app-container">
			<object type="application/x-java-applet" width="100%" height="100%">
				<!--Generic parameters for all Java applets. -->
				<xsl:element name="param">
					<xsl:attribute name="name">archive</xsl:attribute>
					<xsl:attribute name="value">@root@resources/nmaps.jar</xsl:attribute>
				</xsl:element>
				<xsl:element name="param">
					<xsl:attribute name="name">code</xsl:attribute>
					<xsl:attribute name="value">newtonpath.application.JKApplet</xsl:attribute>
				</xsl:element>

				<!--Specific parameters. -->
				<xsl:element name="param">
					<xsl:attribute name="name">colors</xsl:attribute>
					<xsl:attribute name="value">@root@data/colors.xml</xsl:attribute>
				</xsl:element>
				<xsl:element name="param">
					<xsl:attribute name="name">layout</xsl:attribute>
					<xsl:attribute name="value"><xsl:value-of
						select="@layout" /></xsl:attribute>
				</xsl:element>
				<xsl:element name="param">
					<xsl:attribute name="name">orbits</xsl:attribute>
					<xsl:attribute name="value"><xsl:value-of
						select="@orbits" /></xsl:attribute>
				</xsl:element>
				Your browser needs Java to view the Solar System.
			</object>
		</div>
	</xsl:template>

</xsl:stylesheet>