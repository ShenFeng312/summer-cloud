package org.summer.cloud.config.refresh;

import org.springframework.beans.factory.ObjectFactory;

/**
 * @author shenfeng
 * base from spring cloud
 */
public class BeanLifecycleWrapper {

    private final String name;

    private final ObjectFactory<?> objectFactory;

    private volatile Object bean;

    private Runnable callback;

    BeanLifecycleWrapper(String name, ObjectFactory<?> objectFactory) {
        this.name = name;
        this.objectFactory = objectFactory;
    }

    public String getName() {
        return this.name;
    }

    public void setDestroyCallback(Runnable callback) {
        this.callback = callback;
    }

    public Object getBean() {
        if (this.bean == null) {
            synchronized (this.name) {
                if (this.bean == null) {
                    this.bean = this.objectFactory.getObject();
                }
            }
        }
        return this.bean;
    }

    public void destroy() {
        if (this.callback == null) {
            return;
        }
        synchronized (this.name) {
            Runnable callback = this.callback;
            if (callback != null) {
                callback.run();
            }
            this.callback = null;
            this.bean = null;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BeanLifecycleWrapper other = (BeanLifecycleWrapper) obj;
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

}