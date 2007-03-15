package org.seasar.cms.pluggable.hotdeploy;

import org.seasar.cms.pluggable.SingletonPluggableContainerFactory;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.impl.S2ContainerBehavior.DefaultProvider;
import org.seasar.framework.log.Logger;
import org.seasar.framework.util.DisposableUtil;

public class DistributedHotdeployBehavior extends DefaultProvider {

    private boolean hotdeployEnabled_;

    private LocalHotdeployS2Container[] localHotdeployS2Containers_;

    private int counter_ = 0;

    private Logger logger_ = Logger.getLogger(getClass());

    public void init(boolean hotdeployEnabled) {
        hotdeployEnabled_ = hotdeployEnabled;
        initializeLocalHotdeployS2Containers();
    }

    void initializeLocalHotdeployS2Containers() {
        S2Container container = getContainer();

        // LocalHotdeployS2Containerを集める。
        ComponentDef[] componentDefs = container
                .findAllComponentDefs(LocalHotdeployS2Container.class);
        localHotdeployS2Containers_ = new LocalHotdeployS2Container[componentDefs.length];
        for (int i = 0; i < componentDefs.length; i++) {
            ComponentDef cd = componentDefs[i];
            LocalHotdeployS2Container localContainer = (LocalHotdeployS2Container) cd
                    .getComponent();

            // このLocalHotdeployS2Containerが登録されているコンテナから見える
            // HotdeployListenerを登録する。
            HotdeployListener[] listeners = (HotdeployListener[]) cd
                    .getContainer().findAllComponents(HotdeployListener.class);
            for (int j = 0; j < listeners.length; j++) {
                HotdeployListener listener = listeners[j];
                if (listener instanceof LocalHotdeployS2Container
                        && listener != localContainer) {
                    // このLocalHotdeployS2Containerが登録されているコンテナから見えても、
                    // 自分以外のLocalHotdeployS2Containerは登録しない。
                    continue;
                }
                localContainer.addHotdeployListener(listener);
            }
            localContainer.init(hotdeployEnabled_);
            localHotdeployS2Containers_[i] = localContainer;
        }
    }

    public LocalHotdeployS2Container[] getLocalHotdeployS2Containers() {
        return localHotdeployS2Containers_;
    }

    public void destroy() {
        for (int i = 0; i < localHotdeployS2Containers_.length; i++) {
            localHotdeployS2Containers_[i].destroy();
        }
        hotdeployEnabled_ = false;
    }

    public synchronized void start() {
        if (logger_.isDebugEnabled()) {
            logger_.debug("HotdeployBehavior's start() method called");
        }
        if (!hotdeployEnabled_) {
            return;
        }

        if (counter_++ == 0) {
            if (logger_.isDebugEnabled()) {
                logger_.debug("HOTDEPLOY BEHAVIOR STARTING...");
            }
            for (int i = 0; i < localHotdeployS2Containers_.length; i++) {
                localHotdeployS2Containers_[i].start();
            }
            if (logger_.isDebugEnabled()) {
                logger_.debug("HOTDEPLOY BEHAVIOR STARTED");
            }
        }
    }

    S2Container getContainer() {
        return SingletonPluggableContainerFactory.getRootContainer();
    }

    public synchronized void stop() {
        if (logger_.isDebugEnabled()) {
            logger_.debug("HotdeployBehavior's stop() method called");
        }
        if (!hotdeployEnabled_) {
            return;
        }

        if (--counter_ == 0) {
            if (logger_.isDebugEnabled()) {
                logger_.debug("HOTDEPLOY BEHAVIOR STOPPING...");
            }
            DisposableUtil.dispose();

            for (int i = 0; i < localHotdeployS2Containers_.length; i++) {
                localHotdeployS2Containers_[i].stop();
            }

            if (logger_.isDebugEnabled()) {
                logger_.debug("HOTDEPLOY BEHAVIOR STOPPED");
            }
        } else if (counter_ < 0) {
            throw new IllegalStateException("Unbalanced stop() calling");
        }
    }

    protected ComponentDef getComponentDef(S2Container container, Object key) {

        ComponentDef cd = super.getComponentDef(container, key);
        if (cd != null) {
            return cd;
        }
        return findComponentDefFromHotdeployS2Containers(container, key);
    }

    protected ComponentDef findComponentDefFromHotdeployS2Containers(
            S2Container container, Object key) {

        LocalHotdeployS2Container[] localHotdeployS2Containers = (LocalHotdeployS2Container[]) container
                .findAllComponents(LocalHotdeployS2Container.class);
        for (int i = 0; i < localHotdeployS2Containers.length; i++) {
            ComponentDef cd = localHotdeployS2Containers[i]
                    .findComponentDef(key);
            if (cd != null) {
                return cd;
            }
        }
        return null;
    }
}
