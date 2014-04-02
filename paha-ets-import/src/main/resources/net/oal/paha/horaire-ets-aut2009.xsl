<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dyn="http://exslt.org/dynamic"
	xmlns:str="http://exslt.org/strings"
	xmlns:exsl="http://exslt.org/common"
	extension-element-prefixes="xsl dyn str exsl" version="1.0">

	<xsl:output method="xml" omit-xml-declaration="no" indent="yes"/>

	<xsl:param name="debug.limite.phase"></xsl:param> 
	<xsl:param name="debug.limite.output"></xsl:param> 
	<xsl:param name="debug.liste.avant">0,0</xsl:param> 
	<xsl:param name="debug.show.avant">0,0</xsl:param> 
	<xsl:param name="debug.liste.apres">0,0</xsl:param> 
	<xsl:param name="debug.show.apres">0,0</xsl:param> 

	<xsl:param name="xpos.delta">1</xsl:param>
	<xsl:param name="ypos.delta">1</xsl:param> 

	<xsl:param name="entete.xpath">ancestor::page/line[text = 'COURS'][1]</xsl:param>
	<xsl:param name="entete.coursid.xpath"><xsl:value-of select="$entete.xpath"/>/text[. = 'COURS']</xsl:param>
	<xsl:param name="entete.groupe.xpath"><xsl:value-of select="$entete.xpath"/>/text[. = 'GR']</xsl:param>
	<xsl:param name="entete.jour.xpath"><xsl:value-of select="$entete.xpath"/>/text[. = 'JOUR']</xsl:param>
	<xsl:param name="entete.heure.xpath"><xsl:value-of select="$entete.xpath"/>/text[. = 'HEURE']</xsl:param>
	<xsl:param name="entete.activite.xpath"><xsl:value-of select="$entete.xpath"/>/text[. = 'ACTIVITÉ']</xsl:param>
	<xsl:param name="entete.local.xpath"><xsl:value-of select="$entete.xpath"/>/text[. = 'LOCAL']</xsl:param>
	<xsl:param name="entete.prog.xpath"><xsl:value-of select="$entete.xpath"/>/text[. = 'PROGRAMME']</xsl:param>
	<xsl:param name="entete.prealables.xpath"><xsl:value-of select="$entete.xpath"/>/text[. = 'PRÉALABLES']</xsl:param>

	<xsl:param name="coursid.xpos.delta">1</xsl:param>
	<xsl:param name="groupe.xpos.delta">1</xsl:param>
	<xsl:param name="jour.xpos.delta">1</xsl:param>
	<xsl:param name="heure.xpos.delta">10</xsl:param>
	<xsl:param name="activite.xpos.delta">1</xsl:param>
	<xsl:param name="local.xpos.delta">40</xsl:param>
	<xsl:param name="prog.xpos.delta">30</xsl:param>
	<xsl:param name="prealables.xpos.delta">70</xsl:param>

	<xsl:template match="/">
