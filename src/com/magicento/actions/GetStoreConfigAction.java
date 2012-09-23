package com.magicento.actions;

import com.magicento.MagicentoSettings;
import com.magicento.helpers.IdeHelper;
import com.magicento.helpers.MagentoParser;
import com.magicento.helpers.PsiPhpHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiElement;

/**
 * @author Enrique Piatti
 */
public class GetStoreConfigAction extends MagicentoPhpActionAbstract {

    private String _getStoreConfig;

    @Override
    public Boolean isApplicable(AnActionEvent e) {
        setEvent(e);
        _getStoreConfig = null;
        return getStoreConfig() != null;
    }

    public void executeAction()
    {
        // TODO: add support for multiple stores
        String php = "echo "+getStoreConfig()+";"; //"var_dump("+getStoreConfig()+");";
        String output = getMagicentoComponent().executePhpWithMagento(php);
        IdeHelper.showDialog(output, "getStoreConfig");
    }

    public String getStoreConfig()
    {
        PsiElement psiElement = getPsiElementAtCursor();
        if(psiElement == null)
            return null;
        PsiElement methodReference = PsiPhpHelper.isMethodRefence(psiElement) ?
                psiElement : PsiPhpHelper.findFirstParentOfType(psiElement, PsiPhpHelper.METHOD_REFERENCE, PsiPhpHelper.STATEMENT );
        if(methodReference != null){
            // TODO: use PSI here instead of text, allow getStoreCongi with constants and string concatenations inside
            // TODO: allow self::getStoreConfig too
            // TODO: allow getStoreConfig with store parameter, this fails right now:
            // $separator = (string)Mage::getStoreConfig('catalog/seo/title_separator', $store);    // Mage_Catalog_Block_Breadcrumbs linea 44
            _getStoreConfig = MagentoParser.getStoreConfig(methodReference.getText(), 0);
        }
        return _getStoreConfig;
    }

}
