/*
 * BabyFish, Object Model Framework for Java and JPA.
 * https://github.com/babyfish-ct/babyfish
 *
 * Copyright (c) 2008-2016, Tao Chen
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * Please visit "http://opensource.org/licenses/LGPL-3.0" to know more.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 */
package org.babyfish.data.event;

import org.babyfish.data.AttributeContext;
import org.babyfish.data.event.spi.GlobalAttributeContext;
import org.babyfish.data.event.spi.InAllChainAttributeContext;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;

/**
 * @author Tao Chen
 */
public abstract class EventAttributeContext extends AttributeContext {
    
    private static final long serialVersionUID = 9195118361879364496L;

    private EventAttributeContext() {
        
    }
    
    public abstract AttributeScope getScope();
    
    public static EventAttributeContext of(AttributeScope scope) {
        switch (scope) {
        case LOCAL:
            return new Local();
        case IN_BUBBLE_CHAIN:
            return new InBubbleChain();
        case IN_DISPATCH_CHAIN:
            return new InDispatchChain();
        case IN_ALL_CHAIN:
            return new InAllChain();
        case GLOBAL:
            return new Global();
        default:
            return null; //Unrechable code
        }
    }
    
    private static class Local extends EventAttributeContext {
        
        private static final long serialVersionUID = -2083679996268243012L;

        @Override
        public AttributeScope getScope() {
            return AttributeScope.LOCAL;
        }
        
    }
    
    private static class InBubbleChain extends EventAttributeContext {
        
        private static final long serialVersionUID = -5277881728759402858L;

        @Override
        public AttributeScope getScope() {
            return AttributeScope.IN_BUBBLE_CHAIN;
        }
        
    }
    
    private static class InDispatchChain extends EventAttributeContext {
        
        private static final long serialVersionUID = -6386732619939474446L;

        @Override
        public AttributeScope getScope() {
            return AttributeScope.IN_DISPATCH_CHAIN;
        }
        
    }
    
    private static class InAllChain extends EventAttributeContext implements InAllChainAttributeContext {
        
        private static final long serialVersionUID = -3023406720901215054L;
        
        private Throwable preThrowable;
        
        @Override
        public AttributeScope getScope() {
            return AttributeScope.IN_ALL_CHAIN;
        }

        @Override
        public Throwable getPreThrowable() {
            return this.preThrowable;
        }
        
        @Override
        public void setPreThrowable(Throwable preThrowable) {
            this.preThrowable = 
                    Arguments.mustBeInstanceOfAnyOfValue(
                            "preThrowable", 
                            preThrowable, 
                            RuntimeException.class, 
                            Error.class);
        }
        
    }
    
    private static class Global extends EventAttributeContext implements GlobalAttributeContext {
        
        private static final long serialVersionUID = 728870481686715469L;
        
        private Throwable throwable;
        
        private boolean successed;
        
        @Override
        public AttributeScope getScope() {
            return AttributeScope.GLOBAL;
        }
        
        @Override
        public Throwable getThrowable() {
            return this.throwable;
        }

        @Override
        public boolean isSuccessed() {
            return this.successed;
        }

        @Override
        public void setThrowable(Throwable throwable) {
            if (throwable != null) {
                if (this.successed) {
                    throw new IllegalStateException(currentGlobalIsAlreadySuccessed(Global.class));
                }
                this.throwable = Arguments.mustBeInstanceOfAnyOfValue(
                        "throwable", 
                        throwable, 
                        RuntimeException.class, 
                        Error.class);
            }
        }
        
        @Override
        public void success() {
            if (this.throwable != null) {
                throw new IllegalStateException(currentGlobalIsAlreadyFailed(Global.class));
            }
            this.successed = true;
        }
        
        @I18N
        private static native String currentGlobalIsAlreadySuccessed(Class<Global> globalType);
        
        @I18N
        private static native String currentGlobalIsAlreadyFailed(Class<Global> globalType);
    }
}
