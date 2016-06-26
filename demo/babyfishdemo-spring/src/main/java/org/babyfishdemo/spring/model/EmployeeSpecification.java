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
package org.babyfishdemo.spring.model;

import java.util.Collection;
import java.util.Date;

import org.babyfishdemo.spring.entities.Employee.Gender;

/**
 * @author Tao Chen
 */
public class EmployeeSpecification {

    private String likeFirstName;
    
    private String likeLastName;
    
    private Gender gender;
    
    private Date minBirthday;
    
    private Date maxBirthday;
    
    private String likeDepartmentName;
    
    private Collection<String> includedDepartmentNames;
    
    private Collection<String> excludedDepartmentNames;
    
    private Boolean hasPendingAnnualLeaves;

    public String getLikeFirstName() {
        return likeFirstName;
    }

    public void setLikeFirstName(String likeFirstName) {
        this.likeFirstName = likeFirstName;
    }

    public String getLikeLastName() {
        return likeLastName;
    }

    public void setLikeLastName(String likeLastName) {
        this.likeLastName = likeLastName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Date getMinBirthday() {
        return minBirthday;
    }

    public void setMinBirthday(Date minBirthday) {
        this.minBirthday = minBirthday;
    }

    public Date getMaxBirthday() {
        return maxBirthday;
    }

    public void setMaxBirthday(Date maxBirthday) {
        this.maxBirthday = maxBirthday;
    }

    public String getLikeDepartmentName() {
        return likeDepartmentName;
    }

    public void setLikeDepartmentName(String likeDepartmentName) {
        this.likeDepartmentName = likeDepartmentName;
    }

    public Collection<String> getIncludedDepartmentNames() {
        return includedDepartmentNames;
    }

    public void setIncludedDepartmentNames(
            Collection<String> includedDepartmentNames) {
        this.includedDepartmentNames = includedDepartmentNames;
    }

    public Collection<String> getExcludedDepartmentNames() {
        return excludedDepartmentNames;
    }

    public void setExcludedDepartmentNames(
            Collection<String> excludedDepartmentNames) {
        this.excludedDepartmentNames = excludedDepartmentNames;
    }

    public Boolean getHasPendingAnnualLeaves() {
        return hasPendingAnnualLeaves;
    }

    public void setHasPendingAnnualLeaves(Boolean hasPendingAnnualLeaves) {
        this.hasPendingAnnualLeaves = hasPendingAnnualLeaves;
    }
}
