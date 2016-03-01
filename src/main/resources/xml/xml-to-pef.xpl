<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:xml-to-pef" version="1.0"
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:pef="http://www.daisy.org/ns/2008/pef"
    exclude-inline-prefixes="#all"
    name="main">
    
    
    <!-- ============= -->
    <!-- Punktstandard -->
    <!-- ============= -->
    <p:option name="braille-standard" select="'(dots:6)(grade:0)'" px:data-type="nlb:braille-standard">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Punktstandard</h2>
        </p:documentation>
    </p:option>
    
    <!-- ====== -->
    <!-- Layout -->
    <!-- ====== -->
    <p:option name="page-width" select="'40'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Layout: Sidebredde</h2>
        </p:documentation>
    </p:option>
    <p:option name="page-height" select="'25'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Layout: Arkhøyde</h2>
        </p:documentation>
    </p:option>
    <p:option name="left-margin" select="'0'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Layout: Venstre marg</h2>
        </p:documentation>
    </p:option>
    <p:option name="duplex" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Layout: Tosidig trykk</h2>
        </p:documentation>
    </p:option>
    
    <!-- ================ -->
    <!-- Tekstformatering -->
    <!-- ================ -->
    <p:option name="hyphenation" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Tekstformatering: Orddeling</h2>
        </p:documentation>
    </p:option>
    <p:option name="line-spacing" select="'single'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Tekstformatering: Linjeavstand</h2>
        </p:documentation>
    </p:option>
    <p:option name="capital-letters" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Tekstformatering: Store bokstaver</h2>
        </p:documentation>
    </p:option>
    <p:option name="downshift-ordinal-numbers" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Tekstformatering: Skift ordenstall ned</h2>
        </p:documentation>
    </p:option>
    
    <!-- =============== -->
    <!-- Blokk-elementer -->
    <!-- =============== -->
    <p:option name="include-captions" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Blokkelementer: Inkluder bildetekst og tabelltekst</h2>
        </p:documentation>
    </p:option>
    <p:option name="include-images" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Blokkelementer: Inkluder alternativ tekst for bilder</h2>
        </p:documentation>
    </p:option>
    <p:option name="include-image-groups" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Blokkelementer: Inkluder bildegrupper</h2>
        </p:documentation>
    </p:option>
    <p:option name="include-line-groups" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Blokkelementer: Inkluder vers (linjegrupper)</h2>
        </p:documentation>
    </p:option>
    
    <!-- =============== -->
    <!-- Tekst-elementer -->
    <!-- =============== -->
    <p:option name="text-level-formatting" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Tekstelementer: Marker uthevet tekst (fet/kursiv)</h2>
        </p:documentation>
    </p:option>
    <p:option name="include-note-references" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Tekstelementer: Inkluder notereferanser</h2>
        </p:documentation>
    </p:option>
    <p:option name="include-production-notes" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Tekstelementer: Inkluder produsentkommentarer</h2>
        </p:documentation>
    </p:option>
    
    <!-- ========== -->
    <!-- Sidenummer -->
    <!-- ========== -->
    <p:option name="show-braille-page-numbers" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Sidenummer: Vis punktskrift-sidenummer</h2>
        </p:documentation>
    </p:option>
    <p:option name="show-print-page-numbers" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Sidenummer: Vis original-sidenummer</h2>
        </p:documentation>
    </p:option>
    
    <!-- ================ -->
    <!-- Generert innhold -->
    <!-- ================ -->
    <p:option name="toc-depth" select="'0'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Generert innhold: Dybde på innholdsfortegnelse</h2>
        </p:documentation>
    </p:option>
    <p:option name="choice-of-colophon" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Generert innhold: Valg av kolofon</h2>
        </p:documentation>
    </p:option>
    
    <!-- ===================== -->
    <!-- Plassering av innhold -->
    <!-- ===================== -->
    <p:option name="footnotes-placement" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Plassering av innhold: Plassering av fotnoter</h2>
        </p:documentation>
    </p:option>
    <p:option name="colophon-metadata-placement" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Plassering av innhold: Plassering av kolofon/metadata</h2>
        </p:documentation>
    </p:option>
    <p:option name="rear-cover-placement" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Plassering av innhold: Plassering av baksidetekst</h2>
        </p:documentation>
    </p:option>
    
    <!-- ====== -->
    <!-- Hefter -->
    <!-- ====== -->
    <p:option name="number-of-pages" select="'50'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Hefter: Antall sider</h2>
        </p:documentation>
    </p:option>
    <p:option name="maximum-number-of-pages" select="'70'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Hefter: Største antall sider</h2>
        </p:documentation>
    </p:option>
    <p:option name="minimum-number-of-pages" select="'30'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Hefter: Minste antall sider</h2>
        </p:documentation>
    </p:option>
    
    
    <!-- ====== -->
    <!-- Utputt -->
    <!-- ====== -->
    <p:option name="pef-output-dir">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Mappe for PEF</h2>
        </p:documentation>
    </p:option>
    <p:option name="preview-output-dir">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Mappe for HTML-forhåndsvisning</h2>
        </p:documentation>
    </p:option>
    <p:option name="temp-dir">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Mappe for midlertidige filer</h2>
        </p:documentation>
    </p:option>
    
    <!-- ================== -->
    <!-- Ikke gjør noenting -->
    <!-- ================== -->
    <p:sink>
        <p:input port="source">
            <p:empty/>
        </p:input>
    </p:sink>
    
</p:declare-step>
