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

import org.babyfish.persistence.XEntityManager;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ListTest extends AbstractTest {

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
                .createNativeQuery("INSERT INTO MEMORY(MEMORY_ID, MEMORY_NAME, INDEX_IN_COMPUTER, COMPUTER_ID) VALUES(:memoryId, :memoryName, :indexInComputer, :computerId)")
                .setParameter("memoryId", 1L)
                .setParameter("memoryName", "Kingston DDR3 1600, 4GB")
                .setParameter("indexInComputer", 0L)
                .setParameter("computerId", 1L)
                .executeUpdate();
                em
                .createNativeQuery("INSERT INTO MEMORY(MEMORY_ID, MEMORY_NAME, INDEX_IN_COMPUTER, COMPUTER_ID) VALUES(:memoryId, :memoryName, :indexInComputer, :computerId)")
                .setParameter("memoryId", 2L)
                .setParameter("memoryName", "Kingston DDR3 1600, 8GB")
                .setParameter("indexInComputer", 1L)
                .setParameter("computerId", 1L)
                .executeUpdate();
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
        }
    }
    
    @Test
    public void testQueryIndex() {
        XCriteriaBuilder cb = this.getEntityManager().getCriteriaBuilder();
        XCriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
        XRoot<Computer> computer = cq.from(Computer.class);
        cq.where(cb.equal(computer.get(Computer_.id), 1L));
        cq.orderBy(cb.asc(computer.join(Computer_.memories).index()));
        cq.select(computer.join(Computer_.memories).index());
        List<Integer> indexes = this.getEntityManager().createQuery(cq).getResultList();
        Assert.assertEquals(2, indexes.size());
        Assert.assertEquals(0, indexes.get(0).intValue());
        Assert.assertEquals(1, indexes.get(1).intValue());
    }
}
