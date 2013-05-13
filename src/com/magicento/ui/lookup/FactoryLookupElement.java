package com.magicento.ui.lookup;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.magicento.MagicentoIcons;
import org.jetbrains.annotations.NotNull;

public class FactoryLookupElement extends LookupElement {

    private String uri;
    private String className;

    public FactoryLookupElement(String serviceId, String className) {
        this.uri = serviceId;
        this.className = className;
    }

    @NotNull
    @Override
    public String getLookupString() {
        return uri;
    }

    public void renderElement(LookupElementPresentation presentation) {
        presentation.setItemText(getLookupString());
        presentation.setTypeText(className);
        presentation.setTypeGrayed(true);
        presentation.setIcon(MagicentoIcons.MAGENTO_ICON_16x16);
    }
}
