package org.seasar.cms.classbuilder.util;

import org.seasar.cms.classbuilder.S2ContainerPreparer;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;


public class S2ContainerPreparerUtils
{
    protected S2ContainerPreparerUtils()
    {
    }


    public static String toComponentName(String name)
    {
        if (name.length() > 0) {
            // FIXME 正しいルールに置き換えよう。
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
        return name;
    }


    public static S2ContainerPreparer getPreparer(ComponentDef componentDef)
    {
        S2Container container = componentDef.getContainer();
        ComponentDef[] componentDefs = container
            .findLocalComponentDefs(S2ContainerPreparer.class);
        if (componentDefs.length == 0) {
            return null;
        }

        return (S2ContainerPreparer)componentDefs[0].getComponent();
    }
}
