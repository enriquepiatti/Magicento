package com.magicento.extensions;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.magicento.models.psi.MagentoXmlAttributeReferenceProvider;

// import com.jetbrains.php.lang.patterns.PhpPatterns;

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
