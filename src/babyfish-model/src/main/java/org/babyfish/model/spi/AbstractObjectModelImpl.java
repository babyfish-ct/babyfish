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
package org.babyfish.model.spi;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.FrozenContext;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.data.ModificationException;
import org.babyfish.data.event.AttributeScope;
import org.babyfish.data.event.Cause;
import org.babyfish.lang.I18N;
import org.babyfish.lang.Nulls;
import org.babyfish.model.event.ScalarEvent;
import org.babyfish.model.event.ScalarListener;
import org.babyfish.model.metadata.ModelClass;
import org.babyfish.model.metadata.ModelProperty;
import org.babyfish.model.viewinfo.ObjectModelViewInfos;

/**
 * @author Tao Chen
 */
public abstract class AbstractObjectModelImpl implements ObjectModel, Serializable {

    private static final long serialVersionUID = 4366105014958468612L;

    private static final Object AK_SCALAR_LISTENER = new Object();
    
    private ModelClass modelClass;
    
    protected Object owner;
    
    private transient ScalarListener scalarListener;
    
    protected transient ScalarLoader scalarLoader;
    
    protected transient boolean loading;
    
    protected transient boolean executing;
    
    protected AbstractObjectModelImpl(ModelClass modelClass, Object owner) {
        this.modelClass = modelClass;
        this.owner  = owner;
    }

    @Override
    public ModelClass getModelClass() {
        return this.modelClass;
    }
    
    @Override
    public Object getOwner() {
        return this.owner;
    }

    @Override
    public ScalarLoader getScalarLoader() {
        return scalarLoader;
    }

    @Override
    public void setScalarLoader(ScalarLoader scalarLoader) {
        this.scalarLoader = scalarLoader;
    }
    
    @Override
    public void addScalarListener(ScalarListener listener) {
        this.scalarListener = ScalarListener.combine(this.scalarListener, listener);
    }

    @Override
    public void removeScalarListener(ScalarListener listener) {
        this.scalarListener = ScalarListener.remove(this.scalarListener, listener);
    }
    
    @Override
    public void load(int propertyId) {
        Set<ObjectModel> models = new LinkedHashSet<>();
        models.add(this);
        this.batchLoad(models, new int[] { propertyId });
    }

    @Override
    public void load(int... propertyIds) {
        Set<ObjectModel> models = new LinkedHashSet<>();
        models.add(this);
        this.batchLoad(models, propertyIds);
    }
    
    @Override
    public void batchLoad(Iterable<ObjectModel> objectModels, int[] propertyIds) {
        
        BatchLoadingContext ctx = this.new BatchLoadingContext(objectModels, propertyIds);
        if (ctx.objectModels == null) {
            return;
        }
        objectModels = ctx.objectModels;
        int[] scalarPropertyIds = ctx.scalarPropertyIds;
        int[] associationPropertyIds = ctx.associationPropertyIds;
        
        this.loading = true;
        try {
            if (scalarPropertyIds.length != 0) {
                ScalarLoader scalarLoader = this.scalarLoader;
                
                int maxUnloadedScalarCount = scalarPropertyIds.length;
                int propertyCount = this.modelClass.getPropertyList().size();
                Map<ScalarPropertyIdFlags, List<ObjectModel>> map = new LinkedHashMap<>();
                for (ObjectModel objectModel : objectModels) {
                    ScalarPropertyIdFlags flags = new ScalarPropertyIdFlags(propertyCount);
                    for (int scalarPropertyId : scalarPropertyIds) {
                        if (objectModel.isUnloaded(scalarPropertyId)) {
                            flags.set(scalarPropertyId);
                        }
                    }
                    if (flags.count != 0) {
                        List<ObjectModel> list = map.get(flags);
                        if (list == null) {
                            list = new ArrayList<>();
                            map.put(flags, list);
                        }
                        list.add(objectModel);
                    }
                }
                
                if (!map.isEmpty()) {
                    if (scalarLoader == null) {
                        throw new IllegalStateException(scalarLoaderIsNull());
                    }
                    for (Map.Entry<ScalarPropertyIdFlags, List<ObjectModel>> entry : map.entrySet()) {
                        ScalarPropertyIdFlags flags = entry.getKey();
                        int[] usedScalarPropertyIds = 
                                flags.count == maxUnloadedScalarCount ? 
                                        scalarPropertyIds : 
                                        flags.toIdArray();
                        scalarLoader.load(entry.getValue(), usedScalarPropertyIds);
                    }
                }
            }
            if (ctx.associationPropertyIds.length != 0) {
                for (ObjectModel objectModel : objectModels) {
                    for (int associationPropertyId : associationPropertyIds) {
                        objectModel.getAssociatedEndpoint(associationPropertyId).load();
                    }
                }
            }
        } finally {
            this.loading = false;
        }
    }
    
