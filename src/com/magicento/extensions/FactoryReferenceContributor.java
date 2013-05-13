package com.magicento.extensions;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.magicento.MagicentoProjectComponent;
import com.magicento.helpers.MagentoParser;
import com.magicento.models.psi.FactoryReference;
import org.jetbrains.annotations.NotNull;

// import com.jetbrains.php.lang.patterns.PhpPatterns;

/**
 * @author Enrique Piatti
 */
public class FactoryReferenceContributor extends PsiReferenceContributor
{
    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar psiReferenceRegistrar)
    {
        psiReferenceRegistrar.registerReferenceProvider(
                PlatformPatterns.psiElement(StringLiteralExpression.class).withLanguage(PhpLanguage.INSTANCE),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext)
                    {
                        if(MagicentoProjectComponent.getInstance(psiElement.getProject()).isDisabled()){
                            return new PsiReference[0];
                        }

                        // autocomplete for method parameters
                        if (!(psiElement.getContext() instanceof ParameterList)) {
                            return new PsiReference[0];
                        }
                        ParameterList parameterList = (ParameterList) psiElement.getContext();

                        if (!(parameterList.getContext() instanceof MethodReference)) {
                            return new PsiReference[0];
                        }
                        MethodReference method = (MethodReference) parameterList.getContext();

                        if ( ! MagentoParser.isFactory(method)) {
                            return new PsiReference[0];
                        }

                        return new PsiReference[]{ new FactoryReference((StringLiteralExpression) psiElement) };
                    }
                }
        );
    }
}
