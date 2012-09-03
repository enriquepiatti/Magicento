package com.magicento.models.xml.config;

import com.magicento.MagicentoProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlTag;
import com.magicento.models.MagentoClassInfo;
import org.apache.commons.lang.WordUtils;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class GlobalModelsIdRewriteIdXmlTag extends IdXmlTag {

    public GlobalModelsIdRewriteIdXmlTag(){
        super();
        //name = "class";
        help = "Second part of the URI of the model to rewrite";
    }

    @Override
    public List<String> getPossibleNames() {

        Project project = getProject();
        if(project != null){
            XmlTag modelId = getNodeFromContextHierarchy("config/global/models/*");
            if(modelId != null){
                String uriFirstPart = modelId.getName();
                List<Element> modelUriClassTag = getAllNodesFromMergedXml("config/global/models/"+uriFirstPart+"/class");
                if(modelUriClassTag != null && modelUriClassTag.size()>0){
                    String baseClassName = modelUriClassTag.get(0).getValue();
                    if(baseClassName != null){
                        int length = baseClassName.length();
                        List<String> models = MagentoClassInfo.getNames(MagicentoProjectComponent.getInstance(project).findModelsOfFactoryUri(uriFirstPart+"/*"));
                        if(models != null && models.size()>0){
                            for(String model : models){
                                if(model.startsWith(baseClassName)){
                                    String secondPart = model.substring(length + 1);
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
