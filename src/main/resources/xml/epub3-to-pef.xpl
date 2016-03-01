<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="nlb:epub3-to-pef" version="1.0"
    xmlns:nlb="http://www.nlb.no/ns/pipeline/xproc"
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:pef="http://www.daisy.org/ns/2008/pef"
    exclude-inline-prefixes="#all"
    name="main">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">NLB: EPUB 3 til PEF</h1>
        <p px:role="desc">Konverterer en EPUB 3-bok til PEF.</p>
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
    
    <p:option name="epub" required="true" px:type="anyFileURI" px:sequence="false" px:media-type="application/epub+zip application/oebps-package+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">EPUB 3</h2>
            <p px:role="desc">EPUBen du vil konvertere til PEF.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="braille-standard"/>
    <p:option name="page-width"/>
    <p:option name="page-height"/>
    <p:option name="left-margin"/>
    <p:option name="duplex"/>
    <p:option name="hyphenation"/>
    <p:option name="line-spacing"/>
    <p:option name="capital-letters"/>
    <p:option name="downshift-ordinal-numbers"/>
    <p:option name="include-captions"/>
    <p:option name="include-images"/>
    <p:option name="include-image-groups"/>
    <p:option name="include-line-groups"/>
    <p:option name="text-level-formatting"/>
    <p:option name="include-note-references"/>
    <p:option name="include-production-notes"/>
    <p:option name="show-braille-page-numbers"/>
    <p:option name="show-print-page-numbers"/>
    <p:option name="toc-depth"/>
    <p:option name="choice-of-colophon"/>
    <p:option name="footnotes-placement"/>
    <p:option name="colophon-metadata-placement"/>
    <p:option name="rear-cover-placement"/>
    <p:option name="number-of-pages"/>
    <p:option name="maximum-number-of-pages"/>
    <p:option name="minimum-number-of-pages"/>
    <p:option name="pef-output-dir"/>
    <p:option name="preview-output-dir"/>
    <p:option name="temp-dir"/>
    
    <p:import href="http://www.nlb.no/pipeline/modules/braille/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/epub3-to-pef/epub3-to-pef.xpl"/>
    
    <px:message message="Running NLB-specific pre-processing steps"/>
    <!-- nothing here yet -->
    <px:message message="Finished running NLB-specific pre-processing steps" severity="DEBUG"/>
    
    <px:epub3-to-pef>
        <p:with-option name="epub" select="$epub"/>
        <p:with-option name="stylesheet" select="'http://www.nlb.no/pipeline/modules/braille/default.css'"/>
        <p:with-option name="transform" select="concat('(formatter:dotify)(translator:nlb)',$braille-standard)"/>
        <p:with-option name="main-document-language" select="'no'"/>
        <p:with-option name="include-symbols-list" select="'false'"/>
        <p:with-option name="page-width" select="$page-width"/>
        <p:with-option name="page-height" select="$page-height"/>
        <p:with-option name="left-margin" select="$left-margin"/>
        <p:with-option name="duplex" select="$duplex"/>
        <p:with-option name="hyphenation" select="$hyphenation"/>
        <p:with-option name="line-spacing" select="$line-spacing"/>
        <p:with-option name="capital-letters" select="$capital-letters"/>
        <p:with-option name="downshift-ordinal-numbers" select="$downshift-ordinal-numbers"/>
        <p:with-option name="include-captions" select="$include-captions"/>
        <p:with-option name="include-images" select="$include-images"/>
        <p:with-option name="include-image-groups" select="$include-image-groups"/>
        <p:with-option name="include-line-groups" select="$include-line-groups"/>
        <p:with-option name="text-level-formatting" select="$text-level-formatting"/>
        <p:with-option name="include-note-references" select="$include-note-references"/>
        <p:with-option name="include-production-notes" select="$include-production-notes"/>
        <p:with-option name="show-braille-page-numbers" select="$show-braille-page-numbers"/>
        <p:with-option name="show-print-page-numbers" select="$show-print-page-numbers"/>
        <p:with-option name="toc-depth" select="$toc-depth"/>
        <p:with-option name="choice-of-colophon" select="$choice-of-colophon"/>
        <p:with-option name="footnotes-placement" select="$footnotes-placement"/>
        <p:with-option name="colophon-metadata-placement" select="$colophon-metadata-placement"/>
        <p:with-option name="rear-cover-placement" select="$rear-cover-placement"/>
        <p:with-option name="number-of-pages" select="$number-of-pages"/>
        <p:with-option name="maximum-number-of-pages" select="$maximum-number-of-pages"/>
        <p:with-option name="minimum-number-of-pages" select="$minimum-number-of-pages"/>
        <p:with-option name="pef-output-dir" select="$pef-output-dir"/>
        <p:with-option name="preview-output-dir" select="$preview-output-dir"/>
        <p:with-option name="temp-dir" select="$temp-dir"/>
    </px:epub3-to-pef>
    
</p:declare-step>
