package com.magicento.extensions;

import com.intellij.codeInsight.daemon.impl.analysis.encoding.XmlEncodingReferenceProvider;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.filters.*;
import com.intellij.psi.filters.position.NamespaceFilter;
import com.intellij.psi.filters.position.ParentElementFilter;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.DtdReferencesProvider;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.IdReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.SchemaReferencesProvider;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.URIReferenceProvider;
import com.intellij.psi.xml.*;
import com.intellij.xml.util.XmlPrefixReferenceProvider;
import com.intellij.xml.util.XmlUtil;
import com.jetbrains.php.lang.patterns.PhpPatterns;
import com.magicento.models.psi.MagentoXmlAttributeReferenceProvider;

import static com.intellij.patterns.StandardPatterns.string;
import static com.intellij.patterns.XmlPatterns.xmlAttribute;
import static com.intellij.patterns.XmlPatterns.xmlAttributeValue;
import static com.intellij.patterns.XmlPatterns.xmlTag;

/**
 * @author Enrique Piatti
 */
public class MagicentoReferenceContributor extends PsiReferenceContributor
{
    public void registerReferenceProviders(final PsiReferenceRegistrar registrar)
    {

        registrar.registerReferenceProvider(PlatformPatterns.psiElement(), new MagentoXmlAttributeReferenceProvider());
//        registrar.registerReferenceProvider(com.jetbrains.php.lang.patterns.PhpPatterns.phpElement(), new MagentoXmlAttributeReferenceProvider());
//        registrar.registerReferenceProvider(com.jetbrains.php.lang.patterns.PhpPatterns.phpFunctionReference(), new MagentoXmlAttributeReferenceProvider());
//        registrar.registerReferenceProvider(com.jetbrains.php.lang.patterns.PhpPatterns.phpLiteralExpression(), new MagentoXmlAttributeReferenceProvider());

    }
}
