package com.magicento.models.xml.config;

import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlTag;
import com.magicento.MagicentoProjectComponent;
import com.magicento.models.MagentoClassInfo;
import org.apache.commons.lang.WordUtils;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class GlobalHelpersIdRewriteIdXmlTag extends IdXmlTag {

    public GlobalHelpersIdRewriteIdXmlTag(){

        super();
        //name = "class";
        help = "Second part of the URI of the helper to rewrite";
    }

    @Override
    public List<String> getPossibleNames() {

        Project project = getProject();
        if(project != null){
            XmlTag helperId = getNodeFromContextHierarchy("config/global/helpers/*");
            if(helperId != null){
                String uriFirstPart = helperId.getName();
                List<Element> helperUriClassTag = getAllNodesFromMergedXml("config/global/helpers/"+uriFirstPart+"/class");
                if(helperUriClassTag != null && helperUriClassTag.size()>0){
                    String baseClassName = helperUriClassTag.get(0).getValue();
                    if(baseClassName != null){
                        int length = baseClassName.length();
                        List<String> helpers = MagentoClassInfo.getNames(MagicentoProjectComponent.getInstance(project).findHelpersOfFactoryUri(uriFirstPart + "/*"));
                        if(helpers != null && helpers.size()>0){
                            for(String helper : helpers){
                                if(helper.startsWith(baseClassName)){
                                    String secondPart = helper.substring(length + 1);
                                    String secondPartUri = WordUtils.uncapitalize(secondPart.replace('_', ' ')).replace(' ', '_');
                                    names.add(secondPartUri);
                                }
                            }
                        }
                    }
                }

            }
        }

        return new ArrayList<String>(names);
    }


}
