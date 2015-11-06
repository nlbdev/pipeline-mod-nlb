<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
	xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
	xmlns:html="http://www.w3.org/1999/xhtml"
	xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/"
	exclude-result-prefixes="#all">
	
	<xsl:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/transform/block-translator-template.xsl"/>
	
	<xsl:param name="text-transform" required="yes"/>
	
	<xsl:template match="css:block" mode="#default before after">
		<xsl:variable name="text" as="text()*" select="//text()"/>
		<xsl:variable name="style" as="xs:string*">
			<xsl:for-each select="$text">
				<xsl:variable name="inline-style" as="element()*"
					select="css:computed-properties($inline-properties, true(), parent::*)"/>
				<xsl:variable name="transform" as="xs:string*">
					<xsl:if test="ancestor::html:strong">
						<xsl:sequence select="'louis-bold'"/>
					</xsl:if>
					<xsl:if test="ancestor::html:em">
						<xsl:sequence select="'louis-ital'"/>
					</xsl:if>
					<xsl:if test="ancestor::html:u">
						<xsl:sequence select="'louis-under'"/>
					</xsl:if>
				</xsl:variable>
				<xsl:variable name="inline-style" as="element()*"
					select="if (exists($transform)) then ($inline-style,css:property('transform',string-join($transform,' '))) else $inline-style"/>
				<xsl:sequence select="css:serialize-declaration-list($inline-style[not(@value=css:initial-value(@name))])"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:apply-templates select="node()[1]" mode="treewalk">
			<xsl:with-param name="new-text-nodes" select="pf:text-transform($text-transform,$text,$style)"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template match="css:property[@name=('font-style','font-weight','text-decoration','color')]"
	              mode="translate-declaration-list"/>
	
	<xsl:template match="css:property[@name='hyphens' and @value='auto']" mode="translate-declaration-list">
		<xsl:sequence select="css:property('hyphens','manual')"/>
	</xsl:template>
	
	
	
	<!--  code to treat italic in html and dtbook     -->	
	
	<xsl:template match="html:em | html:i  | dtbook:em | dtbook:i" mode="treewalk">       
		<xsl:param name="new-text-nodes" as="xs:string*" required="yes"/>
		<xsl:variable name="text-node-count" select="count(.//text())"/>
		<xsl:copy>
			<xsl:sequence select="@* except @style"/>
			<xsl:if test="@style">
				<xsl:variable name="translated-rules" as="element()*">
					<xsl:apply-templates select="css:parse-stylesheet(@style)" mode="translate-rule-list">
						<xsl:with-param name="context" select="." tunnel="yes"/>
					</xsl:apply-templates>
				</xsl:variable>
				<xsl:sequence select="css:style-attribute(css:serialize-stylesheet($translated-rules))"/>
			</xsl:if>
			<xsl:text>⠆</xsl:text>
			<xsl:apply-templates select="child::node()[1]" mode="#current">
				<xsl:with-param name="new-text-nodes" select="$new-text-nodes[position()&lt;=$text-node-count]"/>
			</xsl:apply-templates>
			<xsl:text>⠰</xsl:text>
		</xsl:copy>
		<xsl:apply-templates select="following-sibling::node()[1]" mode="#current">
			<xsl:with-param name="new-text-nodes" select="$new-text-nodes[position()&gt;$text-node-count]"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--  code to treat underline in html and dtbook     -->	
	
	<xsl:template match="html:u | dtbook:u " mode="treewalk">      
		<xsl:param name="new-text-nodes" as="xs:string*" required="yes"/>
		<xsl:variable name="text-node-count" select="count(.//text())"/>
		<xsl:copy>
			<xsl:sequence select="@* except @style"/>
			<xsl:if test="@style">
				<xsl:variable name="translated-rules" as="element()*">
					<xsl:apply-templates select="css:parse-stylesheet(@style)" mode="translate-rule-list">
						<xsl:with-param name="context" select="." tunnel="yes"/>
					</xsl:apply-templates>
				</xsl:variable>
				<xsl:sequence select="css:style-attribute(css:serialize-stylesheet($translated-rules))"/>
			</xsl:if>
			<xsl:text>⠸</xsl:text>
			<xsl:apply-templates select="child::node()[1]" mode="#current">
				<xsl:with-param name="new-text-nodes" select="$new-text-nodes[position()&lt;=$text-node-count]"/>
			</xsl:apply-templates>
			<xsl:text>⠸</xsl:text>
		</xsl:copy>
		<xsl:apply-templates select="following-sibling::node()[1]" mode="#current">
			<xsl:with-param name="new-text-nodes" select="$new-text-nodes[position()&gt;$text-node-count]"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--  code to treat strikeline in html and dtbook     -->	
	
	<xsl:template match="html:strike |html:s| dtbook:strike |dtbook:s" mode="treewalk">      
		<xsl:param name="new-text-nodes" as="xs:string*" required="yes"/>
		<xsl:variable name="text-node-count" select="count(.//text())"/>
		<xsl:copy>
			<xsl:sequence select="@* except @style"/>
			<xsl:if test="@style">
				<xsl:variable name="translated-rules" as="element()*">
					<xsl:apply-templates select="css:parse-stylesheet(@style)" mode="translate-rule-list">
						<xsl:with-param name="context" select="." tunnel="yes"/>
					</xsl:apply-templates>
				</xsl:variable>
				<xsl:sequence select="css:style-attribute(css:serialize-stylesheet($translated-rules))"/>
			</xsl:if>
			<xsl:text>⠐⠂</xsl:text>
			<xsl:apply-templates select="child::node()[1]" mode="#current">
				<xsl:with-param name="new-text-nodes" select="$new-text-nodes[position()&lt;=$text-node-count]"/>
			</xsl:apply-templates>
			<xsl:text>⠐⠂</xsl:text>
		</xsl:copy>
		<xsl:apply-templates select="following-sibling::node()[1]" mode="#current">
			<xsl:with-param name="new-text-nodes" select="$new-text-nodes[position()&gt;$text-node-count]"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--  code to treat bold/strong in html and dtbook     -->	
	
	<xsl:template match="html:strong |html:b| dtbook:strong |dtbook:b" mode="treewalk">      
		<xsl:param name="new-text-nodes" as="xs:string*" required="yes"/>
		<xsl:variable name="text-node-count" select="count(.//text())"/>
		<xsl:copy>
			<xsl:sequence select="@* except @style"/>
			<xsl:if test="@style">
				<xsl:variable name="translated-rules" as="element()*">
					<xsl:apply-templates select="css:parse-stylesheet(@style)" mode="translate-rule-list">
						<xsl:with-param name="context" select="." tunnel="yes"/>
					</xsl:apply-templates>
				</xsl:variable>
				<xsl:sequence select="css:style-attribute(css:serialize-stylesheet($translated-rules))"/>
			</xsl:if>
			<xsl:text>⠠⠄</xsl:text>
			<xsl:apply-templates select="child::node()[1]" mode="#current">
				<xsl:with-param name="new-text-nodes" select="$new-text-nodes[position()&lt;=$text-node-count]"/>
			</xsl:apply-templates>
			<xsl:text>⠠⠄</xsl:text>
		</xsl:copy>
		<xsl:apply-templates select="following-sibling::node()[1]" mode="#current">
			<xsl:with-param name="new-text-nodes" select="$new-text-nodes[position()&gt;$text-node-count]"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--       -->
	
	
</xsl:stylesheet>