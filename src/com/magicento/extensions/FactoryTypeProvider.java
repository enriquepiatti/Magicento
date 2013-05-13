package com.magicento.extensions;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider;
import com.magicento.MagicentoProjectComponent;
import com.magicento.helpers.IdeHelper;
import com.magicento.helpers.MagentoParser;
import com.magicento.helpers.Magicento;
import com.magicento.helpers.PsiPhpHelper;
import com.magicento.models.MagentoClassInfo;
import com.magicento.models.layout.Template;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FactoryTypeProvider implements PhpTypeProvider {

    @Nullable
    @Override
    public PhpType getType(PsiElement e)
    {
        if (DumbService.getInstance(e.getProject()).isDumb()) {
            return null;
        }

        if( ! IdeHelper.isPhpWithAutocompleteFeature()){
            return null;
        }

        MagicentoProjectComponent magicentoProjectComponent = MagicentoProjectComponent.getInstance(e.getProject());
        if(magicentoProjectComponent == null || magicentoProjectComponent.isDisabled()){
            return null;
        }

        // filter out method calls without parameter
        if(!PlatformPatterns.psiElement(PhpElementTypes.METHOD_REFERENCE).withChild(
                PlatformPatterns.psiElement(PhpElementTypes.PARAMETER_LIST).withFirstChild(
                        PlatformPatterns.psiElement(PhpElementTypes.STRING))).accepts(e)) {

            return null;
        }

        String className = getHelperClassNameFromTemplate(e);
        if(className == null)
        {
            // container calls are only on "get" methods
            // cant we move it up to PlatformPatterns? withName condition dont looks working
            // String methodRefName = ((MethodReference) e).getName();
            if( ! MagentoParser.isFactory(e)){
                return null;
            }

            String uri = MagentoParser.getUriFromFactory(e);
            if (null == uri) {
                return null;
            }

            className = magicentoProjectComponent.getClassNameFromFactory(e);
        }

        if (null == className) {
            return null;
        }

        return new PhpType().add(className);
    }


    /**
     * $this->helper('...') inside .phtml files
     * @return
     */
    protected String getHelperClassNameFromTemplate(PsiElement e)
    {
        if(Magicento.isInsideTemplateFile(e)){
            if(PsiPhpHelper.isMethodRefence(e)){
                String methodName = PsiPhpHelper.getMethodName(e);
                if(methodName != null && methodName.equals("helper")){
                    PsiElement parameterList = PsiPhpHelper.findFirstChildOfType(e, PsiPhpHelper.PARAMETER_LIST);
                    if(parameterList != null)
                    {
                        String value = parameterList.getText();
                        if(value != null){
                            String uri = value.replaceAll("\"", "").replaceAll("'", "");
                            if( ! uri.isEmpty()){
                                MagicentoProjectComponent magicentoProjectComponent = MagicentoProjectComponent.getInstance(e.getProject());
                                List<MagentoClassInfo> helpers = magicentoProjectComponent.findHelpersOfFactoryUri(uri);
                                if(helpers != null && helpers.size() > 0){
                                    return helpers.get(0).name;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

}

