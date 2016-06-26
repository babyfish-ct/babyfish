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
package org.babyfish.lang.bytecode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;

/**
 * @author Tao Chen
 */
public class VariableScopeBuilder {
    
    private Map<String, Variable> parameterMap;
    
    public VariableScopeBuilder parameter(String name, String desc) {
        return this.parameter(name, desc, null);
    }
    
    public VariableScopeBuilder parameter(String name, String desc, String signature) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("The argument \"name\" can't be null or empty.");
        }
        if (desc == null || desc.isEmpty()) {
            throw new IllegalArgumentException("The argument \"desc\" can't be null or empty.");
        }
        if (signature != null && signature.isEmpty()) {
            throw new IllegalArgumentException("The argument \"name\" can't be empty.");
        }
        Map<String, Variable> map = this.parameterMap;
        if (map == null) {
            this.parameterMap = map = new LinkedHashMap<>();
        }
        Variable variable = new Variable(name, desc, signature);
        if (map.put(name, variable) != null) {
            throw new IllegalArgumentException("Duplicated parameter \"" + name + "\"");
        }
        return this;
    }
    
    public VariableScope build(MethodVisitor mv) {
        if (mv == null) {
            throw new IllegalArgumentException("The argument \"mv\" can't be null");
        }
        VariableScope scope = new ScopeImpl(mv, this.parameterMap);
        this.parameterMap = null;
        return scope;
    }
    
    private static class ScopeImpl implements VariableScope {
        
        private static final String HIDDEN_PREFIX = "@";
        
        private MethodVisitor mv;
        
        private int slot;
        
        private int suspendedCount;
        
        private int hiddenSequence;
        
        private boolean closed;
        
        private Map<String, Variable> nameVariableMap;
        
        private NavigableMap<Integer, Variable> slotVariableMap = new TreeMap<>();
        
        ScopeImpl parentGroup;
        
        ScopeImpl(MethodVisitor mv, Map<String, Variable> parameterMap) {
            Label startScopeLabel = new Label();
            int slot = 0;
            if (parameterMap != null) {
                for (Variable variable : parameterMap.values()) {
                    variable.slot = slot;
                    variable.label = startScopeLabel;
                    variable.stored = true;
                    slot += ASMUtils.getSlotCount(variable.desc);
                }
            }
            this.mv = mv;
            this.slot = slot;
            if (parameterMap == null) {
                this.nameVariableMap = new LinkedHashMap<>();
            } else {
                this.nameVariableMap = parameterMap;
            }
            mv.visitLabel(startScopeLabel);
        }
        
        private ScopeImpl(ScopeImpl parentScope) {
            parentScope.suspendedCount++;
            this.parentGroup = parentScope;
            this.mv = parentScope.mv;
            this.slot = parentScope.slot;
            this.hiddenSequence = parentScope.hiddenSequence;
            this.nameVariableMap = new LinkedHashMap<>();
        }
        
        @Override
        public VariableScope declare(String name, String desc) {
            return this.declare(name, desc, null);
        }

        public VariableScope declare(String name, String desc, String signature) {
            this.newVariable(name, desc, signature);
            return this;
        }
        
        public VariableScope declareImmediately(String name, String desc) {
            return this.declareImmediately(name, desc, null);
        }
        
        public VariableScope declareImmediately(String name, String desc, String signature) {
            Variable variable = this.newVariable(name, desc, signature);
            this.allocate(variable);
            return this;
        }
        
        public VariableScope store(String name) {
            if (this.closed) {
                throw new IllegalStateException("The current scope is closed");
            }
            if (!this.storeImpl(name)) {
                throw new IllegalStateException(
                        "The variable \""
                        + name
                        + "\" has not been declared."
                );
            }
            return this;
        }

        public VariableScope load(String name) {
            Variable variable = this.findAllocatedVariable(name, false);
            if (!variable.stored) {
                throw new IllegalStateException(
                        "Can't load the variable \"" +
                        name +
                        "\" because it has not been stored"
                );
            }
            if (variable.slot > 0) {
                for (Variable lessSlotVariable : this.slotVariableMap.headMap(variable.slot, false).values()) {
                    if (!lessSlotVariable.stored) {
                        throw new IllegalStateException(
                                "Can't load the variable \"" +
                                name +
                                "\", its slot is \"" +
                                variable.slot +
                                "\" but the other variable \"" +
                                lessSlotVariable.name +
                                "\" which has a smaller slot " +
                                lessSlotVariable.slot +
                                " has not been stored"
                        );
                    }
                }
            }
            mv.visitVarInsn(ASMUtils.getLoadCode(variable.desc), variable.slot);
            return this;
        };
        
        public int slot(String name) {
            Variable variable = this.findAllocatedVariable(name, false);
            if (variable.slot == -1) {
                throw new IllegalStateException(
                        "The slot has not been allocated for \"" +
                        name +
                        "\""
                );
            }
            return variable.slot;
        };
        
        @Override
        public String descriptor(String name) {
            return this.findAllocatedVariable(name, false).desc;
        }

        @Override
        public String allocateHiddenName() {
            if (this.closed) {
                throw new IllegalStateException("The current scope is closed");
            }
            return HIDDEN_PREFIX + this.hiddenSequence++;
        }
        
        @Override
        public VariableScope createSubScope() {
            return new ScopeImpl(this);
        }

        @Override
        public void close() {
            if (this.closed) {
                return;
            }
            if (this.suspendedCount != 0) {
                throw new IllegalStateException("The current scope is suspended");
            }
            if (this.parentGroup != null) {
                this.parentGroup.suspendedCount--;
            }
            this.closed = true;
            
            Label closeScopeLabel = null;
            for (Variable variable : this.nameVariableMap.values()) {
                if (variable.label != null && !variable.name.startsWith(HIDDEN_PREFIX)) {
                    if (closeScopeLabel == null) {
                        closeScopeLabel = new Label();
                        this.mv.visitLabel(closeScopeLabel);
                    }
                    this.mv.visitLocalVariable(
                            variable.name, 
                            variable.desc, 
                            variable.signature, 
                            variable.label, 
                            closeScopeLabel, 
                            variable.slot
                    );
                }
            }
            this.nameVariableMap = null;
        }

        private Variable newVariable(String name, String desc, String signature) {
            if (this.closed) {
                throw new IllegalStateException("The current scope is closed");
            }
            Variable variable = this.findAllocatedVariable(name, true);
            if (variable != null) {
                throw new IllegalArgumentException(
                        "The variable \"" + name + "\" can't be declared twice in current scope tree"
                );
            }
            variable = new Variable(name, desc, signature);
            this.nameVariableMap.put(name, variable);
            return variable;
        }

        private boolean storeImpl(String name) {
            Variable variable = this.nameVariableMap.get(name);
            if (variable != null) {
                this.store(variable);
                return true;
            }
            if (this.parentGroup != null) {
                return this.parentGroup.storeImpl(name);
            }
            return false;
        }

        private void allocate(Variable variable) {
            if (variable.slot == -1) {
                if (this.suspendedCount != 0) {
                    throw new IllegalStateException(
                            "Can't allocate the slot for the variable \""
                            + variable.name
                            + "\" because the current scope is suspended"
                    );
                }
                variable.slot = this.slot;
                this.slotVariableMap.put(variable.slot, variable);
                this.slot += ASMUtils.getSlotCount(variable.desc);
                variable.label = new Label();
                this.mv.visitLabel(variable.label);
            }
        }

        private void store(Variable variable) {
            this.allocate(variable);
            variable.stored = true;
            this.mv.visitVarInsn(ASMUtils.getStoreCode(variable.desc), variable.slot);
        }

        private Variable findAllocatedVariable(String name, boolean nullable) {
            if (this.closed) {
                throw new IllegalStateException("The current scope is closed");
            }
            Variable variable = null;
            for (ScopeImpl scope = this; scope != null; scope = scope.parentGroup) {
                variable = scope.nameVariableMap.get(name);
                if (variable != null) {
                    break;
                }
            }
            if (variable == null) {
                if (!nullable) {
                    throw new IllegalArgumentException(
                            "The variable \"" + name + "\" has never been declared."
                    );
                }
            } else if (variable.slot == -1) {
                throw new IllegalArgumentException(
                        "The variable \"" + name + "\" has never been allocated."
                );
            }
            return variable;
        }
    }
    
    private static class Variable {
        
        String name;
        
        String desc;
        
        String signature;
        
        int slot = -1;
        
        Label label;
        
        boolean stored;
        
        Variable(String name, String desc, String signature) {
            this.name = name;
            this.desc = desc;
            this.signature = signature;
        }

        @Override
        public String toString() {
            return 
                    "{ name: " + this.name +
                    ", desc: " + this.desc +
                    ", signature: " + this.signature +
                    ", slot: " + this.slot +
                    " }";
        }
    }
}
