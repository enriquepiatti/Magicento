package com.magicento.actions;

import com.magicento.helpers.MagentoParser;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * @author Enrique Piatti
 * TODO: remove this action?
 */
public class GotoMagentoClassAction extends MagicentoActionAbstract {

    private String _className;

    @Override
    public Boolean isApplicable(AnActionEvent e)
    {
        setEvent(e);
        String factory = MagentoParser.getFactory(getCode(), getCursorOffset());
        if(factory == null)
            return false;
        return true;
    }

    public void actionPerformed(AnActionEvent e)
    {
        setEvent(e);
        String className = getMagentoClassName();
        gotoClass(className);
    }

    public void setMagentoClassName(String name)
    {
        _className = name;
    }

    private String getMagentoClassName()
    {
        _className = getMagicentoComponent().getClassNameFromFactory( MagentoParser.getFactory(getCode(), getCursorOffset()) );
        return _className;
    }

}
