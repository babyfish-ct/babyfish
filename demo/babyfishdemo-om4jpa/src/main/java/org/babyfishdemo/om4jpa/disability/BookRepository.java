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

import org.babyfishdemo.om4jpa.entities.disability.Book;
import org.babyfishdemo.om4jpa.entities.disability.Book__;

/**
 * @author Tao Chen
 */
public class BookRepository {
    
    Book getBookById(
            long id, 
            /*
             * We will learn this parameter "queryPaths" in the demo project 
             * babyfishdemo-querypath, don't worry, we use it very simply in
             * this demo so you need NOT to learn it temporarily.
             */
            Book__ ... queryPaths) {
        return TransactionScope.getEntityManager().find(Book.class, id, queryPaths);
    }

    Book mergeBook(Book book) {
        return TransactionScope.getEntityManager().merge(book);
    }
}
