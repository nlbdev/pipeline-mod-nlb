<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="nlb:dtbook-to-pef" version="1.0"
    xmlns:nlb="http://www.nlb.no/ns/pipeline/xproc"
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:pef="http://www.daisy.org/ns/2008/pef"
    exclude-inline-prefixes="#all"
    name="main">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DTBook til PEF (NLB)</h1>
        <p px:role="desc">Konverterer en DTBook til PEF.</p>
        <dl px:role="author">
            <dt>Name:</dt>
            <dd px:role="name">Jostein Austvik Jacobsen</dd>
            <dt>Organization:</dt>
            <dd px:role="organization" href="http://www.nlb.no/">NLB</dd>
            <dt>E-mail:</dt>
            <dd><a px:role="contact" href="mailto:josteinaj@gmail.com">josteinaj@gmail.com</a></dd>
        </dl>
        <dl px:role="author">
            <dt>Name:</dt>
            <dd px:role="name">Ammar Usama</dd>
            <dt>Organization:</dt>
            <dd px:role="organization" href="http://www.nlb.no/">NLB</dd>
            <dt>E-mail:</dt>
            <dd><a px:role="contact" href="mailto:Ammar.Usama@nlb.no">Ammar.Usama@nlb.no</a></dd>
        </dl>
    </p:documentation>
    
    <p:input port="source" primary="true" px:name="source" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DTBook</h2>
        </p:documentation>
    </p:input>
    
    <p:option name="braille-standard"/>
    <p:option name="hyphenation"/>
    <p:option name="line-spacing"/>
    <p:option name="capital-letters"/>
    <p:option name="stylesheet"/>
    <p:option name="page-width"/>
    <p:option name="page-height"/>
    <p:option name="duplex"/>
    <p:option name="include-captions"/>
    <p:option name="include-images"/>
    <p:option name="include-line-groups"/>
    <p:option name="include-notes"/>
    <p:option name="include-production-notes"/>
    <p:option name="show-braille-page-numbers"/>
    <p:option name="show-print-page-numbers"/>
    <p:option name="toc-depth"/>
    <p:option name="colophon-metadata-placement"/>
    <p:option name="maximum-number-of-sheets"/>
    <p:option name="pef-output-dir"/>
    <p:option name="preview-output-dir"/>
    <p:option name="temp-dir"/>
    
    <p:import href="http://www.nlb.no/pipeline/modules/braille/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/dtbook-to-pef/dtbook-to-pef.xpl"/>
    
    <p:in-scope-names name="parameters"/>
    <nlb:pre-processing>
        <p:input port="parameters">
            <p:pipe port="result" step="parameters"/>
        </p:input>
        <p:input port="source">
            <p:pipe port="source" step="main"/>
        </p:input>
    </nlb:pre-processing>
    
    <px:dtbook-to-pef>
        <p:with-option name="stylesheet" select="concat('http://www.nlb.no/pipeline/modules/braille/default.scss', if ($stylesheet) then concat(' ',$stylesheet) else '')"/>
        <p:with-option name="transform" select="concat('(formatter:dotify)(translator:nlb)',$braille-standard)"/>
        <p:with-option name="main-document-language" select="'no'"/>
        <p:with-option name="include-preview" select="'true'"/>
        <p:with-option name="page-width" select="$page-width"/>
        <p:with-option name="page-height" select="$page-height"/>
        <p:with-option name="duplex" select="$duplex"/>
        <p:with-option name="hyphenation" select="$hyphenation"/>
        <p:with-option name="line-spacing" select="$line-spacing"/>
        <p:with-option name="capital-letters" select="$capital-letters"/>
        <p:with-option name="include-captions" select="$include-captions"/>
        <p:with-option name="include-images" select="$include-images"/>
        <p:with-option name="include-line-groups" select="$include-line-groups"/>
        <p:with-option name="include-note-references" select="$include-notes"/>
        <p:with-option name="include-production-notes" select="$include-production-notes"/>
        <p:with-option name="show-braille-page-numbers" select="$show-braille-page-numbers"/>
        <p:with-option name="show-print-page-numbers" select="$show-print-page-numbers"/>
        <p:with-option name="toc-depth" select="$toc-depth"/>
        <p:with-option name="colophon-metadata-placement" select="$colophon-metadata-placement"/>
        <p:with-option name="maximum-number-of-sheets" select="$maximum-number-of-sheets"/>
        <p:with-option name="pef-output-dir" select="$pef-output-dir"/>
        <p:with-option name="preview-output-dir" select="$preview-output-dir"/>
        <p:with-option name="temp-dir" select="$temp-dir"/>
    </px:dtbook-to-pef>
    
</p:declare-step>