    private static ModelProperty realAssociationProperty(ModelProperty modelProperty) {
        while (true) {
            ModelProperty convarianceProperty = modelProperty.getConvarianceProperty();
            if (convarianceProperty == null) {
                break;
            }
            modelProperty = convarianceProperty;
        }
        return modelProperty;
    }
    
    protected void executeModifying(ScalarEvent e) {
        ScalarListener scalarListener = this.scalarListener;
        if (scalarListener == null) {
            return;
        }
        e.getAttributeContext(AttributeScope.LOCAL).setAttribute(AK_SCALAR_LISTENER, scalarListener);
        this.executing = true;
        try {
            scalarListener.modifying(e);
        } catch (Throwable ex) {
            throw new ModificationException(false, e, ex);
        } finally {
            this.executing = false;
        }
    }
    
    protected void executeModified(ScalarEvent e) {
        ScalarListener scalarListener = e.getAttributeContext(AttributeScope.LOCAL).getAttribute(AK_SCALAR_LISTENER);
        if (scalarListener == null) {
            return;
        }
        this.executing = true;
        try {
            scalarListener.modified(e);
        } catch (Throwable ex) {
            throw new ModificationException(true, e, ex);
        } finally {
            this.executing = false;
        }
    }
    
    protected abstract FrozenContext<?> getEmbeddedFrozenContext(int embeddedScalarPropertyId);
    
    protected static class EmbeddedScalarListenerImpl implements ScalarListener {
        
        private AbstractObjectModelImpl parentObjectModel;
        
        private int embeddedScalarPropertyId;

