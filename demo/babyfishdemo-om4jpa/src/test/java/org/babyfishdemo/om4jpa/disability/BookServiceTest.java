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
package org.babyfishdemo.om4jpa.disability;

import java.math.BigDecimal;

import org.babyfish.model.jpa.JPAEntities;
import org.babyfishdemo.om4jpa.disability.AuthorService;
import org.babyfishdemo.om4jpa.disability.BookService;
import org.babyfishdemo.om4jpa.disability.TransactionScope;
import org.babyfishdemo.om4jpa.entities.disability.Author;
import org.babyfishdemo.om4jpa.entities.disability.Book;
import org.babyfishdemo.om4jpa.entities.disability.Book_;
import org.babyfishdemo.om4jpa.entities.disability.Book__;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class BookServiceTest {
    
    private static final String CONNECTION_STRING = "jdbc:hsqldb:mem:org.babyfishdemo.om4jpa.disability.validation_test";

    private BookService bookService = new BookService(CONNECTION_STRING);
    
    private AuthorService authorService = new AuthorService(CONNECTION_STRING);
    
    @Before
    public void prepareData() {
        Assert.assertEquals(
                1L, 
                this.authorService.createAuthor(new Author("Philip A. Bernstein"))
        );
        Assert.assertEquals(
                2L, 
                this.authorService.createAuthor(new Author("Eric Newcomer"))
        );
        Assert.assertEquals(
                3L, 
                this.authorService.createAuthor(new Author("Anand Rajaraman"))
        );
        Assert.assertEquals(
                1L,
                this.bookService.createBook(
                        new Book(
                                "978-7-302-24041-9",
                                "Principles of Transaction Processing, Second Edition",
                                new BigDecimal(48),
                                "Tsinghua University Press",
                                JPAEntities.createFakeEntities(Author.class, 1L, 2L)
                        )
                )
        );
    }
    
    @After
    public void uninstallData() {
        TransactionScope.recreateDatabase();
    }
    
    @Test
    public void testModifyPrimaryBookInfoSuccessed() {
        Book bookBeforeModified = this.bookService.getBookById(
                1L, 
                
                // Fetch the book.authors
                // this is QueryPath, we will learn it later
                Book__.begin().authors().end() 
        );
        assertBook(
                bookBeforeModified, 
                "978-7-302-24041-9", 
                "Principles of Transaction Processing, Second Edition", 
                new BigDecimal("48.00"), 
                "Tsinghua University Press", 
                "Philip A. Bernstein",
                "Eric Newcomer"
        );
        
        Book updateTo = new Book();
        
        /*
         * Disable all the properties except "id"
         */
        JPAEntities.disableAll(updateTo);
        
        updateTo.setId(1L);
        
        // Enable property "isbn" implicitly
        updateTo.setIsbn("978-7-302-24042-9");
        
        // Enable property "name" implicitly
        updateTo.setName("Principles of Transaction Processing, Third Edition");
        
        // Enable property "price" implicitly
        updateTo.setPrice(new BigDecimal(50));
        
        /*
         * The properties "id", "isbn", "name" and "price" are enabled,
         * other properties "publisher" and "authors" are disabled
         */
        Assert.assertTrue(JPAEntities.isEnabled(updateTo, Book_.id));
        Assert.assertTrue(JPAEntities.isEnabled(updateTo, Book_.isbn));
        Assert.assertTrue(JPAEntities.isEnabled(updateTo, Book_.name));
        Assert.assertTrue(JPAEntities.isEnabled(updateTo, Book_.price));
        Assert.assertTrue(JPAEntities.isDisabled(updateTo, Book_.publisher));
        Assert.assertTrue(JPAEntities.isDisabled(updateTo, Book_.authors));
        
        /*
         * BabyFish guarantee that the disabled properties will
         * NEVER be update by babyfish-jpa.
         */
        this.bookService.modifyPrimaryBookInfo(updateTo);
        
        Book bookAfterModified = this.bookService.getBookById(
                1L, 
                Book__.begin().authors().end() // Fetch the book.authors
        );
        assertBook(
                bookAfterModified, 
                "978-7-302-24042-9", 
                "Principles of Transaction Processing, Third Edition", 
                new BigDecimal("50.00"), 
                "Tsinghua University Press", 
                "Philip A. Bernstein",
                "Eric Newcomer");
    }
    
    @Test
    public void testModifyPrimaryBookInfoFailed() {
        Book updateTo = new Book();
        
        /*
         * Disable all the properties except "id"
         */
        JPAEntities.disableAll(updateTo);
        updateTo.setId(1L);
        
        // Enable property "publisher" implicitly
        updateTo.setPublisher("China Machine Press");
        
        try {
            /*
             * Invalid operation because BookService.modifyPrimaryBookInfo(Book)
             * ONLY allows you to update "isbn", "name" and "price"
             * so that you can not update the property "publisher"
             */
            this.bookService.modifyPrimaryBookInfo(updateTo);
            Assert.fail(IllegalArgumentException.class.getName() + " must be raised");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("publisher"));
        }
    }
    
    @Test
    public void testModifySecondaryBookInfoSuccessed() {
        Book bookBeforeModified = this.bookService.getBookById(
                1L, 
                Book__.begin().authors().end() // Fetch the book.authors
        );
        assertBook(
                bookBeforeModified, 
                "978-7-302-24041-9", 
                "Principles of Transaction Processing, Second Edition", 
                new BigDecimal("48.00"), 
                "Tsinghua University Press", 
                "Philip A. Bernstein",
                "Eric Newcomer");
        
        Book updateTo = new Book();
        
        /*
         * Disable all the properties except "id"
         */
        JPAEntities.disableAll(updateTo);
        
        updateTo.setId(1L);
        
        // Enable property "publisher" implicitly
        updateTo.setPublisher("China Machine Press");
    
        // Enable property "authors" implicitly
        updateTo.getAuthors().add(JPAEntities.createFakeEntity(Author.class, 1L));
        updateTo.getAuthors().add(JPAEntities.createFakeEntity(Author.class, 2L));
        updateTo.getAuthors().add(JPAEntities.createFakeEntity(Author.class, 3L));
        
        /*
         * The properties "id", "publisher" and "authors" are enabled,
         * other properties "isbn", "name" and "price" are disabled
         */
        Assert.assertTrue(JPAEntities.isEnabled(updateTo, Book_.id));
        Assert.assertTrue(JPAEntities.isDisabled(updateTo, Book_.isbn));
        Assert.assertTrue(JPAEntities.isDisabled(updateTo, Book_.name));
        Assert.assertTrue(JPAEntities.isDisabled(updateTo, Book_.price));
        Assert.assertTrue(JPAEntities.isEnabled(updateTo, Book_.publisher));
        Assert.assertTrue(JPAEntities.isEnabled(updateTo, Book_.authors));
        
        /*
         * BabyFish guarantee that the disabled properties will
         * NEVER be update by babyfish-jpa.
         */
        this.bookService.modifySecondaryBookInfo(updateTo);
        
        Book bookAfterModified = this.bookService.getBookById(
                1L, 
                Book__.begin().authors().end() // Fetch the book.authors
        );
        assertBook(
                bookAfterModified, 
                "978-7-302-24041-9", 
                "Principles of Transaction Processing, Second Edition", 
                new BigDecimal("48.00"), 
                "China Machine Press", 
                "Philip A. Bernstein",
                "Eric Newcomer",
                "Anand Rajaraman");
    }
    
    @Test
    public void testModifySecondaryBookInfoFailed() {
        Book updateTo = new Book();
        
        /*
         * Disable all the properties except "id"
         */
        JPAEntities.disableAll(updateTo);
        updateTo.setId(1L);
        
        // Enable property "name" implicitly
        updateTo.setName("Principles of Transaction Processing, Third Edition");
        
        try {
            /*
             * Invalid operation because BookService.modifySecondaryBookInfo(Book)
             * ONLY allows you to update "publisher", and "authors"
             * so that you can not update the property "name"
             */
            this.bookService.modifySecondaryBookInfo(updateTo);
            Assert.fail(IllegalArgumentException.class.getName() + " must be raised");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("name"));
        }
    }
    
    @Test
    public void testModifyOnlyOneProperty() {
        Book bookBeforeModified = this.bookService.getBookById(
                1L, 
                Book__.begin().authors().end() // Fetch the book.authors
        );
        assertBook(
                bookBeforeModified, 
                "978-7-302-24041-9", 
                "Principles of Transaction Processing, Second Edition", 
                new BigDecimal("48.00"), 
                "Tsinghua University Press", 
                "Philip A. Bernstein",
                "Eric Newcomer");
        
        Book updateTo = new Book();
        
        /*
         * Disable all the properties except "id"
         */
        JPAEntities.disableAll(updateTo);
        
        updateTo.setId(1L);
        
        // Only modify one property "name"
        // Enable property "name" implicitly
        updateTo.setName("Principles of Transaction Processing, 2nd Edition");
        
        /*
         * All the properties except "id" and "name" are disabled
         */
        Assert.assertTrue(JPAEntities.isEnabled(updateTo, Book_.id));
        Assert.assertTrue(JPAEntities.isDisabled(updateTo, Book_.isbn));
        Assert.assertTrue(JPAEntities.isEnabled(updateTo, Book_.name));
        Assert.assertTrue(JPAEntities.isDisabled(updateTo, Book_.price));
        Assert.assertTrue(JPAEntities.isDisabled(updateTo, Book_.publisher));
        Assert.assertTrue(JPAEntities.isDisabled(updateTo, Book_.authors));
        
        /*
         * BabyFish guarantee that the disabled properties will
         * NEVER be update by babyfish-jpa.
         */
        this.bookService.modifyPrimaryBookInfo(updateTo);
        
        Book bookAfterModified = this.bookService.getBookById(
                1L, 
                Book__.begin().authors().end() // Fetch the book.authors
        );
        /*
         * Only the name has been changed in database.
         */
        assertBook(
                bookAfterModified, 
                "978-7-302-24041-9", 
                "Principles of Transaction Processing, 2nd Edition", 
                new BigDecimal("48.00"), 
                "Tsinghua University Press", 
                "Philip A. Bernstein",
                "Eric Newcomer");
    }
    
    private static void assertBook(
            Book book, 
            String isbn, 
            String name, 
            BigDecimal price, 
            String publisher, 
            String ... authorNames) {
        Assert.assertEquals(isbn, book.getIsbn());
        Assert.assertEquals(name, book.getName());
        Assert.assertEquals(price, book.getPrice());
        Assert.assertEquals(publisher, book.getPublisher());
    }
}
