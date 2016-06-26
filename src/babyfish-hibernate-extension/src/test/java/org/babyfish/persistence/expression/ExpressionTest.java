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
package org.babyfish.persistence.expression;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.persistence.Tuple;

import junit.framework.Assert;

import org.babyfish.hibernate.jpa.HibernatePersistenceProvider;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XEntityManagerFactory;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfish.persistence.criteria.XRoot;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ExpressionTest {
    
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private static XEntityManagerFactory entityManagerFactory;
    
    private XEntityManager entityManager;
    
    @BeforeClass
    public static void initEntityManagerFactory() throws ParseException {
        entityManagerFactory =
                new HibernatePersistenceProvider(
                        ExpressionTest.class.getPackage().getName().replace('.', '/') + 
                        "/persistence.xml")
                .createEntityManagerFactory(null, null);
        try (XEntityManager em = entityManagerFactory.createEntityManager()) {
            try {
                em.getTransaction().begin();
                initData(em);
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
        }
    }
    
    private static void initData(XEntityManager em) throws ParseException {
        
        Product audiA8 = new Product();
        audiA8.setName("ADUI-A8");
        audiA8.setPrice(new BigDecimal(2648000));
        audiA8.setWeight(2185);
        
        Product benzS= new Product();
        benzS.setName("BENZ-S");
        benzS.setPrice(new BigDecimal(3618000));
        benzS.setWeight(2250);
        
        Product lamborghiniAventador = new Product();
        lamborghiniAventador.setName("Aventador LP700-4");
        lamborghiniAventador.setPrice(new BigDecimal(7388800));
        lamborghiniAventador.setWeight(1585);
        
        Product bugattiVeyron = new Product();
        bugattiVeyron.setName("Bugatti Veyron");
        bugattiVeyron.setPrice(new BigDecimal(35000000));
        bugattiVeyron.setWeight(1888);
        
        Customer jim = new Customer();
        jim.setName("Jim");
        jim.setAddress("Jim's house");
        
        Customer smith = new Customer();
        smith.setName("Smith");
        smith.setAddress("Smith's house");
        
        Customer howard = new Customer();
        howard.setName("Howard");
        
        SaleTransaction jimTransaction = new SaleTransaction();
        jimTransaction.setDate(DATE_FORMAT.parse("2013-01-01 13:21:26"));
        jimTransaction.setBuyer(jim);
        jimTransaction.setProduct(audiA8);
        jimTransaction.setProductTransactionPrice(new BigDecimal("2611000"));
        jimTransaction.setQuantity(4);
        
        SaleTransaction smithTransaction = new SaleTransaction();
        smithTransaction.setDate(DATE_FORMAT.parse("2013-01-19 15:21:26"));
        smithTransaction.setBuyer(smith);
        smithTransaction.setProduct(benzS);
        smithTransaction.setProductTransactionPrice(new BigDecimal("3582000"));
        smithTransaction.setQuantity(3);
        
        SaleTransaction howardTransaction1 = new SaleTransaction();
        howardTransaction1.setDate(DATE_FORMAT.parse("2013-02-28 17:43:07"));
        howardTransaction1.setBuyer(howard);
        howardTransaction1.setProduct(benzS);
        howardTransaction1.setProductTransactionPrice(new BigDecimal("3612000"));
        howardTransaction1.setQuantity(1);
        
        SaleTransaction howardTransaction2 = new SaleTransaction();
        howardTransaction2.setDate(DATE_FORMAT.parse("2013-03-01 10:31:58"));
        howardTransaction2.setBuyer(howard);
        howardTransaction2.setProduct(audiA8);
        howardTransaction2.setProductTransactionPrice(new BigDecimal("2620000"));
        howardTransaction2.setQuantity(2);
        
        SaleTransaction howardTransaction3 = new SaleTransaction();
        howardTransaction3.setDate(DATE_FORMAT.parse("2013-03-21 14:01:37"));
        howardTransaction3.setBuyer(howard);
        howardTransaction3.setProduct(audiA8);
        howardTransaction3.setProductTransactionPrice(new BigDecimal("1280000"));
        howardTransaction3.setQuantity(-1);
        
        SaleTransaction howardTransaction4 = new SaleTransaction();
        howardTransaction4.setDate(DATE_FORMAT.parse("2013-06-27 10:59:26"));
        howardTransaction4.setBuyer(howard);
        howardTransaction4.setProduct(lamborghiniAventador);
        howardTransaction4.setProductTransactionPrice(new BigDecimal("7354000"));
        howardTransaction4.setQuantity(1);
        
        em.persist(audiA8);
        em.persist(benzS);
        em.persist(lamborghiniAventador);
        em.persist(bugattiVeyron);
        
        em.persist(jim);
        em.persist(smith);
        em.persist(howard);
        
        em.persist(jimTransaction);
        em.persist(smithTransaction);
        em.persist(howardTransaction1);
        em.persist(howardTransaction2);
        em.persist(howardTransaction3);
        em.persist(howardTransaction4);
    }
    
    @AfterClass
    public static void disposeEntityManagerFactory() {
        XEntityManagerFactory emf = entityManagerFactory;
        if (emf != null) {
            entityManagerFactory = null;
            emf.close();
        }
    }
    
    @Before
    public void initEntityManager() {
        this.entityManager = entityManagerFactory.createEntityManager();
    }
    
    @After
    public void disposeEntityManager() {
        XEntityManager em = this.entityManager;
        if (em != null) {
            this.entityManager = null;
            em.close();
        }
    }
    
    protected static XEntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    protected XEntityManager getEntityManager() {
        if (this.entityManager == null) {
            throw new IllegalStateException();
        }
        return this.entityManager;
    }
    
    @Test
    public void testAbs() {
        XCriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createTupleQuery();
        XRoot<SaleTransaction> saleTransaction = cq.from(SaleTransaction.class);
        cq
        .multiselect(
                saleTransaction
                .alias("saleTransaction"),
                cb.abs(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.productTransactionPrice)
                        )
                )
                .alias("totalPrice"),
                cb.abs(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.join(SaleTransaction_.product).get(Product_.weight)
                        )
                )
                .alias("totalWeight")
        )
        .orderBy(
                cb.asc(saleTransaction.get(SaleTransaction_.id))
        );
        List<Tuple> tuples = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(6, tuples.size());
        
        Assert.assertEquals(1L, ((SaleTransaction)tuples.get(0).get("saleTransaction")).getId().longValue());
        Assert.assertEquals(2611000 * 4 + ".00", ((BigDecimal)tuples.get(0).get("totalPrice")).toString());
        Assert.assertEquals(2185F * 4, ((Float)tuples.get(0).get("totalWeight")).floatValue(), 1E-7F);
        
        Assert.assertEquals(2L, ((SaleTransaction)tuples.get(1).get("saleTransaction")).getId().longValue());
        Assert.assertEquals(3582000 * 3 + ".00", ((BigDecimal)tuples.get(1).get("totalPrice")).toString());
        Assert.assertEquals(2250F * 3, ((Float)tuples.get(1).get("totalWeight")).floatValue(), 1E-7F);
        
        Assert.assertEquals(3L, ((SaleTransaction)tuples.get(2).get("saleTransaction")).getId().longValue());
        Assert.assertEquals(3612000 * 1 + ".00", ((BigDecimal)tuples.get(2).get("totalPrice")).toString());
        Assert.assertEquals(2250F * 1, ((Float)tuples.get(2).get("totalWeight")).floatValue(), 1E-7F);
        
        Assert.assertEquals(4L, ((SaleTransaction)tuples.get(3).get("saleTransaction")).getId().longValue());
        Assert.assertEquals(2620000 * 2 + ".00", ((BigDecimal)tuples.get(3).get("totalPrice")).toString());
        Assert.assertEquals(2185F * 2, ((Float)tuples.get(3).get("totalWeight")).floatValue(), 1E-7F);
        
        Assert.assertEquals(5L, ((SaleTransaction)tuples.get(4).get("saleTransaction")).getId().longValue());
        Assert.assertEquals(1280000 * 1 + ".00", ((BigDecimal)tuples.get(4).get("totalPrice")).toString());
        Assert.assertEquals(2185F * 1, ((Float)tuples.get(4).get("totalWeight")).floatValue(), 1E-7F);
        
        Assert.assertEquals(6L, ((SaleTransaction)tuples.get(5).get("saleTransaction")).getId().longValue());
        Assert.assertEquals(7354000 * 1 + ".00", ((BigDecimal)tuples.get(5).get("totalPrice")).toString());
        Assert.assertEquals(1585F * 1, ((Float)tuples.get(5).get("totalWeight")).floatValue(), 1E-7F);
    }
    
    @Test
    public void testAggregation() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createTupleQuery();
        XRoot<SaleTransaction> saleTransaction = cq.from(SaleTransaction.class);
        cq
        .groupBy(
                saleTransaction.join(SaleTransaction_.buyer).get(Customer_.name),
                saleTransaction.join(SaleTransaction_.product).get(Product_.name)
        )
        .orderBy(
                cb.asc(saleTransaction.join(SaleTransaction_.buyer).get(Customer_.name)),
                cb.asc(saleTransaction.join(SaleTransaction_.product).get(Product_.name))
        )
        .multiselect(
                saleTransaction.join(SaleTransaction_.buyer).get(Customer_.name).alias("customer"),
                saleTransaction.join(SaleTransaction_.product).get(Product_.name).alias("product"),
                cb.count(saleTransaction).alias("count"),
                cb.countDistinct(saleTransaction).alias("distinctCount"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.productTransactionPrice)
                        )
                )
                .alias("totalPrice"),
                cb.sumAsLong(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.productTransactionPrice).as(Integer.class)
                        )
                )
                .alias("totalLongPrice"),
                cb.sumAsDouble(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity).as(Float.class),
                                saleTransaction.get(SaleTransaction_.productTransactionPrice).as(Float.class)
                        )
                )
                .alias("totalDoublePrice"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.join(SaleTransaction_.product).get(Product_.weight)
                        )
                )
                .alias("totalWeight"),
                cb.sumAsLong(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.join(SaleTransaction_.product).get(Product_.weight).as(Integer.class)
                        )
                )
                .alias("totalLongWeight"),
                cb.sumAsDouble(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity).as(Float.class),
                                saleTransaction.join(SaleTransaction_.product).get(Product_.weight)
                        )
                )
                .alias("totalDoubleWeight"),
                cb.min(saleTransaction.get(SaleTransaction_.productTransactionPrice))
                .alias("minPrice"),
                cb.max(saleTransaction.get(SaleTransaction_.productTransactionPrice))
                .alias("maxPrice"),
                cb.avg(saleTransaction.get(SaleTransaction_.productTransactionPrice))
                .alias("avgPrice")
        );
        
        List<Tuple> tuples = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(5, tuples.size());
        
        Assert.assertEquals("Howard", (String)tuples.get(0).get("customer"));
        Assert.assertEquals("ADUI-A8", (String)tuples.get(0).get("product"));
        Assert.assertEquals(2L, ((Long)tuples.get(0).get("count")).longValue());
        Assert.assertEquals(2L, ((Long)tuples.get(0).get("distinctCount")).longValue());
        Assert.assertEquals(2620000L * 2 - 1280000, ((BigDecimal)tuples.get(0).get("totalPrice")).longValue());
        Assert.assertEquals(2620000L * 2 - 1280000, ((Long)tuples.get(0).get("totalLongPrice")).longValue());
        Assert.assertEquals(2620000L * 2 - 1280000, ((Double)tuples.get(0).get("totalDoublePrice")).longValue());
        Assert.assertEquals(2185D, ((Double)tuples.get(0).get("totalWeight")).doubleValue());
        Assert.assertEquals(2185L, ((Long)tuples.get(0).get("totalLongWeight")).longValue());
        Assert.assertEquals(2185D, ((Double)tuples.get(0).get("totalDoubleWeight")).doubleValue());
        Assert.assertEquals(1280000L, ((BigDecimal)tuples.get(0).get("minPrice")).longValue());
        Assert.assertEquals(2620000L, ((BigDecimal)tuples.get(0).get("maxPrice")).longValue());
        Assert.assertEquals(1950000D, ((Double)tuples.get(0).get("avgPrice")).doubleValue(), 1E-6D);
        
        Assert.assertEquals("Howard", (String)tuples.get(1).get("customer"));
        Assert.assertEquals("Aventador LP700-4", (String)tuples.get(1).get("product"));
        Assert.assertEquals(1L, ((Long)tuples.get(1).get("count")).longValue());
        Assert.assertEquals(1L, ((Long)tuples.get(1).get("distinctCount")).longValue());
        Assert.assertEquals(7354000L, ((BigDecimal)tuples.get(1).get("totalPrice")).longValue());
        Assert.assertEquals(7354000L, ((Long)tuples.get(1).get("totalLongPrice")).longValue());
        Assert.assertEquals(7354000L, ((Double)tuples.get(1).get("totalDoublePrice")).longValue());
        Assert.assertEquals(1585D, ((Double)tuples.get(1).get("totalWeight")).doubleValue());
        Assert.assertEquals(1585L, ((Long)tuples.get(1).get("totalLongWeight")).longValue());
        Assert.assertEquals(1585D, ((Double)tuples.get(1).get("totalDoubleWeight")).doubleValue());
        Assert.assertEquals(7354000L, ((BigDecimal)tuples.get(1).get("minPrice")).longValue());
        Assert.assertEquals(7354000L, ((BigDecimal)tuples.get(1).get("maxPrice")).longValue());
        Assert.assertEquals(7354000D, ((Double)tuples.get(1).get("avgPrice")).doubleValue(), 1E-6D);
        
        Assert.assertEquals("Howard", (String)tuples.get(2).get("customer"));
        Assert.assertEquals("BENZ-S", (String)tuples.get(2).get("product"));
        Assert.assertEquals(1L, ((Long)tuples.get(2).get("count")).longValue());
        Assert.assertEquals(1L, ((Long)tuples.get(2).get("distinctCount")).longValue());
        Assert.assertEquals(3612000L, ((BigDecimal)tuples.get(2).get("totalPrice")).longValue());
        Assert.assertEquals(3612000L, ((Long)tuples.get(2).get("totalLongPrice")).longValue());
        Assert.assertEquals(3612000L, ((Double)tuples.get(2).get("totalDoublePrice")).longValue());
        Assert.assertEquals(2250D, ((Double)tuples.get(2).get("totalWeight")).doubleValue());
        Assert.assertEquals(2250L, ((Long)tuples.get(2).get("totalLongWeight")).longValue());
        Assert.assertEquals(2250D, ((Double)tuples.get(2).get("totalDoubleWeight")).doubleValue());
        Assert.assertEquals(3612000L, ((BigDecimal)tuples.get(2).get("minPrice")).longValue());
        Assert.assertEquals(3612000L, ((BigDecimal)tuples.get(2).get("maxPrice")).longValue());
        Assert.assertEquals(3612000D, ((Double)tuples.get(2).get("avgPrice")).doubleValue(), 1E-6D);
        
        Assert.assertEquals("Jim", (String)tuples.get(3).get("customer"));
        Assert.assertEquals("ADUI-A8", (String)tuples.get(3).get("product"));
        Assert.assertEquals(1L, ((Long)tuples.get(3).get("count")).longValue());
        Assert.assertEquals(1L, ((Long)tuples.get(3).get("distinctCount")).longValue());
        Assert.assertEquals(2611000L * 4, ((BigDecimal)tuples.get(3).get("totalPrice")).longValue());
        Assert.assertEquals(2611000L * 4, ((Long)tuples.get(3).get("totalLongPrice")).longValue());
        Assert.assertEquals(2611000L * 4, ((Double)tuples.get(3).get("totalDoublePrice")).longValue());
        Assert.assertEquals(2185D * 4, ((Double)tuples.get(3).get("totalWeight")).doubleValue());
        Assert.assertEquals(2185L * 4, ((Long)tuples.get(3).get("totalLongWeight")).longValue());
        Assert.assertEquals(2185D * 4, ((Double)tuples.get(3).get("totalDoubleWeight")).doubleValue());
        Assert.assertEquals(2611000L, ((BigDecimal)tuples.get(3).get("minPrice")).longValue());
        Assert.assertEquals(2611000L, ((BigDecimal)tuples.get(3).get("maxPrice")).longValue());
        Assert.assertEquals(2611000D, ((Double)tuples.get(3).get("avgPrice")).doubleValue(), 1E-6D);
        
        Assert.assertEquals("Smith", (String)tuples.get(4).get("customer"));
        Assert.assertEquals("BENZ-S", (String)tuples.get(4).get("product"));
        Assert.assertEquals(1L, ((Long)tuples.get(4).get("count")).longValue());
        Assert.assertEquals(1L, ((Long)tuples.get(4).get("distinctCount")).longValue());
        Assert.assertEquals(3582000L * 3, ((BigDecimal)tuples.get(4).get("totalPrice")).longValue());
        Assert.assertEquals(3582000L * 3, ((Long)tuples.get(4).get("totalLongPrice")).longValue());
        Assert.assertEquals(3582000L * 3, ((Double)tuples.get(4).get("totalDoublePrice")).longValue());
        Assert.assertEquals(2250D * 3, ((Double)tuples.get(4).get("totalWeight")).doubleValue());
        Assert.assertEquals(2250L * 3, ((Long)tuples.get(4).get("totalLongWeight")).longValue());
        Assert.assertEquals(2250D * 3, ((Double)tuples.get(4).get("totalDoubleWeight")).doubleValue());
        Assert.assertEquals(3582000L, ((BigDecimal)tuples.get(4).get("minPrice")).longValue());
        Assert.assertEquals(3582000L, ((BigDecimal)tuples.get(4).get("maxPrice")).longValue());
        Assert.assertEquals(3582000D, ((Double)tuples.get(4).get("avgPrice")).doubleValue(), 1E-6D);
        
        cq = cb.createTupleQuery();
        XRoot<Product> product = cq.from(Product.class);
        cq
        .multiselect(
                cb.least(product.get(Product_.name)).alias("min"),
                cb.greatest(product.get(Product_.name)).alias("max")
        );
        Tuple tuple = this.entityManager.createQuery(cq).getSingleResult();
        Assert.assertEquals("ADUI-A8", (String)tuple.get("min"));
        Assert.assertEquals("Bugatti Veyron", (String)tuple.get("max"));
    }
    
    @Test
    public void testAs() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createTupleQuery();
        XRoot<SaleTransaction> saleTransaction = cq.from(SaleTransaction.class);
        cq
        .groupBy(
                saleTransaction.join(SaleTransaction_.buyer).get(Customer_.name),
                saleTransaction.join(SaleTransaction_.product).get(Product_.name)
        )
        .orderBy(
                cb.asc(saleTransaction.join(SaleTransaction_.buyer).get(Customer_.name)),
                cb.asc(saleTransaction.join(SaleTransaction_.product).get(Product_.name))
        )
        .multiselect(
                saleTransaction.get(SaleTransaction_.buyer).get(Customer_.name).alias("customer"),
                saleTransaction.get(SaleTransaction_.product).get(Product_.name).alias("product"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.productTransactionPrice)
                        )
                )
                .as(int.class)
                .alias("totalIntPrice"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.productTransactionPrice)
                        )
                )
                .as(long.class)
                .alias("totalLongPrice"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.productTransactionPrice)
                        )
                )
                .as(float.class)
                .alias("totalFloatPrice"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.productTransactionPrice)
                        )
                )
                .as(double.class)
                .alias("totalDoublePrice"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.productTransactionPrice)
                        )
                )
                .as(Integer.class)
                .alias("totalNullableIntPrice"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.productTransactionPrice)
                        )
                )
                .as(Long.class)
                .alias("totalNullableLongPrice"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.productTransactionPrice)
                        )
                )
                .as(Float.class)
                .alias("totalNullableFloatPrice"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.productTransactionPrice)
                        )
                )
                .as(Double.class)
                .alias("totalNullableDoublePrice"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.productTransactionPrice)
                        )
                )
                .as(BigInteger.class)
                .alias("totalBigIntegerPrice"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.productTransactionPrice)
                        )
                )
                .as(BigDecimal.class)
                .alias("totalBigDecimalPrice"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.product).get(Product_.weight)
                        )
                )
                .as(short.class)
                .alias("totalShortWeight"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.product).get(Product_.weight)
                        )
                )
                .as(int.class)
                .alias("totalIntWeight"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.product).get(Product_.weight)
                        )
                )
                .as(long.class)
                .alias("totalLongWeight"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.product).get(Product_.weight)
                        )
                )
                .as(float.class)
                .alias("totalFloatWeight"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.product).get(Product_.weight)
                        )
                )
                .as(double.class)
                .alias("totalDoubleWeight"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.product).get(Product_.weight)
                        )
                )
                .as(Short.class)
                .alias("totalNullableShortWeight"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.product).get(Product_.weight)
                        )
                )
                .as(Integer.class)
                .alias("totalNullableIntWeight"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.product).get(Product_.weight)
                        )
                )
                .as(Long.class)
                .alias("totalNullableLongWeight"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.product).get(Product_.weight)
                        )
                )
                .as(Float.class)
                .alias("totalNullableFloatWeight"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.product).get(Product_.weight)
                        )
                )
                .as(Double.class)
                .alias("totalNullableDoubleWeight"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.product).get(Product_.weight)
                        )
                )
                .as(BigInteger.class)
                .alias("totalBigIntegerWeight"),
                cb.sum(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.quantity),
                                saleTransaction.get(SaleTransaction_.product).get(Product_.weight)
                        )
                )
                .as(BigDecimal.class)
                .alias("totalBigDecimalWeight")
        );
        
        List<Tuple> tuples = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(5, tuples.size());
        
        Assert.assertEquals("Howard", (String)tuples.get(0).get("customer"));
        Assert.assertEquals("ADUI-A8", (String)tuples.get(0).get("product"));
        Assert.assertEquals(2620000 * 2 - 1280000, ((Integer)tuples.get(0).get("totalIntPrice")).intValue());
        Assert.assertEquals(2620000L * 2 - 1280000, ((Long)tuples.get(0).get("totalLongPrice")).longValue());
        Assert.assertEquals(2620000F * 2 - 1280000, ((Float)tuples.get(0).get("totalFloatPrice")).floatValue());
        Assert.assertEquals(2620000D * 2 - 1280000, ((Double)tuples.get(0).get("totalDoublePrice")).doubleValue(), 1E-6D);
        Assert.assertEquals(2620000 * 2 - 1280000, ((Integer)tuples.get(0).get("totalNullableIntPrice")).intValue());
        Assert.assertEquals(2620000L * 2 - 1280000, ((Long)tuples.get(0).get("totalNullableLongPrice")).longValue());
        Assert.assertEquals(2620000F * 2 - 1280000, ((Float)tuples.get(0).get("totalNullableFloatPrice")).floatValue());
        Assert.assertEquals(2620000D * 2 - 1280000, ((Double)tuples.get(0).get("totalNullableDoublePrice")).doubleValue(), 1E-6D);
        Assert.assertEquals(2620000L * 2 - 1280000, ((BigInteger)tuples.get(0).get("totalBigIntegerPrice")).longValue());
        Assert.assertEquals(2620000L * 2 - 1280000, ((BigDecimal)tuples.get(0).get("totalBigDecimalPrice")).longValue());
        Assert.assertEquals((short)2185, ((Short)tuples.get(0).get("totalShortWeight")).shortValue());
        Assert.assertEquals(2185, ((Integer)tuples.get(0).get("totalIntWeight")).intValue());
        Assert.assertEquals(2185L, ((Long)tuples.get(0).get("totalLongWeight")).longValue());
        Assert.assertEquals(2185F, ((Float)tuples.get(0).get("totalFloatWeight")).floatValue());
        Assert.assertEquals(2185D, ((Double)tuples.get(0).get("totalDoubleWeight")).doubleValue(), 1E-6D);
        Assert.assertEquals((short)2185, ((Short)tuples.get(0).get("totalNullableShortWeight")).shortValue());
        Assert.assertEquals(2185, ((Integer)tuples.get(0).get("totalNullableIntWeight")).intValue());
        Assert.assertEquals(2185L, ((Long)tuples.get(0).get("totalNullableLongWeight")).longValue());
        Assert.assertEquals(2185F, ((Float)tuples.get(0).get("totalNullableFloatWeight")).floatValue());
        Assert.assertEquals(2185D, ((Double)tuples.get(0).get("totalNullableDoubleWeight")).doubleValue(), 1E-6D);
        Assert.assertEquals(2185L, ((BigInteger)tuples.get(0).get("totalBigIntegerWeight")).longValue());
        Assert.assertEquals(2185L, ((BigDecimal)tuples.get(0).get("totalBigDecimalWeight")).longValue());
        
        Assert.assertEquals("Howard", (String)tuples.get(1).get("customer"));
        Assert.assertEquals("Aventador LP700-4", (String)tuples.get(1).get("product"));
        Assert.assertEquals(7354000, ((Integer)tuples.get(1).get("totalIntPrice")).intValue());
        Assert.assertEquals(7354000L, ((Long)tuples.get(1).get("totalLongPrice")).longValue());
        Assert.assertEquals(7354000F, ((Float)tuples.get(1).get("totalFloatPrice")).floatValue());
        Assert.assertEquals(7354000D, ((Double)tuples.get(1).get("totalDoublePrice")).doubleValue(), 1E-6D);
        Assert.assertEquals(7354000, ((Integer)tuples.get(1).get("totalNullableIntPrice")).intValue());
        Assert.assertEquals(7354000L, ((Long)tuples.get(1).get("totalNullableLongPrice")).longValue());
        Assert.assertEquals(7354000F, ((Float)tuples.get(1).get("totalNullableFloatPrice")).floatValue());
        Assert.assertEquals(7354000D, ((Double)tuples.get(1).get("totalNullableDoublePrice")).doubleValue(), 1E-6D);
        Assert.assertEquals(7354000L, ((BigInteger)tuples.get(1).get("totalBigIntegerPrice")).longValue());
        Assert.assertEquals(7354000L, ((BigDecimal)tuples.get(1).get("totalBigDecimalPrice")).longValue());
        Assert.assertEquals((short)1585, ((Short)tuples.get(1).get("totalShortWeight")).shortValue());
        Assert.assertEquals(1585, ((Integer)tuples.get(1).get("totalIntWeight")).intValue());
        Assert.assertEquals(1585L, ((Long)tuples.get(1).get("totalLongWeight")).longValue());
        Assert.assertEquals(1585F, ((Float)tuples.get(1).get("totalFloatWeight")).floatValue());
        Assert.assertEquals(1585D, ((Double)tuples.get(1).get("totalDoubleWeight")).doubleValue(), 1E-6D);
        Assert.assertEquals((short)1585, ((Short)tuples.get(1).get("totalNullableShortWeight")).shortValue());
        Assert.assertEquals(1585, ((Integer)tuples.get(1).get("totalNullableIntWeight")).intValue());
        Assert.assertEquals(1585L, ((Long)tuples.get(1).get("totalNullableLongWeight")).longValue());
        Assert.assertEquals(1585F, ((Float)tuples.get(1).get("totalNullableFloatWeight")).floatValue());
        Assert.assertEquals(1585D, ((Double)tuples.get(1).get("totalNullableDoubleWeight")).doubleValue(), 1E-6D);
        Assert.assertEquals(1585L, ((BigInteger)tuples.get(1).get("totalBigIntegerWeight")).longValue());
        Assert.assertEquals(1585L, ((BigDecimal)tuples.get(1).get("totalBigDecimalWeight")).longValue());
        
        Assert.assertEquals("Howard", (String)tuples.get(2).get("customer"));
        Assert.assertEquals("BENZ-S", (String)tuples.get(2).get("product"));
        Assert.assertEquals(3612000, ((Integer)tuples.get(2).get("totalIntPrice")).intValue());
        Assert.assertEquals(3612000L, ((Long)tuples.get(2).get("totalLongPrice")).longValue());
        Assert.assertEquals(3612000F, ((Float)tuples.get(2).get("totalFloatPrice")).floatValue());
        Assert.assertEquals(3612000D, ((Double)tuples.get(2).get("totalDoublePrice")).doubleValue(), 1E-6D);
        Assert.assertEquals(3612000, ((Integer)tuples.get(2).get("totalNullableIntPrice")).intValue());
        Assert.assertEquals(3612000L, ((Long)tuples.get(2).get("totalNullableLongPrice")).longValue());
        Assert.assertEquals(3612000F, ((Float)tuples.get(2).get("totalNullableFloatPrice")).floatValue());
        Assert.assertEquals(3612000D, ((Double)tuples.get(2).get("totalNullableDoublePrice")).doubleValue(), 1E-6D);
        Assert.assertEquals(3612000L, ((BigInteger)tuples.get(2).get("totalBigIntegerPrice")).longValue());
        Assert.assertEquals(3612000L, ((BigDecimal)tuples.get(2).get("totalBigDecimalPrice")).longValue());
        Assert.assertEquals((short)2250, ((Short)tuples.get(2).get("totalShortWeight")).shortValue());
        Assert.assertEquals(2250, ((Integer)tuples.get(2).get("totalIntWeight")).intValue());
        Assert.assertEquals(2250L, ((Long)tuples.get(2).get("totalLongWeight")).longValue());
        Assert.assertEquals(2250F, ((Float)tuples.get(2).get("totalFloatWeight")).floatValue());
        Assert.assertEquals(2250D, ((Double)tuples.get(2).get("totalDoubleWeight")).doubleValue(), 1E-6D);
        Assert.assertEquals((short)2250, ((Short)tuples.get(2).get("totalNullableShortWeight")).shortValue());
        Assert.assertEquals(2250, ((Integer)tuples.get(2).get("totalNullableIntWeight")).intValue());
        Assert.assertEquals(2250L, ((Long)tuples.get(2).get("totalNullableLongWeight")).longValue());
        Assert.assertEquals(2250F, ((Float)tuples.get(2).get("totalNullableFloatWeight")).floatValue());
        Assert.assertEquals(2250D, ((Double)tuples.get(2).get("totalNullableDoubleWeight")).doubleValue(), 1E-6D);
        Assert.assertEquals(2250L, ((BigInteger)tuples.get(2).get("totalBigIntegerWeight")).longValue());
        Assert.assertEquals(2250L, ((BigDecimal)tuples.get(2).get("totalBigDecimalWeight")).longValue());
        
        Assert.assertEquals("Jim", (String)tuples.get(3).get("customer"));
        Assert.assertEquals("ADUI-A8", (String)tuples.get(3).get("product"));
        Assert.assertEquals(2611000 * 4, ((Integer)tuples.get(3).get("totalIntPrice")).intValue());
        Assert.assertEquals(2611000L * 4, ((Long)tuples.get(3).get("totalLongPrice")).longValue());
        Assert.assertEquals(2611000F * 4, ((Float)tuples.get(3).get("totalFloatPrice")).floatValue());
        Assert.assertEquals(2611000D * 4, ((Double)tuples.get(3).get("totalDoublePrice")).doubleValue(), 1E-6D);
        Assert.assertEquals(2611000 * 4, ((Integer)tuples.get(3).get("totalNullableIntPrice")).intValue());
        Assert.assertEquals(2611000L * 4, ((Long)tuples.get(3).get("totalNullableLongPrice")).longValue());
        Assert.assertEquals(2611000F * 4, ((Float)tuples.get(3).get("totalNullableFloatPrice")).floatValue());
        Assert.assertEquals(2611000D * 4, ((Double)tuples.get(3).get("totalNullableDoublePrice")).doubleValue(), 1E-6D);
        Assert.assertEquals(2611000L * 4, ((BigInteger)tuples.get(3).get("totalBigIntegerPrice")).longValue());
        Assert.assertEquals(2611000L * 4, ((BigDecimal)tuples.get(3).get("totalBigDecimalPrice")).longValue());
        Assert.assertEquals((short)(2185 * 4), ((Short)tuples.get(3).get("totalShortWeight")).shortValue());
        Assert.assertEquals(2185 * 4, ((Integer)tuples.get(3).get("totalIntWeight")).intValue());
        Assert.assertEquals(2185L * 4, ((Long)tuples.get(3).get("totalLongWeight")).longValue());
        Assert.assertEquals(2185F * 4, ((Float)tuples.get(3).get("totalFloatWeight")).floatValue());
        Assert.assertEquals(2185D * 4, ((Double)tuples.get(3).get("totalDoubleWeight")).doubleValue(), 1E-6D);
        Assert.assertEquals((short)(2185 * 4), ((Short)tuples.get(3).get("totalNullableShortWeight")).shortValue());
        Assert.assertEquals(2185 * 4, ((Integer)tuples.get(3).get("totalNullableIntWeight")).intValue());
        Assert.assertEquals(2185L * 4, ((Long)tuples.get(3).get("totalNullableLongWeight")).longValue());
        Assert.assertEquals(2185F * 4, ((Float)tuples.get(3).get("totalNullableFloatWeight")).floatValue());
        Assert.assertEquals(2185D * 4, ((Double)tuples.get(3).get("totalNullableDoubleWeight")).doubleValue(), 1E-6D);
        Assert.assertEquals(2185L * 4, ((BigInteger)tuples.get(3).get("totalBigIntegerWeight")).longValue());
        Assert.assertEquals(2185L * 4, ((BigDecimal)tuples.get(3).get("totalBigDecimalWeight")).longValue());
        
        Assert.assertEquals("Smith", (String)tuples.get(4).get("customer"));
        Assert.assertEquals("BENZ-S", (String)tuples.get(4).get("product"));
        Assert.assertEquals(3582000 * 3, ((Integer)tuples.get(4).get("totalIntPrice")).intValue());
        Assert.assertEquals(3582000L * 3, ((Long)tuples.get(4).get("totalLongPrice")).longValue());
        Assert.assertEquals(3582000F * 3, ((Float)tuples.get(4).get("totalFloatPrice")).floatValue());
        Assert.assertEquals(3582000D * 3, ((Double)tuples.get(4).get("totalDoublePrice")).doubleValue(), 1E-6D);
        Assert.assertEquals(3582000 * 3, ((Integer)tuples.get(4).get("totalNullableIntPrice")).intValue());
        Assert.assertEquals(3582000L * 3, ((Long)tuples.get(4).get("totalNullableLongPrice")).longValue());
        Assert.assertEquals(3582000F * 3, ((Float)tuples.get(4).get("totalNullableFloatPrice")).floatValue());
        Assert.assertEquals(3582000D * 3, ((Double)tuples.get(4).get("totalNullableDoublePrice")).doubleValue(), 1E-6D);
        Assert.assertEquals(3582000L * 3, ((BigInteger)tuples.get(4).get("totalBigIntegerPrice")).longValue());
        Assert.assertEquals(3582000L * 3, ((BigDecimal)tuples.get(4).get("totalBigDecimalPrice")).longValue());
        Assert.assertEquals((short)(2250 * 3), ((Short)tuples.get(4).get("totalShortWeight")).shortValue());
        Assert.assertEquals(2250 * 3, ((Integer)tuples.get(4).get("totalIntWeight")).intValue());
        Assert.assertEquals(2250L * 3, ((Long)tuples.get(4).get("totalLongWeight")).longValue());
        Assert.assertEquals(2250F * 3, ((Float)tuples.get(4).get("totalFloatWeight")).floatValue());
        Assert.assertEquals(2250D * 3, ((Double)tuples.get(4).get("totalDoubleWeight")).doubleValue(), 1E-6D);
        Assert.assertEquals((short)(2250 * 3), ((Short)tuples.get(4).get("totalNullableShortWeight")).shortValue());
        Assert.assertEquals(2250 * 3, ((Integer)tuples.get(4).get("totalNullableIntWeight")).intValue());
        Assert.assertEquals(2250L * 3, ((Long)tuples.get(4).get("totalNullableLongWeight")).longValue());
        Assert.assertEquals(2250F * 3, ((Float)tuples.get(4).get("totalNullableFloatWeight")).floatValue());
        Assert.assertEquals(2250D * 3, ((Double)tuples.get(4).get("totalNullableDoubleWeight")).doubleValue(), 1E-6D);
        Assert.assertEquals(2250L * 3, ((BigInteger)tuples.get(4).get("totalBigIntegerWeight")).longValue());
        Assert.assertEquals(2250L * 3, ((BigDecimal)tuples.get(4).get("totalBigDecimalWeight")).longValue());
    }
    
    @Test
    public void testConvert() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<BigDecimal> cq = cb.createQuery(BigDecimal.class);
        XRoot<SaleTransaction> saleTransaction = cq.from(SaleTransaction.class);
        cq
        .select(
                cb.toBigDecimal(
                    cb.sum(
                            cb.prod(
                                    saleTransaction.get(SaleTransaction_.productTransactionPrice),
                                    saleTransaction.get(SaleTransaction_.quantity)
                            )
                    )
                )
        );
        BigDecimal result = this.entityManager.createQuery(cq).getSingleResult();
        Assert.assertEquals(36116000, result.longValue());
    }
    
    @Test
    public void testBetween() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        
        XCriteriaQuery<SaleTransaction> stcq = cb.createQuery(SaleTransaction.class);
        XRoot<SaleTransaction> st = stcq.from(SaleTransaction.class);
        stcq.where(
                cb.between(
                    cb.prod(
                            st.get(SaleTransaction_.productTransactionPrice), 
                            st.get(SaleTransaction_.quantity)
                    ).as(BigDecimal.class),
                    new BigDecimal(6000000),
                    new BigDecimal(8000000)
                )
        );
        SaleTransaction saleTransaction = 
                this
                .entityManager
                .createQuery(stcq)
                .setQueryPaths(
                        SaleTransaction__.begin().buyer().end(),
                        SaleTransaction__.begin().product().end()
                )
                .getSingleResult();
        Assert.assertEquals("Howard", saleTransaction.getBuyer().getName());
        Assert.assertEquals("Aventador LP700-4", saleTransaction.getProduct().getName());
        
        XCriteriaQuery<Product> pcq;
        XRoot<Product> p;
        List<Product> products;
        
        pcq = cb.createQuery(Product.class);
        p = pcq.from(Product.class);
        pcq
        .where(cb.between(p.get(Product_.weight), 1800F, 2200F))
        .orderBy(cb.asc(p.get(Product_.name)));
        products = this.entityManager.createQuery(pcq).getResultList();
        Assert.assertEquals("ADUI-A8", products.get(0).getName());
        Assert.assertEquals("Bugatti Veyron", products.get(1).getName());
        
        pcq = cb.createQuery(Product.class);
        p = pcq.from(Product.class);
        pcq
        .where(cb.between(p.get(Product_.weight), 1800F, 2200F).not())
        .orderBy(cb.asc(p.get(Product_.name)));
        products = this.entityManager.createQuery(pcq).getResultList();
        Assert.assertEquals(2, products.size());
        Assert.assertEquals("Aventador LP700-4", products.get(0).getName());
        Assert.assertEquals("BENZ-S", products.get(1).getName());
    }
    
    @Test
    public void testBinaryArithmeticExpression() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        
        XCriteriaQuery<Tuple> cq = cb.createTupleQuery();
        XRoot<SaleTransaction> saleTransaction = cq.from(SaleTransaction.class);
        cq
        .orderBy(
                cb.asc(saleTransaction.join(SaleTransaction_.buyer).get(Customer_.name)),
                cb.asc(saleTransaction.join(SaleTransaction_.product).get(Product_.name)),
                cb.asc(saleTransaction.get(SaleTransaction_.date))
        )
        .multiselect(
                saleTransaction.join(SaleTransaction_.buyer).get(Customer_.name).alias("customer"),
                saleTransaction.join(SaleTransaction_.product).get(Product_.name).alias("product"),
                cb.diff(
                        saleTransaction.get(SaleTransaction_.productTransactionPrice), 
                        saleTransaction.get(SaleTransaction_.product).get(Product_.price)
                ).alias("diff"),
                cb.diff(
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.productTransactionPrice),
                                saleTransaction.get(SaleTransaction_.quantity)
                        ).as(BigDecimal.class), 
                        cb.prod(
                                saleTransaction.get(SaleTransaction_.product).get(Product_.price),
                                saleTransaction.get(SaleTransaction_.quantity)
                        ).as(BigDecimal.class)
                ).alias("totalDiff")
        );
        List<Tuple> tuples = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(6, tuples.size());
        
        Assert.assertEquals("Howard", (String)tuples.get(0).get("customer"));
        Assert.assertEquals("ADUI-A8", (String)tuples.get(0).get("product"));
        Assert.assertEquals(-28000L, ((BigDecimal)tuples.get(0).get("diff")).longValue());
        Assert.assertEquals(-56000L, ((BigDecimal)tuples.get(0).get("totalDiff")).longValue());
        
        Assert.assertEquals("Howard", (String)tuples.get(1).get("customer"));
        Assert.assertEquals("ADUI-A8", (String)tuples.get(1).get("product"));
        Assert.assertEquals(-1368000L, ((BigDecimal)tuples.get(1).get("diff")).longValue());
        Assert.assertEquals(1368000L, ((BigDecimal)tuples.get(1).get("totalDiff")).longValue());
        
        Assert.assertEquals("Howard", (String)tuples.get(2).get("customer"));
        Assert.assertEquals("Aventador LP700-4", (String)tuples.get(2).get("product"));
        Assert.assertEquals(-34800L, ((BigDecimal)tuples.get(2).get("diff")).longValue());
        Assert.assertEquals(-34800L, ((BigDecimal)tuples.get(2).get("totalDiff")).longValue());
        
        Assert.assertEquals("Howard", (String)tuples.get(3).get("customer"));
        Assert.assertEquals("BENZ-S", (String)tuples.get(3).get("product"));
        Assert.assertEquals(-6000L, ((BigDecimal)tuples.get(3).get("diff")).longValue());
        Assert.assertEquals(-6000L, ((BigDecimal)tuples.get(3).get("totalDiff")).longValue());
        
        Assert.assertEquals("Jim", (String)tuples.get(4).get("customer"));
        Assert.assertEquals("ADUI-A8", (String)tuples.get(4).get("product"));
        Assert.assertEquals(-37000L, ((BigDecimal)tuples.get(4).get("diff")).longValue());
        Assert.assertEquals(-148000L, ((BigDecimal)tuples.get(4).get("totalDiff")).longValue());
        
        Assert.assertEquals("Smith", (String)tuples.get(5).get("customer"));
        Assert.assertEquals("BENZ-S", (String)tuples.get(5).get("product"));
        Assert.assertEquals(-36000L, ((BigDecimal)tuples.get(5).get("diff")).longValue());
        Assert.assertEquals(-108000L, ((BigDecimal)tuples.get(5).get("totalDiff")).longValue());
    }
    
    @Test
    public void testCoalesce() {
        XCriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        XCriteriaQuery<Object[]> cq;
        XRoot<Customer> customer;
        List<Object[]> rows;
        
        cq = cb.createQuery(Object[].class);
        customer = cq.from(Customer.class);
        cq
        .orderBy(cb.asc(customer.get(Customer_.name)))
        .multiselect(
                customer.get(Customer_.name),
                cb.<String>coalesce().value(customer.get(Customer_.address)).value("Unknown")
        );
        rows = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(3, rows.size());
        Assert.assertEquals("Howard", rows.get(0)[0]);
        Assert.assertEquals("Unknown", rows.get(0)[1]);
        Assert.assertEquals("Jim", rows.get(1)[0]);
        Assert.assertEquals("Jim's house", rows.get(1)[1]);
        Assert.assertEquals("Smith", rows.get(2)[0]);
        Assert.assertEquals("Smith's house", rows.get(2)[1]);
        
        cq = cb.createQuery(Object[].class);
        customer = cq.from(Customer.class);
        cq
        .orderBy(cb.asc(customer.get(Customer_.name)))
        .multiselect(
                customer.get(Customer_.name),
                cb.coalesce(customer.get(Customer_.address), "Unknown")
        );
        rows = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(3, rows.size());
        Assert.assertEquals("Howard", rows.get(0)[0]);
        Assert.assertEquals("Unknown", rows.get(0)[1]);
        Assert.assertEquals("Jim", rows.get(1)[0]);
        Assert.assertEquals("Jim's house", rows.get(1)[1]);
        Assert.assertEquals("Smith", rows.get(2)[0]);
        Assert.assertEquals("Smith's house", rows.get(2)[1]);
    }
    
    @Test
    public void testNullif() {
        XCriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        
        XCriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        XRoot<Customer> customer = cq.from(Customer.class);
        cq
        .orderBy(cb.asc(customer.get(Customer_.name)))
        .multiselect(
                customer.get(Customer_.name),
                cb.nullif(customer.get(Customer_.name), "Howard")
        );
        List<Object[]> rows = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(3, rows.size());
        Assert.assertEquals("Howard", rows.get(0)[0]);
        Assert.assertNull(rows.get(0)[1]);
        Assert.assertEquals("Jim", rows.get(1)[0]);
        Assert.assertEquals("Jim", rows.get(1)[1]);
        Assert.assertEquals("Smith", rows.get(2)[0]);
        Assert.assertEquals("Smith", rows.get(2)[1]);
    }
    
    @Test
    public void testSimpleCaseExpression() {
        XCriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        XCriteriaQuery<Object[]> cq;
        List<Object[]> rows;
        
        cq = cb.createQuery(Object[].class);
        XRoot<Customer> customer = cq.from(Customer.class);
        cq
        .orderBy(cb.asc(customer.get(Customer_.name)))
        .multiselect(
                customer.get(Customer_.name),
                cb
                .selectCase(customer.get(Customer_.address))
                .when("Jim's house", cb.trim(cb.literal("The house of Jim")))
                .when("Smith's house", cb.trim(cb.literal("The house of Smith")))
                .otherwise(cb.trim(cb.literal("The house whose owner is neither Jim nor Smith")))
        );
        rows = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(3, rows.size());
        Assert.assertEquals("Howard", rows.get(0)[0]);
        Assert.assertEquals("The house whose owner is neither Jim nor Smith", rows.get(0)[1]);
        Assert.assertEquals("Jim", rows.get(1)[0]);
        Assert.assertEquals("The house of Jim", rows.get(1)[1]);
        Assert.assertEquals("Smith", rows.get(2)[0]);
        Assert.assertEquals("The house of Smith", rows.get(2)[1]);
        
        cq = cb.createQuery(Object[].class);
        XRoot<Product> product = cq.from(Product.class);
        cq
        .orderBy(cb.asc(product.get(Product_.name)))
        .multiselect(
                product.get(Product_.name),
                cb
                .<BigDecimal, Long>selectCase(product.get(Product_.price))
                .when(new BigDecimal(2648000), 2640000L)
                .when(new BigDecimal(3618000), 3610000L)
                .otherwise(8000000L)
        );
        rows = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(4, rows.size());
        Assert.assertEquals("ADUI-A8", rows.get(0)[0]);
        Assert.assertEquals(2640000L, ((Long)rows.get(0)[1]).longValue());
        Assert.assertEquals("Aventador LP700-4", rows.get(1)[0]);
        Assert.assertEquals(8000000L, ((Long)rows.get(1)[1]).longValue());
        Assert.assertEquals("BENZ-S", rows.get(2)[0]);
        Assert.assertEquals(3610000L, ((Long)rows.get(2)[1]).longValue());
        Assert.assertEquals("Bugatti Veyron", rows.get(3)[0]);
        Assert.assertEquals(8000000L, ((Long)rows.get(3)[1]).longValue());
    }
    
    @Test
    public void testSearchedCaseExpression() {
        XCriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        XCriteriaQuery<Object[]> cq;
        List<Object[]> rows;
        
        cq = cb.createQuery(Object[].class);
        XRoot<Customer> customer = cq.from(Customer.class);
        cq
        .orderBy(cb.asc(customer.get(Customer_.name)))
        .multiselect(
                customer.get(Customer_.name),
                cb
                .<String>selectCase()
                .when(
                        cb.like(customer.get(Customer_.address), "%Jim%"),
                        cb.trim(cb.literal("The house of Jim"))
                )
                .when(
                        cb.like(customer.get(Customer_.address), "%Smith%"), 
                        cb.trim(cb.literal("The house of Smith"))
                )
                .otherwise(cb.trim(cb.literal("The house whose owner is neither Jim nor Smith")))
        );
        rows = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(3, rows.size());
        Assert.assertEquals("Howard", rows.get(0)[0]);
        Assert.assertEquals("The house whose owner is neither Jim nor Smith", rows.get(0)[1]);
        Assert.assertEquals("Jim", rows.get(1)[0]);
        Assert.assertEquals("The house of Jim", rows.get(1)[1]);
        Assert.assertEquals("Smith", rows.get(2)[0]);
        Assert.assertEquals("The house of Smith", rows.get(2)[1]);
        
        cq = cb.createQuery(Object[].class);
        XRoot<Product> product = cq.from(Product.class);
        cq
        .orderBy(cb.asc(product.get(Product_.name)))
        .multiselect(
                product.get(Product_.name),
                cb
                .<Long>selectCase()
                .when(
                        cb.between(
                                product.get(Product_.price), 
                                new BigDecimal(2000000), 
                                new BigDecimal(3000000)
                        ), 
                        2640000L
                )
                .when(
                        cb.between(
                                product.get(Product_.price), 
                                new BigDecimal(3000000), 
                                new BigDecimal(4000000)
                        ), 
                        3610000L
                )
                .otherwise(8000000L)
        );
        rows = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(4, rows.size());
        Assert.assertEquals("ADUI-A8", rows.get(0)[0]);
        Assert.assertEquals(2640000L, ((Long)rows.get(0)[1]).longValue());
        Assert.assertEquals("Aventador LP700-4", rows.get(1)[0]);
        Assert.assertEquals(8000000L, ((Long)rows.get(1)[1]).longValue());
        Assert.assertEquals("BENZ-S", rows.get(2)[0]);
        Assert.assertEquals(3610000L, ((Long)rows.get(2)[1]).longValue());
        Assert.assertEquals("Bugatti Veyron", rows.get(3)[0]);
        Assert.assertEquals(8000000L, ((Long)rows.get(3)[1]).longValue());
    }
    
    @Test
    public void testIsEmpty() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<String> cq;
        XRoot<Product> product;
        
        cq = cb.createQuery(String.class);
        product = cq.from(Product.class);
        cq
        .where(cb.isEmpty(product.get(Product_.transactions)))
        .select(product.get(Product_.name).alias("name"));
        String name = this.entityManager.createQuery(cq).getSingleResult();
        Assert.assertEquals("Bugatti Veyron", name);
        
        cq = cb.createQuery(String.class);
        product = cq.from(Product.class);
        cq
        .where(cb.isEmpty(product.get(Product_.transactions)).not())
        .orderBy(cb.asc(product.get(Product_.name)))
        .select(product.get(Product_.name).alias("name"));
        List<String> names = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(3, names.size());
        Assert.assertEquals("ADUI-A8", names.get(0));
        Assert.assertEquals("Aventador LP700-4", names.get(1));
        Assert.assertEquals("BENZ-S", names.get(2));
    }
    
    @Test
    public void testSize() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<String> cq;
        XRoot<Product> product;
        
        cq = cb.createQuery(String.class);
        product = cq.from(Product.class);
        cq
        .where(cb.equal(cb.size(product.get(Product_.transactions)), 0))
        .select(product.get(Product_.name).alias("name"));
        String name = this.entityManager.createQuery(cq).getSingleResult();
        Assert.assertEquals("Bugatti Veyron", name);
        
        cq = cb.createQuery(String.class);
        product = cq.from(Product.class);
        cq
        .where(cb.equal(cb.size(product.get(Product_.transactions)), 0).not())
        .orderBy(cb.asc(product.get(Product_.name)))
        .select(product.get(Product_.name).alias("name"));
        List<String> names = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(3, names.size());
        Assert.assertEquals("ADUI-A8", names.get(0));
        Assert.assertEquals("Aventador LP700-4", names.get(1));
        Assert.assertEquals("BENZ-S", names.get(2));
    }
    
    @Test
    public void testCompoundPredicate() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Product> cq;
        XRoot<Product> product;
        List<Product> products;
        
        cq = cb.createQuery(Product.class);
        product = cq.from(Product.class);
        cq
        .where(
                cb.and(
                    cb.ge(product.get(Product_.weight), 1800F),
                    cb.le(product.get(Product_.weight), 2200F)
                )
        )
        .orderBy(cb.asc(product.get(Product_.name)));
        products = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(2, products.size());
        Assert.assertEquals("ADUI-A8", products.get(0).getName());
        Assert.assertEquals("Bugatti Veyron", products.get(1).getName());
        
        cq = cb.createQuery(Product.class);
        product = cq.from(Product.class);
        cq
        .where(
                cb.not(
                        cb.and(
                            cb.ge(product.get(Product_.weight), 1800F),
                            cb.le(product.get(Product_.weight), 2200F)
                        )
                )
        )
        .orderBy(cb.asc(product.get(Product_.name)));
        products = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(2, products.size());
        Assert.assertEquals("Aventador LP700-4", products.get(0).getName());
        Assert.assertEquals("BENZ-S", products.get(1).getName());
    }
    
    @Test
    public void testNullness() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<String> cq;
        XRoot<Customer> customer;
        List<String> names;
        
        cq = cb.createQuery(String.class);
        customer = cq.from(Customer.class);
        cq
        .orderBy(cb.asc(customer.get(Customer_.name)))
        .where(cb.isNull(customer.get(Customer_.address)))
        .select(customer.get(Customer_.name));
        names = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(1, names.size());
        Assert.assertEquals("Howard", names.get(0));
        
        cq = cb.createQuery(String.class);
        customer = cq.from(Customer.class);
        cq
        .orderBy(cb.asc(customer.get(Customer_.name)))
        .where(cb.isNotNull(customer.get(Customer_.address)).not())
        .select(customer.get(Customer_.name));
        names = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(1, names.size());
        Assert.assertEquals("Howard", names.get(0));
        
        cq = cb.createQuery(String.class);
        customer = cq.from(Customer.class);
        cq
        .orderBy(cb.asc(customer.get(Customer_.name)))
        .where(cb.not(cb.isNotNull(customer.get(Customer_.address))))
        .select(customer.get(Customer_.name));
        names = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(1, names.size());
        Assert.assertEquals("Howard", names.get(0));
        
        cq = cb.createQuery(String.class);
        customer = cq.from(Customer.class);
        cq
        .orderBy(cb.asc(customer.get(Customer_.name)))
        .where(cb.isNotNull(customer.get(Customer_.address)))
        .select(customer.get(Customer_.name));
        names = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(2, names.size());
        Assert.assertEquals("Jim", names.get(0));
        Assert.assertEquals("Smith", names.get(1));
        
        cq = cb.createQuery(String.class);
        customer = cq.from(Customer.class);
        cq
        .orderBy(cb.asc(customer.get(Customer_.name)))
        .where(cb.isNull(customer.get(Customer_.address)).not())
        .select(customer.get(Customer_.name));
        names = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(2, names.size());
        Assert.assertEquals("Jim", names.get(0));
        Assert.assertEquals("Smith", names.get(1));
        
        cq = cb.createQuery(String.class);
        customer = cq.from(Customer.class);
        cq
        .orderBy(cb.asc(customer.get(Customer_.name)))
        .where(cb.not(cb.isNull(customer.get(Customer_.address))))
        .select(customer.get(Customer_.name));
        names = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(2, names.size());
        Assert.assertEquals("Jim", names.get(0));
        Assert.assertEquals("Smith", names.get(1));
    }
    
    @Test
    public void testMemberOf() {
        SaleTransaction jimAudiA8Transaction = this.entityManager.getReference(SaleTransaction.class, 1L);
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Product> cq;
        XRoot<Product> product;
        
        cq = cb.createQuery(Product.class);
        product = cq.from(Product.class);
        cq
        .where(cb.isMember(jimAudiA8Transaction, product.get(Product_.transactions)))
        .orderBy(cb.asc(product.get(Product_.name)));
        Assert.assertEquals("ADUI-A8", this.entityManager.createQuery(cq).getSingleResult().getName());
        
        cq = cb.createQuery(Product.class);
        product = cq.from(Product.class);
        cq
        .where(cb.isMember(jimAudiA8Transaction, product.get(Product_.transactions)).not())
        .orderBy(cb.asc(product.get(Product_.name)));
        List<Product> products = this.entityManager.createQuery(cq).getResultList();
        Assert.assertEquals(3, products.size());
        Assert.assertEquals("Aventador LP700-4", products.get(0).getName());
        Assert.assertEquals("BENZ-S", products.get(1).getName());
        Assert.assertEquals("Bugatti Veyron", products.get(2).getName());
    }
}
