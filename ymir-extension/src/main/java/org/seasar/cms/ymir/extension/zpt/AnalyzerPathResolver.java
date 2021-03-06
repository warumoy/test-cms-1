package org.seasar.cms.ymir.extension.zpt;

import java.beans.PropertyDescriptor;

import org.seasar.cms.ymir.extension.creator.ClassDesc;
import org.seasar.cms.ymir.extension.creator.PropertyDesc;
import org.seasar.cms.ymir.extension.creator.PropertyTypeHint;
import org.seasar.cms.ymir.extension.creator.TypeDesc;
import org.seasar.cms.ymir.extension.creator.impl.TypeDescImpl;

import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.freyja.zpt.tales.PathResolver;

public class AnalyzerPathResolver implements PathResolver {

    public boolean accept(TemplateContext context,
            VariableResolver varResolver, Object obj, String child) {
        return true;
    }

    public Object resolve(TemplateContext context,
            VariableResolver varResolver, Object obj, String child) {
        AnalyzerContext analyzerContext = (AnalyzerContext) context;
        if (obj instanceof DescWrapper) {
            DescWrapper wrapper = (DescWrapper) obj;
            ClassDesc classDesc = wrapper.getClassDesc();
            if (wrapper.isArray()) {
                // 配列にはプロパティを追加できないのでなにもしない。
                return null;
            }
            int mode = (classDesc.isKindOf(ClassDesc.KIND_DTO) ? (PropertyDesc.READ | PropertyDesc.WRITE)
                    : PropertyDesc.READ);
            return new DescWrapper(analyzerContext, adjustPropertyType(
                    analyzerContext, classDesc.getName(), classDesc
                            .addProperty(child, mode)));
        } else {
            return null;
        }
    }

    PropertyDesc adjustPropertyType(AnalyzerContext analyzerContext,
            String className, PropertyDesc pd) {
        String propertyTypeName;
        boolean array;
        PropertyTypeHint hint = analyzerContext.getPropertyTypeHint(className,
                pd.getName());
        if (hint != null) {
            propertyTypeName = hint.getTypeName();
            array = hint.isArray();
        } else {
            PropertyDescriptor descriptor = analyzerContext.getSourceCreator()
                    .getPropertyDescriptor(className, pd.getName());
            if (descriptor != null) {
                Class<?> propertyType;
                array = descriptor.getPropertyType().isArray();
                if (array) {
                    propertyType = descriptor.getPropertyType()
                            .getComponentType();
                } else {
                    propertyType = descriptor.getPropertyType();
                }
                propertyTypeName = propertyType.getName();
            } else {
                // ヒントも既存クラスからの情報もなければ何もしない。
                return pd;
            }
        }
        TypeDesc td = new TypeDescImpl(analyzerContext
                .getTemporaryClassDescFromClassName(propertyTypeName), array,
                true);
        pd.setTypeDesc(td);
        return pd;
    }
}
