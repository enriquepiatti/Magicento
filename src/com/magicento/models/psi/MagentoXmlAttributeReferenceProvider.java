package com.magicento.models.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Enrique Piatti
 */
public class MagentoXmlAttributeReferenceProvider extends PsiReferenceProvider {
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
//        XmlAttributeValue attributeValue = (XmlAttributeValue)element;
//        XmlTag root = ((XmlFile)attributeValue.getContainingFile()).getRootTag();
//        return new PsiReference[] {
//                new TagNameReference(root.getNode(), true)
//        };
        return new PsiReference[0];
    }
}
