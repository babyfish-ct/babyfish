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
package org.babyfish.collection.spi.base;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Function;

import org.babyfish.collection.MACollections;
import org.babyfish.collection.XNavigableMap;
import org.babyfish.collection.XNavigableMap.XNavigableMapView;
import org.babyfish.collection.XNavigableSet;
import org.babyfish.collection.XNavigableSet.XNavigableSetView;
import org.babyfish.collection.viewinfo.NavigableMapViewInfos;
import org.babyfish.collection.viewinfo.NavigableSetViewInfos;
import org.babyfish.collection.viewinfo.SortedMapViewInfos;
import org.babyfish.collection.viewinfo.SortedSetViewInfos;
import org.babyfish.data.ViewInfo;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.lang.Nulls;

/**
 * @author Tao Chen
 */
public class NavigableRange<E> implements Serializable {
    
    private static final long serialVersionUID = -6373518586859062982L;
    
    private static final NavigableRange<Object> DEFAULT_RANGE = new NavigableRange<>();
    
    private static final boolean DEFAULT_FROM_INCLUSIVE = true;
    
    private static final boolean DEFAULT_TO_INCLUSIVE = false;
    
    private boolean descending;
    
    private Comparator<? super E> comparator;

    private boolean hasFrom;
    
    private E from;
    
    private boolean fromInclusive;
    
    private boolean hasTo;
    
    private E to;
    
    private boolean toInclusive;
    
    private transient String toString;
    
    private NavigableRange() {
        this.hasFrom = false;
        this.from = null;
        this.fromInclusive = true;
        this.hasTo = false;
        this.to = null;
        this.toInclusive = true;
    }
    
    @SuppressWarnings("unchecked")
    private NavigableRange(NavigableBaseEntries<E, ?> parentEntries) {
        NavigableRange<E> parentRange = parentEntries.range();
        if (parentRange == null) {
            parentRange = (NavigableRange<E>)DEFAULT_RANGE;
        }
        this.descending = !parentRange.descending;
        this.comparator = MACollections.reverseOrder(parentEntries.comparator());
        this.hasFrom = parentRange.hasTo;
        this.from = parentRange.to;
        this.fromInclusive = parentRange.toInclusive;
        this.hasTo = parentRange.hasFrom;
        this.to = parentRange.from;
        this.toInclusive = parentRange.fromInclusive;
    }
    
    @SuppressWarnings("unchecked")
    private NavigableRange(
            NavigableBaseEntries<E, ?> parentEntries,
            boolean hasFrom, E from, boolean fromInclusive,
            boolean hasTo, E to, boolean toInclusive) {
        NavigableRange<E> parentRange = parentEntries.range();
        if (parentRange == null) {
            parentRange = (NavigableRange<E>)DEFAULT_RANGE;
        }
        this.descending = parentRange.descending;
        this.comparator = parentEntries.comparator();
        this.hasFrom = parentRange.hasFrom;
        this.from = parentRange.from;
        this.fromInclusive = parentRange.fromInclusive;
        this.hasTo = parentRange.hasTo;
        this.to = parentRange.to;
        this.toInclusive = parentRange.toInclusive;
        if (hasFrom && hasTo && this.compare(from, to) > 0) {
            throw new IllegalArgumentException(formGreaterThanTo(from, to));
        } 
        if (hasFrom) {
            this.acceptFrom(from, fromInclusive);
        }
        if (hasTo) {
            this.acceptTo(to, toInclusive);
        }
        /*
         * The custom can use break point to debug this class,
         * the debugger's variable/expression window may initialize these fields
         * So it is necessary to clear them. 
         */
        this.toString = null;
    }
    
    public static <E> NavigableRange<E> descendingRange(NavigableBaseEntries<E, ?> baseEntries) {
        return new NavigableRange<>(baseEntries);
    }
    
