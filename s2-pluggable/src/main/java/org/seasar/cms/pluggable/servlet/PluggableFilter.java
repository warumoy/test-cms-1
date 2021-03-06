package org.seasar.cms.pluggable.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.seasar.cms.pluggable.SingletonPluggableContainerFactory;
import org.seasar.cms.pluggable.hotdeploy.DistributedHotdeployBehavior;
import org.seasar.cms.pluggable.util.PluggableUtils;
import org.seasar.framework.container.ExternalContext;
import org.seasar.framework.container.impl.S2ContainerBehavior;

public class PluggableFilter implements Filter {

    public void init(FilterConfig config) throws ServletException {
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        DistributedHotdeployBehavior behavior = (DistributedHotdeployBehavior) S2ContainerBehavior
                .getProvider();
        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        behavior.start();
        try {
            Thread.currentThread().setContextClassLoader(
                    PluggableUtils.adjustClassLoader(behavior, classLoader));

            ExternalContext externalContext = SingletonPluggableContainerFactory
                    .getRootContainer().getExternalContext();
            externalContext.setRequest(request);
            externalContext.setResponse(response);
            try {
                chain.doFilter(request, response);
            } finally {
                externalContext.setRequest(null);
                externalContext.setResponse(null);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
            behavior.stop();
        }
    }
}