        public EmbeddedScalarListenerImpl(AbstractObjectModelImpl parentObjectModel, int embeddedScalarPropertyId) {
            this.parentObjectModel = parentObjectModel;
            this.embeddedScalarPropertyId = embeddedScalarPropertyId;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void modifying(ScalarEvent e) throws Throwable {
            final AbstractObjectModelImpl parentObjectModel = this.parentObjectModel;
            final int embeddedScalarPropertyId = this.embeddedScalarPropertyId;
            ScalarEvent bubbledEvent = new ScalarEvent(
                    parentObjectModel, 
                    new Cause(ObjectModelViewInfos.scalar(embeddedScalarPropertyId), e), 
                    () -> embeddedScalarPropertyId,
                    version -> parentObjectModel.get(embeddedScalarPropertyId)
            );
            parentObjectModel.executeModifying(bubbledEvent);
            FrozenContext<Object> ctx =  (FrozenContext<Object>)
                    parentObjectModel.getEmbeddedFrozenContext(embeddedScalarPropertyId);
            e.getAttributeContext(AttributeScope.LOCAL).setAttribute(this, new BubbleContext(bubbledEvent, ctx));
            FrozenContext.suspendFreezing(ctx, parentObjectModel.getOwner());
        }

        @Override
        public void modified(ScalarEvent e) throws Throwable {
            BubbleContext ctx = (BubbleContext)
                    e
                    .getAttributeContext(AttributeScope.LOCAL)
                    .getAttribute(this);
            try {
                FrozenContext.resumeFreezing(ctx.frozenContext);
            } finally {
                this.parentObjectModel.executeModified(ctx.bubbledEvent);
            }
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this.parentObjectModel) ^ 
                    this.embeddedScalarPropertyId;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof EmbeddedScalarListenerImpl)) {
                return false;
            }
            EmbeddedScalarListenerImpl other = (EmbeddedScalarListenerImpl)obj;
            return this.parentObjectModel == other.parentObjectModel &&
                    this.embeddedScalarPropertyId == other.embeddedScalarPropertyId;
        }
        
        // TODO: this class need to be removed finally.
        private static class BubbleContext {
            
            ScalarEvent bubbledEvent;
            
            FrozenContext<?> frozenContext;

            BubbleContext(ScalarEvent bubbledEvent, FrozenContext<?> frozenContext) {
                this.bubbledEvent = bubbledEvent;
                this.frozenContext = frozenContext;
            }
        }
    }
    
    private class BatchLoadingContext {
        
        Set<ObjectModel> objectModels;
        
        int[] scalarPropertyIds;
        
        int[] associationPropertyIds;
        
        public BatchLoadingContext(Iterable<ObjectModel> objectModels, int[] propertyIds) {
            
            if (Nulls.isNullOrEmpty(propertyIds)) {
                return;
            }
            
            Set<ObjectModel> objectModelSet = new LinkedHashSet<>();
            for (ObjectModel objectModel : objectModels) {
                if (objectModel != null) {
                    objectModelSet.add(objectModel);
                }
            }
            if (objectModelSet.isEmpty()) {
                return;
            }
            
            List<ModelProperty> propertyList = AbstractObjectModelImpl.this.modelClass.getPropertyList();
            Set<Integer> scalarPropertyIdSet = new LinkedHashSet<>();
            Set<Integer> associationPropertyIdSet = new LinkedHashSet<>();
            for (int propertyId : propertyIds) {
                if (propertyId < 0 || propertyId >= propertyList.size()) {
                    throw new IllegalArgumentException(illegalPropertyId(propertyId, propertyList.size()));
                }
                ModelProperty modelProperty = propertyList.get(propertyId);
                switch (modelProperty.getPropertyType()) {
                case SCALAR:
                    scalarPropertyIdSet.add(modelProperty.getId());
                    break;
                case INDEX:
                case KEY:
                    associationPropertyIdSet.add(realAssociationProperty(modelProperty.getReferenceProperty()).getId());
                    break;
                default:
                    associationPropertyIdSet.add(realAssociationProperty(modelProperty).getId());
                    break;
                }
            }
            this.objectModels = objectModelSet;
            this.scalarPropertyIds = MACollections.toIntArray(scalarPropertyIdSet);
            this.associationPropertyIds = MACollections.toIntArray(associationPropertyIdSet);
        }
    }
    
    private static class ScalarPropertyIdFlags {
        
        boolean[] arr;
        
        int count;
        
        int hash;
        
        ScalarPropertyIdFlags(int capacity) {
            this.arr = new boolean[capacity];
        }
        
        void set(int id) {
            if (!this.arr[id]) {
                this.arr[id] = true;
                this.count++;
                this.hash = 0;
            }
        }
        
        int[] toIdArray() {
            boolean[] src = this.arr;
            int[] dst = new int[this.count];
            int dstIndex = dst.length;
            for (int i = src.length - 1; i >= 0; i--) {
                if (src[i]) {
                    dst[--dstIndex] = i;
                }
            }
            return dst;
        }

        @Override
        public int hashCode() {
            int h = this.hash;
            if (h == 0) {
                h = Arrays.hashCode(this.arr);
                if (h == 0) {
                    h = -1;
                }
                this.hash = h;
            }
            return h;
        }

        @Override
        public boolean equals(Object obj) {
            // Private class, need only to check type.
            ScalarPropertyIdFlags other = (ScalarPropertyIdFlags)obj;
            return 
                    this.count == other.count &&
                    Arrays.equals(this.arr, other.arr);
        }
    }
    
    @I18N
    private static native String scalarLoaderIsNull();
    
    @I18N
    private static native String illegalPropertyId(int id, int maxIdExclusive);
}