<!--
	--><xsl:call-template name="multiphase"><xsl:with-param name="content" select="/"/><xsl:with-param name="currentPhase" select="1"/></xsl:call-template><!--
 --></xsl:template>

	<xsl:template name="multiphase"><!--
	--><xsl:param name="content"/><!-- 
	--><xsl:param name="currentPhase">1</xsl:param><!-- 

	--><xsl:message>Traitement de la phase <xsl:value-of select="$currentPhase"/> : <xsl:value-of select="count($content/*)"/> elements</xsl:message><!--

	--><xsl:if test="str:split($debug.liste.avant, ',')[$currentPhase][. != '']"><!--
			--><xsl:variable name="nbElement" select="str:split($debug.liste.avant, ',')[$currentPhase]"/><!--
			--><xsl:for-each select="$content/*[position() &lt;= $nbElement]"><!--
			--><xsl:message>Avant - Element <xsl:value-of select="position()"/> : <xsl:value-of select="local-name(.)"/> = <xsl:copy-of select="."/></xsl:message><!--
		--></xsl:for-each><!--
	--></xsl:if><!--

	--><xsl:variable name="newContent"><!--
	--><xsl:choose><!-- 
		--><xsl:when test="$currentPhase = 1"><!-- 
			--><xsl:message>Identification des elements...</xsl:message><!--
			--><root><xsl:call-template name="identifie"/></root><!--
			--><xsl:message>Identification des elements terminee.</xsl:message><!--
		--></xsl:when><!-- 
		--><xsl:when test="$currentPhase = 2"><!-- 
			--><xsl:message>Filtrage des elements...</xsl:message><!--
			--><root><xsl:call-template name="filtre"><xsl:with-param name="content" select="$content"/></xsl:call-template></root><!--
			--><xsl:message>Filtrage des elements termine.</xsl:message><!--
		--></xsl:when><!-- 
		--><xsl:when test="$currentPhase = 3"><!-- 
			--><xsl:message>Regroupement des elements d'une periode de cours...</xsl:message><!--
			--><root><xsl:call-template name="periodeCours"><xsl:with-param name="content" select="$content"/></xsl:call-template></root><!-- 
			--><xsl:message>Regroupement des elements d'une periode de cours termine.</xsl:message><!--
		--></xsl:when><!-- 
		--><xsl:when test="$currentPhase = 4"><!-- 
			--><xsl:message>Regroupement des elements d'un groupe...</xsl:message><!--
			--><root><xsl:call-template name="groupe"><xsl:with-param name="content" select="$content"/></xsl:call-template></root><!-- 
			--><xsl:message>Regroupement des elements d'un groupe termine.</xsl:message><!--
		--></xsl:when><!-- 
		--><xsl:when test="$currentPhase = 5"><!-- 
			--><xsl:message>Regroupement des elements d'un cours...</xsl:message><!--
			--><root><xsl:call-template name="cours"><xsl:with-param name="content" select="$content"/></xsl:call-template></root><!-- 
			--><xsl:message>Regroupement des elements d'un cours termine.</xsl:message><!--
		--></xsl:when><!-- 
		--><xsl:when test="$currentPhase = 6"><!-- 
			--><xsl:message>Regroupement des elements de l'horaire...</xsl:message><!--
			--><root><xsl:call-template name="horaire"><xsl:with-param name="content" select="$content"/></xsl:call-template></root><!-- 
			--><xsl:message>Regroupement des elements de l'horaire termine.</xsl:message><!--
		--></xsl:when><!-- 
		--><xsl:otherwise><!-- 
		--></xsl:otherwise><!-- 
	--></xsl:choose><!-- 
	--></xsl:variable><!-- 

	--><xsl:message>Traitement de la phase <xsl:value-of select="$currentPhase"/> terminee : <xsl:value-of select="count(exsl:node-set($newContent)/root/*)"/> elements</xsl:message><!--

	--><xsl:if test="str:split($debug.liste.apres, ',')[$currentPhase][. != '']"><!--
			--><xsl:variable name="nbElement" select="str:split($debug.liste.apres, ',')[$currentPhase]"/><!--
			--><xsl:for-each select="exsl:node-set($newContent)/root/*[position() &lt;= $nbElement]"><!--
				--><xsl:message>Apres - Element <xsl:value-of select="position()"/> : <xsl:value-of select="local-name(.)"/> = <xsl:copy-of select="."/></xsl:message><!--
		--></xsl:for-each><!--
	--></xsl:if><!--
	--><xsl:if test="str:split($debug.show.apres, ',')[$currentPhase][(. != '') and (. != '0')]"><!--
		--><xsl:copy-of select="exsl:node-set($newContent)"/><!--
	--></xsl:if><!--

	--><xsl:choose><!-- 
		--><xsl:when test="not($newContent) or (normalize-space($newContent) = '')"><!--
			--><xsl:copy-of select="$content/*"/><!--
		--></xsl:when><!--
		--><xsl:when test="($debug.limite.phase = '') or ($currentPhase &lt; $debug.limite.phase)"><!--
			--><xsl:call-template name="multiphase"><xsl:with-param name="content" select="exsl:node-set($newContent)/root"/><xsl:with-param name="currentPhase" select="$currentPhase+1"/></xsl:call-template><!--
		--></xsl:when><!--
		--><xsl:otherwise><!-- 
			--><xsl:copy-of select="exsl:node-set($newContent)/root"/><!--
		--></xsl:otherwise><!-- 
	--></xsl:choose><!-- 

 --></xsl:template>

	<xsl:template name="identifie"><!--
	--><xsl:param name="content"/><!-- 

	--><xsl:for-each select="//document/page/line/text"><!--
		--><xsl:sort select="ancestor::page/@no" data-type="number"/><!--
		--><xsl:sort select="format-number(ancestor::line/@ypos, '000.0')" data-type="number"/><!--
		--><xsl:sort select="@xpos" data-type="number"/><!--

				Recherche de l'entete de la page avec le titre des colonnes
			--><xsl:variable name="entete" select="dyn:evaluate($entete.xpath)"/><!--

				Position verticale minimale des lignes qui nous interessent, en dessous de l'entete
			--><xsl:variable name="ypos.min" select="$entete/@ypos"/><!--
				Position verticale maximale des lignes qui nous interessent, au dessus des notes
			--><xsl:variable name="ypos.max" select="ancestor::page//text[. = 'NOTES'][1]/@ypos"/><!--

			--><xsl:variable name="coursid.xpos" select="dyn:evaluate($entete.coursid.xpath)/@xpos"/><!--
			--><xsl:variable name="groupe.xpos" select="dyn:evaluate($entete.groupe.xpath)/@xpos"/><!--
			--><xsl:variable name="jour.xpos" select="dyn:evaluate($entete.jour.xpath)/@xpos"/><!--
			--><xsl:variable name="heure.xpos" select="dyn:evaluate($entete.heure.xpath)/@xpos"/><!--
			--><xsl:variable name="activite.xpos" select="dyn:evaluate($entete.activite.xpath)/@xpos"/><!--
			--><xsl:variable name="local.xpos" select="dyn:evaluate($entete.local.xpath)/@xpos"/><!--
			--><xsl:variable name="prog.xpos" select="dyn:evaluate($entete.prog.xpath)/@xpos"/><!--
			--><xsl:variable name="prealables.xpos" select="dyn:evaluate($entete.prealables.xpath)/@xpos"/><!--

			--><xsl:if test="count(ancestor::line/preceding-sibling::*) = 0"><!--
				--><xsl:message>Page <xsl:value-of select="ancestor::page/@no"/> - ypos.min = <xsl:value-of select="$ypos.min"/> - ypos.max = <xsl:value-of select="$ypos.max"/></xsl:message><!--
				--><xsl:message>Page <xsl:value-of select="ancestor::page/@no"/> - Cours : xpos = <xsl:value-of select="$coursid.xpos"/> - delta = <xsl:value-of select="$coursid.xpos.delta"/></xsl:message><!--
				--><xsl:message>Page <xsl:value-of select="ancestor::page/@no"/> - Groupe : xpos = <xsl:value-of select="$groupe.xpos"/> - delta = <xsl:value-of select="$groupe.xpos.delta"/></xsl:message><!--
				--><xsl:message>Page <xsl:value-of select="ancestor::page/@no"/> - Jour : xpos = <xsl:value-of select="$jour.xpos"/> - delta = <xsl:value-of select="$jour.xpos.delta"/></xsl:message><!--
				--><xsl:message>Page <xsl:value-of select="ancestor::page/@no"/> - Heure : xpos = <xsl:value-of select="$heure.xpos"/> - delta = <xsl:value-of select="$heure.xpos.delta"/></xsl:message><!--
				--><xsl:message>Page <xsl:value-of select="ancestor::page/@no"/> - Activite : xpos = <xsl:value-of select="$activite.xpos"/> - delta = <xsl:value-of select="$activite.xpos.delta"/></xsl:message><!--
				--><xsl:message>Page <xsl:value-of select="ancestor::page/@no"/> - Local : xpos = <xsl:value-of select="$local.xpos"/> - delta = <xsl:value-of select="$local.xpos.delta"/></xsl:message><!--
				--><xsl:message>Page <xsl:value-of select="ancestor::page/@no"/> - Programme : xpos = <xsl:value-of select="$prog.xpos"/> - delta = <xsl:value-of select="$prog.xpos.delta"/></xsl:message><!--
				--><xsl:message>Page <xsl:value-of select="ancestor::page/@no"/> - Préalables : xpos = <xsl:value-of select="$prealables.xpos"/> - delta = <xsl:value-of select="$prealables.xpos.delta"/></xsl:message><!--
			--></xsl:if><!--

			--><xsl:choose><!--
				On ignore les textes au dessus ou au meme niveau que l'entete
				--><xsl:when test="not($ypos.min) or (number(translate(($ypos.min - @ypos),'-','')) &lt; $ypos.delta)"></xsl:when><!--
				--><xsl:when test="not($ypos.min) or (@ypos &lt; $ypos.min)"></xsl:when><!--

				On ignore les textes au dessous ou au meme niveau que le page de page
				--><xsl:when test="$ypos.max and (number(translate(($ypos.max - @ypos),'-','')) &lt; $ypos.delta)"></xsl:when><!--
				--><xsl:when test="$ypos.max and (@ypos &gt; $ypos.max)"></xsl:when><!--
				
				Les textes positionnées en x avec l'entete COURS
				--><xsl:when test="number(translate(($coursid.xpos - @xpos),'-','')) &lt;= $coursid.xpos.delta"><!--
				--><coursid><xsl:value-of select="normalize-space(.)"/></coursid><!--
				--></xsl:when><!--
				
				Les textes positionnées en x avec l'entete GROUPE avec une longueur inferieur a 4
				--><xsl:when test="(number(translate(($groupe.xpos - @xpos),'-','')) &lt;= $groupe.xpos.delta) and (string-length(.) &lt; 4)"><!--
				--><groupeid><xsl:value-of select="normalize-space(.)"/></groupeid><!--
				--></xsl:when><!--
				
				Les textes positionnées en x avec l'entete GROUPE et non traite precedemment
				--><xsl:when test="(number(translate(($groupe.xpos - @xpos),'-','')) &lt;= $groupe.xpos.delta) and (number(translate(($groupe.xpos - @xpos),'-','')) &lt;= $groupe.xpos.delta) and not(starts-with(normalize-space(.), 'Volet')) and not(starts-with(normalize-space(.), 'Doit ')) and not(contains(normalize-space(.), 'Prendre note'))"><!--
				--><description><xsl:value-of select="normalize-space(.)"/></description><!--
				--></xsl:when><!--
				
				Les textes positionnées en x avec l'entete JOUR
				--><xsl:when test="number(translate(($jour.xpos - @xpos),'-','')) &lt;= $jour.xpos.delta"><!--
				--><jour><!--
				--><xsl:choose><!--
					--><xsl:when test="normalize-space(.) = 'Sam'"><!--
				--><xsl:value-of select="0"/><!--
				 --></xsl:when><!--
					--><xsl:when test="normalize-space(.) = 'Dim'"><!--
				--><xsl:value-of select="1"/><!--
				 --></xsl:when><!--
					--><xsl:when test="normalize-space(.) = 'Lun'"><!--
				--><xsl:value-of select="2"/><!--
				 --></xsl:when><!--
					--><xsl:when test="normalize-space(.) = 'Mar'"><!--
				--><xsl:value-of select="3"/><!--
				 --></xsl:when><!--
					--><xsl:when test="normalize-space(.) = 'Mer'"><!--
				--><xsl:value-of select="4"/><!--
				 --></xsl:when><!--
					--><xsl:when test="normalize-space(.) = 'Jeu'"><!--
				--><xsl:value-of select="5"/><!--
				 --></xsl:when><!--
					--><xsl:when test="normalize-space(.) = 'Ven'"><!--
				--><xsl:value-of select="6"/><!--
				 --></xsl:when><!--
				--></xsl:choose><!--
				--></jour><!--
				--></xsl:when><!--
				
				Les textes positionnées en x avec l'entete HEURE
				--><xsl:when test="number(translate(($heure.xpos - @xpos),'-','')) &lt;= $heure.xpos.delta"><!--
				--><heureDebut><xsl:value-of select="normalize-space(substring-before(., ' - '))"/></heureDebut><heureFin><xsl:value-of select="normalize-space(substring-after(., ' - '))"/></heureFin><!--
				--></xsl:when><!--
				
				Les textes positionnées en x avec l'entete ACTIVITÉ
				--><xsl:when test="number(translate(($activite.xpos - @xpos),'-','')) &lt;= $activite.xpos.delta"><!--
				--><activite><!--
				--><xsl:choose><!--
					--><xsl:when test="normalize-space(.) = 'C'"><!--
				--><xsl:text>Cours</xsl:text><!--
				 --></xsl:when><!--
					--><xsl:when test="starts-with(normalize-space(.), 'TP')"><!--
				--><xsl:text>TP</xsl:text><!--
				 --></xsl:when><!--
					--><xsl:when test="starts-with(normalize-space(.), 'Lab')"><!--
				--><xsl:text>Lab</xsl:text><!--
				 --></xsl:when><!--
					--><xsl:when test="starts-with(normalize-space(.), 'P')"><!--
				--><xsl:text>Projet</xsl:text><!--
				 --></xsl:when><!--
				--></xsl:choose><!--
				--></activite><!--
				--></xsl:when><!--

				Les textes positionnées en x avec l'entete LOCAL
				--><xsl:when test="number(translate(($local.xpos - @xpos),'-','')) &lt;= $local.xpos.delta"><!--
				--><locaux><!--
					--><xsl:for-each select="str:split(normalize-space(.), ',')"><!--
						Si le contenu est vide, on l'ignore
						--><xsl:if test="normalize-space(.) != ''"><!--
							--><string><xsl:value-of select="normalize-space(.)"/></string><!--
						--></xsl:if><!--
					--></xsl:for-each><!--
				--></locaux><!--
				--></xsl:when><!--

				Les textes positionnées en x avec l'entete PROGRAMME
				--><xsl:when test="number(translate(($prog.xpos - @xpos),'-','')) &lt;= $prog.xpos.delta"><!--
				--><programme><xsl:value-of select="normalize-space(.)"/></programme><!--
				--></xsl:when><!--

				Les textes positionnées en x avec l'entete PREALABLES
				--><xsl:when test="false and (number(translate(($prealables.xpos - @xpos),'-','')) &lt;= $prealables.xpos.delta) and not(starts-with(normalize-space(.), 'Début le'))"><!--
				--><prealables><!--
						Si le contenu est vide, on l'ignore
					--><xsl:for-each select="str:split(normalize-space(.), ',')"><!--
						--><xsl:if test="(normalize-space(.) != '')"><!--
							--><prealable><xsl:value-of select="normalize-space(.)"/></prealable><!--
						--></xsl:if><!--
					--></xsl:for-each><!--
				--></prealables><!--
				--></xsl:when><!--
				
				--><xsl:otherwise><!--
					--><xsl:copy-of select="."/><!--
				--></xsl:otherwise><!--
			--></xsl:choose><!--
		
	--></xsl:for-each><!--
 --></xsl:template>

	<xsl:template name="filtre"><!--
	--><xsl:param name="content"/><!-- 

	--><xsl:for-each select="$content/*"><!--

			--><xsl:choose><!--
				--><!--<xsl:when test="text() = ''"></xsl:when>--><!--
				--><xsl:when test="./self::text"></xsl:when><!--
				--><xsl:otherwise><!--
					--><xsl:copy-of select="."/><!--
				--></xsl:otherwise><!--
			--></xsl:choose><!--
		
	--></xsl:for-each><!--
 --></xsl:template>

	<xsl:template name="periodeCours"><!--
	--><xsl:param name="content"/><!-- 

	--><xsl:for-each select="$content/*"><!--

			--><xsl:choose><!--
			--><xsl:when test="self::jour"><!--
				--><xsl:variable name="nextNotInside" select="following-sibling::*[not(./self::heureDebut) and not(./self::heureFin) and not(./self::activite) and not(./self::locaux) and not(./self::description) and not(./self::prealables)][1]"/><!--

				--><xsl:variable name="currentPos" select="count(preceding-sibling::*)"/><!--
				--><xsl:variable name="maxDelta"><!--
					--><xsl:choose><!--
						--><xsl:when test="not($nextNotInside)"><xsl:value-of select="count(following-sibling::*)"/></xsl:when><!--
						--><xsl:otherwise><xsl:value-of select="count($nextNotInside/preceding-sibling::*) - $currentPos"/></xsl:otherwise><!--
					--></xsl:choose><!--
				--></xsl:variable><!--
				--><!--<xsl:message>Current : <xsl:value-of select="local-name()"/> - <xsl:value-of select="$currentPos"/> / Next <xsl:value-of select="local-name($nextNotInside)"/> - <xsl:value-of select="$nextPos"/> / maxDelta = <xsl:value-of select="$maxDelta"/>.</xsl:message>--><!--

				--><periode><!--
					--><xsl:copy-of select="."/><!--
					--><groupe><xsl:attribute name="reference"><xsl:text>../..</xsl:text></xsl:attribute></groupe><!--
					--><xsl:copy-of select="following-sibling::*[position() &lt; $maxDelta][./self::heureDebut]"/><!--
					--><xsl:copy-of select="following-sibling::*[position() &lt; $maxDelta][./self::heureFin]"/><!--
					--><xsl:copy-of select="following-sibling::*[position() &lt; $maxDelta][./self::activite]"/><!--
					--><xsl:copy-of select="following-sibling::*[position() &lt; $maxDelta][./self::locaux]"/><!--
				--></periode><!--
			--></xsl:when><!--
			--><xsl:when test="self::heureDebut|self::heureFin|self::activite|self::locaux"></xsl:when><!--
			--><xsl:otherwise><xsl:copy-of select="."/></xsl:otherwise><!--
			--></xsl:choose><!--
		
	--></xsl:for-each><!--
 --></xsl:template>

	<xsl:template name="groupe"><!--
	--><xsl:param name="content"/><!-- 

	--><xsl:for-each select="$content/*"><!--

			--><xsl:choose><!--
			--><xsl:when test="self::groupeid"><!--
				--><xsl:variable name="nextNotInside" select="following-sibling::*[not(./self::periode) and not(./self::description) and not(./self::prealables)][1]"/><!--

				--><xsl:variable name="currentPos" select="count(preceding-sibling::*)"/><!--
				--><xsl:variable name="maxDelta"><!--
					--><xsl:choose><!--
						--><xsl:when test="not($nextNotInside)"><xsl:value-of select="count(following-sibling::*)"/></xsl:when><!--
						--><xsl:otherwise><xsl:value-of select="count($nextNotInside/preceding-sibling::*) - $currentPos"/></xsl:otherwise><!--
					--></xsl:choose><!--
				--></xsl:variable><!--
				--><!--<xsl:message>Current : <xsl:value-of select="local-name()"/> - <xsl:value-of select="$currentPos"/> / Next <xsl:value-of select="local-name($nextNotInside)"/> - <xsl:value-of select="$nextPos"/> / maxDelta = <xsl:value-of select="$maxDelta"/>.</xsl:message>--><!--

				--><groupe><!--
					--><xsl:attribute name="id"><xsl:value-of select="."/></xsl:attribute><!--
					--><cours><xsl:attribute name="reference"><xsl:text>../..</xsl:text></xsl:attribute></cours><!--
					--><xsl:copy-of select="following-sibling::*[position() &lt; $maxDelta][./self::periode]"/><!--
				--></groupe><!--
			--></xsl:when><!--
			--><xsl:when test="self::periode"></xsl:when><!--
			--><xsl:otherwise><xsl:copy-of select="."/></xsl:otherwise><!--
			--></xsl:choose><!--
		
	--></xsl:for-each><!--
 --></xsl:template>

	<xsl:template name="cours"><!--
	--><xsl:param name="content"/><!-- 

	--><listeCours><!--
	--><xsl:for-each select="$content/*"><!--

			--><xsl:choose><!--
			--><xsl:when test="self::coursid"><!--
				--><xsl:variable name="nextNotInside" select="following-sibling::*[not(./self::programme) and not(./self::description) and not(./self::prealables) and not(./self::groupe)][1]"/><!--

				--><xsl:variable name="currentPos" select="count(preceding-sibling::*)"/><!--
				--><xsl:variable name="maxDelta"><!--
					--><xsl:choose><!--
						--><xsl:when test="not($nextNotInside)"><xsl:value-of select="count(following-sibling::*)"/></xsl:when><!--
						--><xsl:otherwise><xsl:value-of select="count($nextNotInside/preceding-sibling::*) - $currentPos"/></xsl:otherwise><!--
					--></xsl:choose><!--
				--></xsl:variable><!--
				--><!--<xsl:message>Current : <xsl:value-of select="local-name()"/> - <xsl:value-of select="$currentPos"/> / Next <xsl:value-of select="local-name($nextNotInside)"/> - <xsl:value-of select="$nextPos"/> / maxDelta = <xsl:value-of select="$maxDelta"/>.</xsl:message>--><!--

				--><entry><!--
				--><string><xsl:value-of select="."/></string><!--
				--><cours><!--
					--><xsl:attribute name="id"><xsl:value-of select="."/></xsl:attribute><!--
					--><xsl:copy-of select="following-sibling::*[position() &lt; $maxDelta][./self::programme]"/><!--
					--><xsl:copy-of select="following-sibling::*[position() &lt; $maxDelta][./self::description]"/><!--
					--><xsl:copy-of select="following-sibling::*[position() &lt; $maxDelta][./self::prealables]"/><!--
					--><xsl:copy-of select="following-sibling::*[position() &lt; $maxDelta][./self::groupe]"/><!--
				--></cours><!--
				--></entry><!--
			--></xsl:when><!--
			--><xsl:when test="self::programme or self::description or self::prealables or self::groupe"></xsl:when><!--
			--><xsl:otherwise><xsl:copy-of select="."/></xsl:otherwise><!--
			--></xsl:choose><!--
		
	--></xsl:for-each><!--
	--></listeCours><!--
 --></xsl:template>

	<xsl:template name="horaire"><!--
	--><xsl:param name="content"/><!-- 

	--><horaire><!--
	--><xsl:if test="//document/page[1]/line/text[contains(., 'Baccalauréat en')]"><!--
		--><titre><xsl:value-of select="//document/page[1]/line/text[contains(., 'Baccalauréat en')]"/></titre><!--
	--></xsl:if><!--
	--><xsl:if test="//document/page[1]/line/text[contains(., 'INSCRIPTION POUR')]"><!--
		--><session><xsl:value-of select="substring(string(//document/page[1]/line/text[contains(., 'INSCRIPTION POUR')]), 30, 99)"/></session><!--
	--></xsl:if><!--
	--><xsl:for-each select="$content/*"><!--
		--><xsl:copy-of select="."/><!--
	--></xsl:for-each><!--
	--></horaire><!--
 --></xsl:template>
</xsl:stylesheet>