    public static <E> NavigableRange<E> subRange(
            NavigableBaseEntries<E, ?> parentEntries, 
            boolean hasFrom, E from, boolean fromInclusive, 
            boolean hasTo, E to, boolean toInclusive) {
        if (!hasFrom && !hasTo) {
            throw new IllegalArgumentException(parametersCanNotBeFalseSimultaneously("hasFrom", "hasTo"));
        }
        if (Arguments.mustNotBeNull("parentEntries", parentEntries).comparator() == null) {
            if (hasFrom) {
                Arguments.mustBeInstanceOfValueWhen(
                        whenExpressionIsNullAndParameterIsTrue("parentEntries.comparator()", "hasFrom"), 
                        "from", 
                        Arguments.mustNotBeNull("from", from), 
                        Comparable.class);
            }
            if (hasTo) {
                Arguments.mustBeInstanceOfValueWhen(
                        whenExpressionIsNullAndParameterIsTrue("parentEntries.comparator()", "hasTo"),
                        "to", 
                        Arguments.mustNotBeNull("to", to), 
                        Comparable.class);
            }
        }
        return new NavigableRange<>(parentEntries, hasFrom, from, fromInclusive, hasTo, to, toInclusive);
    }
    
    //Only used by AbstractWrapperXSet<E>
    public NavigableRange(XNavigableSetView<E> view, Function<XNavigableSetView<E>, XNavigableSet<E>> parentLambda) {
        Arguments.mustNotBeNull("view", view);
        Arguments.mustNotBeNull("parentLambda", parentLambda);
        this.fromInclusive = true;
        this.toInclusive = true;
        this.accept(view, parentLambda);
    }
    
    //Only used by AbstractWrapperXMap<K, V>
    public NavigableRange(XNavigableMapView<E, ?> view, Function<XNavigableMapView<E, ?>, XNavigableMap<E, ?>> parentLambda) {
        Arguments.mustNotBeNull("view", view);
        Arguments.mustNotBeNull("parentLambda", parentLambda);
        this.fromInclusive = true;
        this.toInclusive = true;
        this.accept(view, parentLambda);
    }
    
    public boolean descending() {
        return this.descending;
    }
    
    public Comparator<? super E> comparator(boolean abs) {
        if (abs && this.descending) {
            return MACollections.reverseOrder(this.comparator);
        }
        return this.comparator;
    }
    
    public boolean hasFrom(boolean abs) {
        if (abs && this.descending) {
            return this.hasTo;
        }
        return this.hasFrom;
    }
    
    public E from(boolean abs) {
        if (abs && this.descending) {
            return this.to;
        }
        return this.from;
    }
    
    public boolean fromInclusive(boolean abs) {
        if (abs && this.descending) {
            return this.toInclusive;
        }
        return this.fromInclusive;
    }
    
    public boolean hasTo(boolean abs) {
        if (abs && this.descending) {
            return this.hasFrom;
        }
        return this.hasTo;
    }
    
    public E to(boolean abs) {
        if (abs && this.descending) {
            return this.from;
        }
        return this.to;
    }
    
    public boolean toInclusive(boolean abs) {
        if (abs && this.descending) {
            return this.fromInclusive;
        }
        return this.toInclusive;
    }
    
