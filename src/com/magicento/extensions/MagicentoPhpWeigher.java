package com.magicento.extensions;

import com.intellij.codeInsight.completion.CompletionLocation;
import com.intellij.codeInsight.completion.CompletionWeigher;
import com.intellij.codeInsight.lookup.LookupElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author Enrique Piatti
 */
public class MagicentoPhpWeigher extends CompletionWeigher {

    @Override
    public Integer weigh(@NotNull LookupElement element, @NotNull CompletionLocation location)
    {
        if (location == null) {
            return null;
        }
//        if( ! location.getCompletionParameters().getOriginalFile().getVirtualFile().getPath().endsWith("config.xml")){
//            return null;
//        }
        // element.getLookupString()
        // location.getCompletionParameters().myPosition.getPrevSibling().getPrevSibling()
        Object object = element.getObject();    // we can pass any object inside the LookupElement creator in our MagicentoXmlCompletionContributor
        if(object instanceof String){
            String reference = (String) object;
            if(reference.startsWith("magicento")){
                return -100;
            }
        }
        return null;
        //return element.getLookupString().toLowerCase();
    }

}
