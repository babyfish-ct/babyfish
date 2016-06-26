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
package org.babyfish.persistence.criteria;

import java.util.List;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.babyfish.persistence.XEntityManager;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class MapTest extends AbstractTest {
    
    @BeforeClass
    public static void initData() {
        try (XEntityManager em = getEntityManagerFactory().createEntityManager()) {
            em.getTransaction().begin();
            try {
                em
                .createNativeQuery("INSERT INTO COMPUTER(COMPUTER_ID, COMPUTER_NAME) VALUES(:computerId, :computerName)")
                .setParameter("computerId", 1L)
                .setParameter("computerName", "Computer1")
                .executeUpdate();
                em
                .createNativeQuery("INSERT INTO CPU(CPU_ID, CPU_NAME, KEY_IN_COMPUTER, COMPUTER_ID) VALUES(:cpuId, :cpuName, :keyInComputer, :computerId)")
                .setParameter("cpuId", 1L)
                .setParameter("cpuName", "Intel Core-i5")
                .setParameter("keyInComputer", "first-cpu")
                .setParameter("computerId", 1L)
                .executeUpdate();
                em
                .createNativeQuery("INSERT INTO CPU(CPU_ID, CPU_NAME, KEY_IN_COMPUTER, COMPUTER_ID) VALUES(:cpuId, :cpuName, :keyInComputer, :computerId)")
                .setParameter("cpuId", 2L)
                .setParameter("cpuName", "Intel Core-i7")
                .setParameter("keyInComputer", "second-cpu")
                .setParameter("computerId", 1L)
                .executeUpdate();
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testQueryEntry() {
        XCriteriaBuilder cb = this.getEntityManager().getCriteriaBuilder();
        XCriteriaQuery<Entry<String, CPU>> cq = (XCriteriaQuery)cb.createQuery(Entry.class);
        XRoot<Computer> computer = cq.from(Computer.class);
        cq.where(cb.equal(computer.get(Computer_.id), 1L));
        cq.orderBy(cb.asc(computer.join(Computer_.cpus).key()));
        cq.select(computer.join(Computer_.cpus).entry());
        List<Entry<String, CPU>> entries = this.getEntityManager().createQuery(cq).getResultList();
        Assert.assertEquals(2, entries.size());
        Assert.assertEquals("first-cpu", entries.get(0).getKey());
        Assert.assertEquals(1L, entries.get(0).getValue().getId().longValue());
        Assert.assertEquals("Intel Core-i5", entries.get(0).getValue().getName());
        Assert.assertEquals("second-cpu", entries.get(1).getKey());
        Assert.assertEquals(2L, entries.get(1).getValue().getId().longValue());
        Assert.assertEquals("Intel Core-i7", entries.get(1).getValue().getName());
    }
    
    @Test
    public void testQueryKey() {
        XCriteriaBuilder cb = this.getEntityManager().getCriteriaBuilder();
        XCriteriaQuery<String> cq = cb.createQuery(String.class);
        XRoot<Computer> computer = cq.from(Computer.class);
        cq.where(cb.equal(computer.get(Computer_.id), 1L));
        cq.select(computer.join(Computer_.cpus).key());
        List<String> keys = this.getEntityManager().createQuery(cq).getResultList();
        Assert.assertEquals(2, keys.size());
        Assert.assertEquals("first-cpu", keys.get(0));
        Assert.assertEquals("second-cpu", keys.get(1));
    }
    
    @Test
    public void testQueryValue() {
        XCriteriaBuilder cb = this.getEntityManager().getCriteriaBuilder();
        XCriteriaQuery<CPU> cq = cb.createQuery(CPU.class);
        XRoot<Computer> computer = cq.from(Computer.class);
        cq.where(cb.equal(computer.get(Computer_.id), 1L));
        cq.select(computer.join(Computer_.cpus).value());
        List<CPU> values = this.getEntityManager().createQuery(cq).getResultList();
        Assert.assertEquals(2, values.size());
        Assert.assertEquals(1L, values.get(0).getId().longValue());
        Assert.assertEquals("Intel Core-i5", values.get(0).getName());
        Assert.assertEquals(2L, values.get(1).getId().longValue());
        Assert.assertEquals("Intel Core-i7", values.get(1).getName());
    }
}
