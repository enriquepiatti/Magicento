package com.magicento.models.psi;

import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.*;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magicento.MagicentoProjectComponent;
import com.magicento.models.MagentoClassInfo;
import com.magicento.ui.lookup.FactoryLookupElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * To enable this uncomment the extension point:
 * @author Enrique Piatti <contacto@enriquepiatti.com>
 */
public class FactoryReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {

    private String uri;

    public FactoryReference(@NotNull StringLiteralExpression element)
    {
        super(element);

        uri = element.getText().substring(
                element.getValueRange().getStartOffset(),
                element.getValueRange().getEndOffset()
        ); // Remove quotes
        // ugly hack
        uri = uri.replace("IntellijIdeaRulezzz", "").trim();
    }


    protected MagicentoProjectComponent getMagicentoProjectComponent()
    {
        return getElement().getProject().getComponent(MagicentoProjectComponent.class);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode)
    {
        // Return the PsiElement for the class corresponding to the uri
        MagicentoProjectComponent magicentoProjectComponent = getMagicentoProjectComponent();
        if (null == magicentoProjectComponent || magicentoProjectComponent.isDisabled()) {
            return new ResolveResult[]{};
        }


        List<MagentoClassInfo> classes = magicentoProjectComponent.findClassesOfFactoryUri(uri);

        if (null == classes) {
            return new ResolveResult[]{};
        }

        PhpIndex phpIndex = PhpIndex.getInstance(getElement().getProject());
        Collection<PhpClass> phpClasses = new ArrayList<PhpClass>();
        // Collection<PhpClass> phpInterfaces = phpIndex.getInterfacesByFQN(magentoClassInfo.name);
        for(MagentoClassInfo magentoClassInfo : classes){
            phpClasses.addAll(phpIndex.getClassesByFQN(magentoClassInfo.name));

        }

        List<ResolveResult> results = new ArrayList<ResolveResult>();
        for (PhpClass phpClass : phpClasses) {
            results.add(new PsiElementResolveResult(phpClass));
        }

        return results.toArray(new ResolveResult[results.size()]);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);

        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    @NotNull
    @Override
    /**
     * @todo differentiate factories (helper, getModel, etc)
     *
     */
    public Object[] getVariants()
    {
        List<LookupElement> results = new ArrayList<LookupElement>();

        MagicentoProjectComponent magicentoProjectComponent = getMagicentoProjectComponent();
        if (null != magicentoProjectComponent && magicentoProjectComponent.isEnabled())
        {
            PhpIndex phpIndex = PhpIndex.getInstance(getElement().getProject());


            List<MagentoClassInfo> classes = magicentoProjectComponent.findClassesOfFactoryUri(uri+"*");
            if (null != classes)
            {
                for(MagentoClassInfo magentoClassInfo : classes)
                {
                    String uri = magentoClassInfo.getUri();
                    String clasName = magentoClassInfo.name;
                    Collection<PhpClass> phpClasses = phpIndex.getClassesByFQN(clasName);
                    if (phpClasses.size() > 0) {
                        results.add(new FactoryLookupElement(uri, clasName));
                    }
//                    else {
//                        Collection<PhpClass> phpInterfaces = phpIndex.getInterfacesByFQN(clasName);
//                        if (phpInterfaces.size() > 0) {
//                            results.add(new FactoryLookupElement(uri, phpInterfaces.iterator().next()));
//                        }
//                    }
                }
            }
        }

        return results.toArray();
    }
}

