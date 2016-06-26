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
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author Tao Chen
 */
@Entity
@Table(name = "Computer")
@SequenceGenerator(
        name = "computerSequence", 
        sequenceName = "SEQ_COMPUTER_ID", 
        initialValue = 1, 
        allocationSize = 1
)
public class Computer {

    @Id
    @Column(name = "COMPUTER_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "computerSequence")
    private Long id;
    
    @Column(name = "COMPUTER_NAME")
    private String name;
    
    @OneToMany(fetch = FetchType.LAZY)
    @MapKeyColumn(name = "KEY_IN_COMPUTER")
    @JoinColumn(name = "COMPUTER_ID", nullable = true)
    private Map<String, CPU> cpus;
    
    @OneToMany(fetch = FetchType.LAZY)
    @OrderColumn(name = "INDEX_IN_COMPUTER")
    @JoinColumn(name = "COMPUTER_ID", nullable = true)
    private List<Memory> memories;
}
