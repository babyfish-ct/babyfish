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

import org.babyfish.lang.Arguments;
import org.babyfishdemo.om4jpa.entities.disability.Author;

/*
 * The AuthorService does NOT contain business logic,
 * so the functionality of DAO is embedded here directly
 * and need not to create AuthorRepository
 * 
 * In real project, please create the AuthorRepository
 * even if AuthorService has NO business logic.
 */
/**
 * @author Tao Chen
 */
public class AuthorService {

    private String connectionString;
    
    public AuthorService(String connectionString) {
        this.connectionString = connectionString;
    }
    
    public long createAuthor(Author author) {
        Arguments.mustBeNull(
                "author.id", 
                Arguments.mustNotBeNull("author", author).getId()
        );
        try (TransactionScope ts = new TransactionScope(this.connectionString)) {
            // In real project, please don't do this
            // You should create AuthorRepository and
            // NEVER invoke any JPA API here directly.
            return ts.complete(TransactionScope.getEntityManager().merge(author).getId());
        }
    }
}
