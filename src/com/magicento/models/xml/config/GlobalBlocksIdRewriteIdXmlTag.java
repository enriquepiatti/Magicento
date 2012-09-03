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
public class GlobalBlocksIdRewriteIdXmlTag extends IdXmlTag {

    public GlobalBlocksIdRewriteIdXmlTag(){

        super();
        //name = "class";
        help = "Second part of the URI of the block to rewrite";
    }

    @Override
    public List<String> getPossibleNames() {

        Project project = getProject();
        if(project != null){
            XmlTag blockId = getNodeFromContextHierarchy("config/global/blocks/*");
            if(blockId != null){
                String uriFirstPart = blockId.getName();
                List<Element> blockUriClassTag = getAllNodesFromMergedXml("config/global/blocks/"+uriFirstPart+"/class");
                if(blockUriClassTag != null && blockUriClassTag.size()>0){
                    String baseClassName = blockUriClassTag.get(0).getValue();
                    if(baseClassName != null){
                        int length = baseClassName.length();
                        List<String> blocks = MagentoClassInfo.getNames(MagicentoProjectComponent.getInstance(project).findBlocksOfFactoryUri(uriFirstPart + "/*"));
                        if(blocks != null && blocks.size()>0){
                            for(String block : blocks){
                                if(block.startsWith(baseClassName)){
                                    String secondPart = block.substring(length + 1);
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
