package org.seasar.cms.classbuilder.impl;

import static org.seasar.cms.classbuilder.impl.RedefinableXmlS2ContainerBuilder.DELIMITER;

import java.util.ArrayList;
import java.util.List;

import org.seasar.cms.classbuilder.util.S2ContainerBuilderUtils;
import org.seasar.framework.container.ArgDef;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.deployer.InstanceDefFactory;
import org.seasar.framework.container.factory.AnnotationHandler;
import org.seasar.framework.container.factory.AnnotationHandlerFactory;
import org.seasar.framework.container.factory.ComponentTagHandler;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.container.factory.TagAttributeNotDefinedRuntimeException;
import org.seasar.framework.util.StringUtil;
import org.seasar.framework.xml.TagHandlerContext;

public class RedefinableComponentTagHandler extends ComponentTagHandler {
    private static final long serialVersionUID = 2513809305883784501L;

    public void end(TagHandlerContext context, String body) {
        ComponentDef componentDef = (ComponentDef) context.pop();
        AnnotationHandler annoHandler = AnnotationHandlerFactory
                .getAnnotationHandler();
        annoHandler.appendInitMethod(componentDef);
        annoHandler.appendDestroyMethod(componentDef);
        annoHandler.appendAspect(componentDef);
        String expression = null;
        if (body != null) {
            expression = body.trim();
            if (!StringUtil.isEmpty(expression)) {
                componentDef.setExpression(expression);
            } else {
                expression = null;
            }
        }
        if (componentDef.getComponentClass() == null
                && !InstanceDefFactory.OUTER.equals(componentDef
                        .getInstanceDef()) && expression == null) {
            throw new TagAttributeNotDefinedRuntimeException("component",
                    "class");
        }
        if (context.peek() instanceof S2Container) {
            S2Container container = (S2Container) context.peek();
            if (componentDef.getComponentName() != null) {
                ComponentDef[] redefined = redefine(componentDef,
                        (String) context.getParameter("path"),
                        (RedefinableXmlS2ContainerBuilder) context
                                .getParameter("builder"));
                for (int i = 0; i < redefined.length; i++) {
                    container.register(redefined[i]);
                }
            } else {
                container.register(componentDef);
            }
        } else {
            ArgDef argDef = (ArgDef) context.peek();
            argDef.setChildComponentDef(componentDef);
        }
    }

    ComponentDef[] redefine(ComponentDef componentDef, String path,
            RedefinableXmlS2ContainerBuilder builder) {
        int delimiter = path.lastIndexOf(DELIMITER);
        int slash = path.lastIndexOf('/');
        if (delimiter >= 0 && delimiter > slash) {
            // リソース名に「+」が含まれている場合は特別な処理を行なわない。
            return new ComponentDef[] { componentDef };
        }

        String name = componentDef.getComponentName();
        String[] diconPaths = constructRedifinitionDiconPaths(path, name);
        String diconPath = null;
        for (int i = 0; i < diconPaths.length; i++) {
            if (S2ContainerBuilderUtils.resourceExists(diconPaths[i], builder)) {
                diconPath = diconPaths[i];
                break;
            }
        }
        if (diconPath == null) {
            return new ComponentDef[] { componentDef };
        }

        S2Container container = S2ContainerFactory.create(diconPath);
        int size = container.getComponentDefSize();
        ComponentDef[] redefined = new ComponentDef[size];
        for (int i = 0; i < size; i++) {
            redefined[i] = container.getComponentDef(i);
            redefined[i].setContainer(componentDef.getContainer());
        }

        return redefined;
    }

    protected String[] constructRedifinitionDiconPaths(String path, String name) {
        List<String> pathList = new ArrayList<String>();
        String body;
        String suffix;
        int dot = path.lastIndexOf('.');
        if (dot < 0) {
            body = path;
            suffix = "";
        } else {
            body = path.substring(0, dot);
            suffix = path.substring(dot);
        }
        String resourcePath = S2ContainerBuilderUtils
                .fromURLToResourcePath(body + DELIMITER + name + suffix);
        if (resourcePath != null) {
            // パスがJarのURLの場合はURLをリソースパスに変換した上で作成したパスを候補に含める。
            pathList.add(resourcePath);
        }
        pathList.add(body + DELIMITER + name + suffix);
        return pathList.toArray(new String[0]);
    }
}