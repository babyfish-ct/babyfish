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
package org.babyfishdemo.foundation.typedi18n;

import java.util.Date;

import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;

/*
 * Before learn this class, please learn ComplexDemo at first.
 */
/**
 * @author Tao Chen
 */
public class AnnualLeaveRequest {
    
    /*
     * CREATED---------+-------------->SUBMITTED---+--------------->APPROVED
     *  |  /|\         |   submit()                |   approve()
     *  |   |          |                           | 
     *  |   \------\   |                           \-------------->REJECTED
     *  | modify() |   |   cancel()                    reject()
     *  \----------/   \-------------->CANCELLED
     */
    private AnnualLeaveRequestState state;
    
    private String name;

    private Date startTime;
    
    private Date endTime;
    
    public AnnualLeaveRequest(String name, Date startTime, Date endTime) {
        this.name = Arguments.mustNotBeEmpty(
                "name", 
                Arguments.mustNotBeNull("name", name)
        );
        this.state = AnnualLeaveRequestState.CREATED;
        this.modify(startTime, endTime);
    }

    public AnnualLeaveRequestState getState() {
        return this.state;
    }

    public String getName() {
        return this.name;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }
    
    public void modify(Date startTime, Date endTime) {
        if (this.state != AnnualLeaveRequestState.CREATED) {
            throw new IllegalStateException(
                    requestCanNotBeModified(this.state)
            );
        }
        Arguments.mustNotBeNull("startTime", startTime);
        Arguments.mustNotBeNull("endTime", endTime);
        Arguments.mustBeGreaterThanOther("endTime", endTime, "startTime", startTime);
        Arguments.mustBeLessThanOrEqualToValue(
                "Time annual leave hours(endTime - startTime)", 
                (endTime.getTime() - startTime.getTime()) / (60 * 60 * 1000), 
                160
        );
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public void submit() {
        if (this.state != AnnualLeaveRequestState.CREATED) {
            throw new IllegalStateException(
                    requestCanNotBeSubmitted(this.state)
            );
        }
        this.state = AnnualLeaveRequestState.SUBMITTED;
    }
    
    public void cancel() {
        if (this.state != AnnualLeaveRequestState.CREATED) {
            throw new IllegalStateException(
                    requestCanNotBeCancelled(this.state)
            );
        }
        this.state = AnnualLeaveRequestState.CANCELLED;
    }
    
    public void approve() {
        if (this.state != AnnualLeaveRequestState.SUBMITTED) {
            throw new IllegalStateException(
                    requestCanNotBeApproved(this.state)
            );
        }
        this.state = AnnualLeaveRequestState.APPROVED;
    }
    
    public void reject() {
        if (this.state != AnnualLeaveRequestState.SUBMITTED) {
            throw new IllegalStateException(
                    requestCanNotBeRejected(this.state)
            );
        }
        this.state = AnnualLeaveRequestState.REJECTED;
    }
    
    /*
     * The annotation @I18N tell the maven-plugin of babyfish 
     * that the bytecode of this method need to be implemented during compilation
     * 
     * When the maven-plugin compile this method, it will do full validation to
     * check whether the metadata of native method can match the message of I18N resource file,
     * if validation failed, an compilation error will be reported;
     * otherwise, the key word "native" will be removed and the real byte-code will be inserted.
     */
    @I18N
    private static native String requestCanNotBeModified(AnnualLeaveRequestState currentState);
        
    /*
     * The annotation @I18N tell the maven-plugin of babyfish 
     * that the bytecode of this method need to be implemented during compilation
     * 
     * When the maven-plugin compile this method, it will do full validation to
     * check whether the metadata of native method can match the message of I18N resource file,
     * if validation failed, an compilation error will be reported;
     * otherwise, the key word "native" will be removed and the real byte-code will be inserted.
     */
    @I18N
    private static native String requestCanNotBeSubmitted(AnnualLeaveRequestState currentState);
        
    /*
     * The annotation @I18N tell the maven-plugin of babyfish 
     * that the bytecode of this method need to be implemented during compilation
     * 
     * When the maven-plugin compile this method, it will do full validation to
     * check whether the metadata of native method can match the message of I18N resource file,
     * if validation failed, an compilation error will be reported;
     * otherwise, the key word "native" will be removed and the real byte-code will be inserted.
     */
    @I18N
    private static native String requestCanNotBeCancelled(AnnualLeaveRequestState currentState);
        
    /*
     * The annotation @I18N tell the maven-plugin of babyfish 
     * that the bytecode of this method need to be implemented during compilation
     * 
     * When the maven-plugin compile this method, it will do full validation to
     * check whether the metadata of native method can match the message of I18N resource file,
     * if validation failed, an compilation error will be reported;
     * otherwise, the key word "native" will be removed and the real byte-code will be inserted.
     */
    @I18N
    private static native String requestCanNotBeApproved(AnnualLeaveRequestState currentState);
      
    /*
     * The annotation @I18N tell the maven-plugin of babyfish 
     * that the bytecode of this method need to be implemented during compilation
     * 
     * When the maven-plugin compile this method, it will do full validation to
     * check whether the metadata of native method can match the message of I18N resource file,
     * if validation failed, an compilation error will be reported;
     * otherwise, the key word "native" will be removed and the real byte-code will be inserted.
     */
    @I18N
    private static native String requestCanNotBeRejected(AnnualLeaveRequestState currentState);
}
