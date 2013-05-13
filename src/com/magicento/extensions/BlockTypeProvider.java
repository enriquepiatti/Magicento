package com.magicento.extensions;

import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.parser.PhpElementTypes;
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

/**
 * @author Enrique Piatti
 */
public class BlockTypeProvider implements PhpTypeProvider {

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

        if( ! magicentoProjectComponent.getMagicentoSettings().automaticThisInTemplate){
            return null;
        }

        // e.isVariable && e.getText.equals("$this");
        // filter out method calls without parameter
        if(!PlatformPatterns.psiElement(PhpElementTypes.VARIABLE).withName("this").accepts(e)) {
            return null;
        }

        if( ! Magicento.isInsideTemplateFile(e)){
            return null;
        }

        final VirtualFile file = e.getContainingFile().getOriginalFile().getVirtualFile();
        Template template = new Template(file);
        List<String> blocks = template.getBlocksClasses(e.getProject());
        if(blocks != null)
        {
            return new PhpType().add(blocks.get(0));
        }

        return null;

    }


}
