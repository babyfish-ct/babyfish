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
package org.babyfish.model.event;

import org.babyfish.data.event.PropertyVersion;
import org.babyfish.model.event.entities.Name;
import org.babyfish.model.event.entities.Person;
import org.babyfish.model.metadata.ModelClass;
import org.babyfish.model.spi.ObjectModelProvider;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class PersonTest {

    @Test
    public void testBubbleScalarEvent() {
        Person person = new Person();
        ScalarListenerImpl listener = new ScalarListenerImpl();
        ((ObjectModelProvider)person).objectModel().addScalarListener(listener);
        
        Name name1 = new Name();
        name1.setFirstName("Jame");
        name1.setLastName("Gosling");
        Name name2 = new Name();
        name2.setFirstName("Josh");
        name2.setLastName("Bloch");
        Assert.assertEquals("", listener.getAndClearString());
        
        person.setName(name1);
        name1.setFirstName("JAMES");
        name1.setLastName("GOSLING");
        name2.setFirstName("JOSH");
        name2.setLastName("BLOCH");
        Assert.assertEquals(
                ":pre({ scalarPropertyName: 'name', detachedScalar: null, attchedScalar: { firstName: 'Jame', lastName: 'Gosling' } })\n" +
                ":post({ scalarPropertyName: 'name', detachedScalar: null, attchedScalar: { firstName: 'Jame', lastName: 'Gosling' } })\n" +
                ":pre({ scalarPropertyName: 'name', detachedScalar: { firstName: 'Jame', lastName: 'Gosling' }, attchedScalar: { firstName: 'Jame', lastName: 'Gosling' }, causeScalarPropertyName: 'firstName' })\n" +
                ":post({ scalarPropertyName: 'name', detachedScalar: { firstName: 'JAMES', lastName: 'Gosling' }, attchedScalar: { firstName: 'JAMES', lastName: 'Gosling' }, causeScalarPropertyName: 'firstName' })\n" +
                ":pre({ scalarPropertyName: 'name', detachedScalar: { firstName: 'JAMES', lastName: 'Gosling' }, attchedScalar: { firstName: 'JAMES', lastName: 'Gosling' }, causeScalarPropertyName: 'lastName' })\n" +
                ":post({ scalarPropertyName: 'name', detachedScalar: { firstName: 'JAMES', lastName: 'GOSLING' }, attchedScalar: { firstName: 'JAMES', lastName: 'GOSLING' }, causeScalarPropertyName: 'lastName' })\n",
                listener.getAndClearString()
        );
        
        person.setName(name2);
        name1.setFirstName("james");
        name1.setLastName("gosling");
        name2.setFirstName("josh");
        name2.setLastName("bloch");
        Assert.assertEquals(
                ":pre({ scalarPropertyName: 'name', detachedScalar: { firstName: 'JAMES', lastName: 'GOSLING' }, attchedScalar: { firstName: 'JOSH', lastName: 'BLOCH' } })\n" +
                ":post({ scalarPropertyName: 'name', detachedScalar: { firstName: 'JAMES', lastName: 'GOSLING' }, attchedScalar: { firstName: 'JOSH', lastName: 'BLOCH' } })\n" +
                ":pre({ scalarPropertyName: 'name', detachedScalar: { firstName: 'JOSH', lastName: 'BLOCH' }, attchedScalar: { firstName: 'JOSH', lastName: 'BLOCH' }, causeScalarPropertyName: 'firstName' })\n" +
                ":post({ scalarPropertyName: 'name', detachedScalar: { firstName: 'josh', lastName: 'BLOCH' }, attchedScalar: { firstName: 'josh', lastName: 'BLOCH' }, causeScalarPropertyName: 'firstName' })\n" +
                ":pre({ scalarPropertyName: 'name', detachedScalar: { firstName: 'josh', lastName: 'BLOCH' }, attchedScalar: { firstName: 'josh', lastName: 'BLOCH' }, causeScalarPropertyName: 'lastName' })\n" +
                ":post({ scalarPropertyName: 'name', detachedScalar: { firstName: 'josh', lastName: 'bloch' }, attchedScalar: { firstName: 'josh', lastName: 'bloch' }, causeScalarPropertyName: 'lastName' })\n",
                listener.getAndClearString()
        );
        
        person.setName(name1);
        name1.setFirstName("JAMES");
        name1.setLastName("GOSLING");
        name2.setFirstName("JOSH");
        name2.setLastName("BLOCH");
        Assert.assertEquals(
                ":pre({ scalarPropertyName: 'name', detachedScalar: { firstName: 'josh', lastName: 'bloch' }, attchedScalar: { firstName: 'james', lastName: 'gosling' } })\n" +
                ":post({ scalarPropertyName: 'name', detachedScalar: { firstName: 'josh', lastName: 'bloch' }, attchedScalar: { firstName: 'james', lastName: 'gosling' } })\n" +
                ":pre({ scalarPropertyName: 'name', detachedScalar: { firstName: 'james', lastName: 'gosling' }, attchedScalar: { firstName: 'james', lastName: 'gosling' }, causeScalarPropertyName: 'firstName' })\n" +
                ":post({ scalarPropertyName: 'name', detachedScalar: { firstName: 'JAMES', lastName: 'gosling' }, attchedScalar: { firstName: 'JAMES', lastName: 'gosling' }, causeScalarPropertyName: 'firstName' })\n" +
                ":pre({ scalarPropertyName: 'name', detachedScalar: { firstName: 'JAMES', lastName: 'gosling' }, attchedScalar: { firstName: 'JAMES', lastName: 'gosling' }, causeScalarPropertyName: 'lastName' })\n" +
                ":post({ scalarPropertyName: 'name', detachedScalar: { firstName: 'JAMES', lastName: 'GOSLING' }, attchedScalar: { firstName: 'JAMES', lastName: 'GOSLING' }, causeScalarPropertyName: 'lastName' })\n",
                listener.getAndClearString()
        );
        
        person.setName(name2);
        name1.setFirstName("james");
        name1.setLastName("gosling");
        name2.setFirstName("josh");
        name2.setLastName("bloch");
        Assert.assertEquals(
                ":pre({ scalarPropertyName: 'name', detachedScalar: { firstName: 'JAMES', lastName: 'GOSLING' }, attchedScalar: { firstName: 'JOSH', lastName: 'BLOCH' } })\n" +
                ":post({ scalarPropertyName: 'name', detachedScalar: { firstName: 'JAMES', lastName: 'GOSLING' }, attchedScalar: { firstName: 'JOSH', lastName: 'BLOCH' } })\n" +
                ":pre({ scalarPropertyName: 'name', detachedScalar: { firstName: 'JOSH', lastName: 'BLOCH' }, attchedScalar: { firstName: 'JOSH', lastName: 'BLOCH' }, causeScalarPropertyName: 'firstName' })\n" +
                ":post({ scalarPropertyName: 'name', detachedScalar: { firstName: 'josh', lastName: 'BLOCH' }, attchedScalar: { firstName: 'josh', lastName: 'BLOCH' }, causeScalarPropertyName: 'firstName' })\n" +
                ":pre({ scalarPropertyName: 'name', detachedScalar: { firstName: 'josh', lastName: 'BLOCH' }, attchedScalar: { firstName: 'josh', lastName: 'BLOCH' }, causeScalarPropertyName: 'lastName' })\n" +
                ":post({ scalarPropertyName: 'name', detachedScalar: { firstName: 'josh', lastName: 'bloch' }, attchedScalar: { firstName: 'josh', lastName: 'bloch' }, causeScalarPropertyName: 'lastName' })\n",
                listener.getAndClearString()
        );
    }
    
    static class ScalarListenerImpl implements ScalarListener {
            
        private StringBuilder builder;
        
        public ScalarListenerImpl() {
            this.builder = new StringBuilder();
        }

        @Override
        public void modifying(ScalarEvent e) throws Throwable {
            this.builder.append(":pre(");
            this.append(e);
            this.builder.append(")\n");
        }

        @Override
        public void modified(ScalarEvent e) throws Throwable {
            this.builder.append(":post(");
            this.append(e);
            this.builder.append(")\n");
        }
        
        public String getAndClearString() {
            String text = this.builder.toString();
            if (!text.isEmpty()) {
                this.builder = new StringBuilder();
            }
            return text;
        }
        
        private void append(ScalarEvent e) {
            this
            .builder
            .append("{ scalarPropertyName: '")
            .append(ModelClass.of(Person.class).getProperty(e.getScalarPropertyId()).getName())
            .append("', detachedScalar: ")
            .append(e.getValue(PropertyVersion.DETACH))
            .append(", attchedScalar: ")
            .append(e.getValue(PropertyVersion.ATTACH))
            .append("");
            if (e.getCause() != null) {
                ScalarEvent rawEvent = (ScalarEvent)e.getCause().getViewEvent();
                this
                .builder
                .append(", causeScalarPropertyName: '")
                .append(ModelClass.of(Name.class).getProperty(rawEvent.getScalarPropertyId()).getName())
                .append('\'');
            }
            this.builder.append(" }");
        }
    }
}