    public boolean tooLow(E element, boolean abs) {
        if (abs && this.descending) {
            return this.tooHigh(element, false);
        }
        if (this.hasFrom) {
            int cmp = this.compare(this.from, element);         
            if (this.fromInclusive) {
                if (cmp > 0) {
                    return true;
                }
            } else {
                if (cmp >= 0) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean tooHigh(E element, boolean abs) {
        if (abs && this.descending) {
            return this.tooLow(element, false);
        }
        if (this.hasTo) {
            int cmp = this.compare(this.to, element);
            if (this.toInclusive) {
                if (cmp < 0) {
                    return true;
                }
            } else {
                if (cmp <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean contains(E element) {
        return !tooLow(element, false) && !tooHigh(element, false);
    }
    
    public boolean containsAll(Collection<? extends E> c) {
        for (E e : c) {
            if (!this.contains(e)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String toString() {
        String toString = this.toString;
        if (toString == null) {
            StringBuilder builder = new StringBuilder();
            builder
            .append(this.descending ? "desc" : "asc")
            .append(this.hasFrom && this.fromInclusive ? '[' : '(')
            .append(this.hasFrom ? Nulls.toString(this.from) : "\u221E")
            .append(", ")
            .append(this.hasTo ? Nulls.toString(this.to) : "\u221E")
            .append(this.hasTo && this.toInclusive ? ']' : ')');
            this.toString = toString = builder.toString();
        }
        return toString;
    }

    @SuppressWarnings("unchecked")
    private void accept(XNavigableSetView<E> view, Function<XNavigableSetView<E>, XNavigableSet<E>> parentLambda) {
        
        XNavigableSet<E> parent = parentLambda.apply(view);
        if (parent instanceof XNavigableSetView<?>) {
            this.accept((XNavigableSetView<E>)parent, parentLambda);
        }
        
        ViewInfo viewInfo = view.viewInfo();
        
        if (viewInfo instanceof NavigableSetViewInfos.DescendingSet) {
            boolean has = this.hasFrom;
            E e = this.from;
            boolean inclusive = this.fromInclusive;
            this.descending = !this.descending;
            this.comparator = MACollections.reverseOrder(parent.comparator());
            this.hasFrom = this.hasTo;
            this.from = this.to;
            this.fromInclusive = this.toInclusive;
            this.hasTo = has;
            this.to = e;
            this.toInclusive = inclusive;
        } else {
            this.comparator = parent.comparator();
            if (viewInfo instanceof SortedSetViewInfos.HeadSet) {
                boolean toInclusive;
                if (viewInfo instanceof NavigableSetViewInfos.HeadSet) {
                    toInclusive = ((NavigableSetViewInfos.HeadSet)viewInfo).isInclusive();
                } else {
                    toInclusive = DEFAULT_TO_INCLUSIVE;
                }
                E to = (E)((SortedSetViewInfos.HeadSet)viewInfo).getToElement();
                this.acceptTo(to, toInclusive);
            } else if (viewInfo instanceof SortedSetViewInfos.TailSet) {
                boolean fromInclusive;
                if (viewInfo instanceof NavigableSetViewInfos.TailSet) {
                    fromInclusive = ((NavigableSetViewInfos.TailSet)viewInfo).isInclusive();
                } else {
                    fromInclusive = DEFAULT_FROM_INCLUSIVE;
                }
                E from  = (E)((SortedSetViewInfos.TailSet)viewInfo).getFromElement();
                this.acceptFrom(from, fromInclusive);
            } else {
                boolean fromInclusive;
                boolean toInclusive;
                if (viewInfo instanceof NavigableSetViewInfos.SubSet) {
                    fromInclusive = ((NavigableSetViewInfos.SubSet)viewInfo).isFromInclusive();
                    toInclusive = ((NavigableSetViewInfos.SubSet)viewInfo).isToInclusive();
                } else {
                    fromInclusive = DEFAULT_FROM_INCLUSIVE;
                    toInclusive = DEFAULT_TO_INCLUSIVE;
                }
                E from  = (E)((SortedSetViewInfos.SubSet)viewInfo).getFromElement();
                E to = (E)((SortedSetViewInfos.SubSet)viewInfo).getToElement();
                this.acceptFrom(from, fromInclusive);
                this.acceptTo(to, toInclusive);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void accept(
            XNavigableMapView<E, ?> view, 
            Function<XNavigableMapView<E, ?>, XNavigableMap<E, ?>> parentLambda) {
        
        XNavigableMap<E, ?> parent = parentLambda.apply(view);
        if (parent instanceof XNavigableMapView<?, ?>) {
            this.accept((XNavigableMapView<E, ?>)parent, parentLambda);
        }
        
        ViewInfo viewInfo = view.viewInfo();
        
        if (viewInfo instanceof NavigableMapViewInfos.DescendingMap) {
            boolean has = this.hasFrom;
            E e = this.from;
            boolean inclusive = this.fromInclusive;
            this.descending = !this.descending;
            this.comparator = MACollections.reverseOrder(parent.comparator());
            this.hasFrom = this.hasTo;
            this.from = this.to;
            this.fromInclusive = this.toInclusive;
            this.hasTo = has;
            this.to = e;
            this.toInclusive = inclusive;
        } else {
            this.comparator = parent.comparator();
            if (viewInfo instanceof SortedMapViewInfos.HeadMap) {
                boolean toInclusive;
                if (viewInfo instanceof NavigableMapViewInfos.HeadMap) {
                    toInclusive = ((NavigableMapViewInfos.HeadMap)viewInfo).isInclusive();
                } else {
                    toInclusive = DEFAULT_TO_INCLUSIVE;
                }
                E to = (E)((SortedMapViewInfos.HeadMap)viewInfo).getToKey();
                this.acceptTo(to, toInclusive);
            } else if (viewInfo instanceof SortedMapViewInfos.TailMap) {
                boolean fromInclusive;
                if (viewInfo instanceof NavigableMapViewInfos.TailMap) {
                    fromInclusive = ((NavigableMapViewInfos.TailMap)viewInfo).isInclusive();
                } else {
                    fromInclusive = DEFAULT_FROM_INCLUSIVE;
                }
                E from  = (E)((SortedMapViewInfos.TailMap)viewInfo).getFromKey();
                this.acceptFrom(from, fromInclusive);
            } else {
                boolean fromInclusive;
                boolean toInclusive;
                if (viewInfo instanceof NavigableMapViewInfos.SubMap) {
                    fromInclusive = ((NavigableMapViewInfos.SubMap)viewInfo).isFromInclusive();
                    toInclusive = ((NavigableMapViewInfos.SubMap)viewInfo).isToInclusive();
                } else {
                    fromInclusive = DEFAULT_FROM_INCLUSIVE;
                    toInclusive = DEFAULT_TO_INCLUSIVE;
                }
                E from  = (E)((SortedMapViewInfos.SubMap)viewInfo).getFromKey();
                E to = (E)((SortedMapViewInfos.SubMap)viewInfo).getToKey();
                this.acceptFrom(from, fromInclusive);
                this.acceptTo(to, toInclusive);
            }
        }
    }
    
    private void acceptFrom(E from, boolean fromInclusive) {
        if (!this.hasFrom) {
            this.hasFrom = true;
            this.from = from;
            this.fromInclusive = fromInclusive;
        } else {
            int cmp = this.compare(this.from, from);
            if (cmp > 0) {
                throw new IllegalArgumentException(parameterOutOfRange("from", from, this));
            }
            if (cmp == 0) {
                if (!this.fromInclusive) {
                    throw new IllegalArgumentException(parameterOutOfRange("from", from, this));
                }
                this.fromInclusive &= fromInclusive;
            } else {
                this.from = from;
                this.fromInclusive = fromInclusive;
            }
        }
    }
    
    private void acceptTo(E to, boolean toInclusive) {
        if (!this.hasTo) {
            this.hasTo = true;
            this.to = to;
            this.toInclusive = toInclusive;
        } else {
            int cmp = this.compare(this.to, to);
            if (cmp < 0) {
                throw new IllegalArgumentException(parameterOutOfRange("to", to, this));
            }
            if (cmp == 0) {
                if (!this.toInclusive) {
                    throw new IllegalArgumentException(parameterOutOfRange("to", to, this));
                }
                this.toInclusive &= toInclusive;
            } else {
                this.to = to;
                this.toInclusive = toInclusive;
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private int compare(E e1, E e2) {
        if (this.comparator == null) {
            return ((Comparable<Object>)e1).compareTo(e2);
        }
        return this.comparator.compare(e1, e2);
    }
        
    @I18N
    private static native String parametersCanNotBeFalseSimultaneously(String hasFrom, String hasTo);
    
    @I18N
    private static native String whenExpressionIsNullAndParameterIsTrue(String expression, String parameterName);
    
    @I18N
    private static native String formGreaterThanTo(Object from, Object to);
    
    @I18N
    private static native String parameterOutOfRange(String parameterName, Object argument, NavigableRange<?> range);
}
