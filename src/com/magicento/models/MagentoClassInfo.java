package com.magicento.models;

import com.intellij.openapi.util.text.StringUtil;
import com.magicento.helpers.MagentoParser;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Enrique Piatti
 */
public class MagentoClassInfo {

    public enum UriType
    {
        MODEL, RESOURCEMODEL, HELPER, BLOCK;
    }

    public enum ClassType {
        MODEL, RESOURCEMODEL, COLLECTION, HELPER, BLOCK, CONTROLLER, INSTALLER;
    }

    public static UriType getUriTypeFromClassType(ClassType classType)
    {
        UriType uriType = null;
        if(classType != null){
            switch (classType){
                case BLOCK:
                    uriType = UriType.BLOCK;
                    break;
                case HELPER:
                    uriType = UriType.HELPER;
                    break;
                case MODEL:
                    uriType = UriType.MODEL;
                    break;
                case RESOURCEMODEL:
                case COLLECTION:
                    uriType = UriType.RESOURCEMODEL;
                    break;
            }
        }
        return uriType;
    }

    protected String module;
    public String name;
    public String uriFirstPart;
    public String uriSecondPart;
    protected UriType type;
    public boolean isRewrite = false;

    public void setType(UriType type){
        this.type = type;
    }

    public void setType(String type){
        if(type != null){
            type = type.toLowerCase();
            if(type.startsWith("model")){
                this.type = UriType.MODEL;
            }
            else if(type.startsWith("resource")){
                this.type = UriType.RESOURCEMODEL;
            }
            else if(type.startsWith("helper")){
                this.type = UriType.HELPER;
            }
            else if(type.startsWith("block")){
                this.type = UriType.BLOCK;
            }
        }
    }


    public UriType getType()
    {
        if(type == null){
            if(name != null){
                List<String> nameParts = StringUtil.split(name, "_");
                if(nameParts.size() > 2){
                    setType(nameParts.get(2));
                }
            }
        }
        return type;
    }

    public String getUri()
    {
        if(uriFirstPart != null)
        {
            // if it's a rewrite we can't assume the second part uri is the same as the new rewritten class name
            if((uriSecondPart == null || uriSecondPart.isEmpty()) && ! isRewrite)
            {
                uriSecondPart = MagentoParser.getSecondPartUriFromClassName(name, MagentoParser.getClassPrefix(name, getType()==UriType.RESOURCEMODEL));
            }
            if(uriSecondPart != null && ! uriSecondPart.isEmpty()){
                return uriFirstPart+"/"+uriSecondPart;
            }
        }
        return null;
    }

    public String getModule()
    {
        if(module == null){
            if(name != null){
                List<String> nameParts = StringUtil.split(name, "_");
                if(nameParts.size()>1){
                    module = nameParts.get(0)+"_"+nameParts.get(1);
                }
            }
        }
        return module;
    }

    public void setModule(String module)
    {
        this.module = module;
    }


    public static List<String> getNames(List<MagentoClassInfo> list)
    {
        List<String> names = new ArrayList<String>();
        if(list != null){
            for(MagentoClassInfo classInfo : list){
                names.add(classInfo.name);
            }
        }
        return names;
    }

    public MagentoClassInfo clone()
    {
        MagentoClassInfo newInstance = new MagentoClassInfo();
        newInstance.name = name;
        newInstance.isRewrite = isRewrite;
        newInstance.module = module;
        newInstance.type = type;
        newInstance.uriFirstPart = uriFirstPart;
        newInstance.uriSecondPart = uriSecondPart;
        return newInstance;
    }

}
