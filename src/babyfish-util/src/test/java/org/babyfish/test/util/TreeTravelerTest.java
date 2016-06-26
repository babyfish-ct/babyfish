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
package org.babyfish.test.util;

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.Strings;
import org.babyfish.util.GraphTravelAction;
import org.babyfish.util.GraphTravelContext;
import org.babyfish.util.GraphTraveler;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class TreeTravelerTest {

    @Test
    public void testTreeDepthFirst() {
        List<String> beforeList = new ArrayList<String>();
        List<String> afterList = new ArrayList<String>();
        new TreeTraveler(beforeList, afterList).depthFirstTravel(createTree());
        Assert.assertEquals(56, beforeList.size());
        Assert.assertEquals(56, afterList.size());
        assertList(
                beforeList,
                "Technology { index = 0, path = Technology }",
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "Win32 { index = 0.0.0.0, path = Technology->CommonLanguages->C++->Win32 }",
                "Socket { index = 0.0.0.1, path = Technology->CommonLanguages->C++->Socket }",
                "MFC { index = 0.0.0.3, path = Technology->CommonLanguages->C++->MFC }",
                "ATL { index = 0.0.0.4, path = Technology->CommonLanguages->C++->ATL }",
                "DirectX { index = 0.0.0.6, path = Technology->CommonLanguages->C++->DirectX }",
                "Direct3D { index = 0.0.0.6.0, path = Technology->CommonLanguages->C++->DirectX->Direct3D }",
                "DirectDraw { index = 0.0.0.6.1, path = Technology->CommonLanguages->C++->DirectX->DirectDraw }",
                "DirectInput { index = 0.0.0.6.2, path = Technology->CommonLanguages->C++->DirectX->DirectInput }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                "CGLIB { index = 0.0.1.0, path = Technology->CommonLanguages->Java->CGLIB }",
                "ASM { index = 0.0.1.1, path = Technology->CommonLanguages->Java->ASM }",
                "ORM { index = 0.0.1.3, path = Technology->CommonLanguages->Java->ORM }",
                "Hibernate { index = 0.0.1.3.0, path = Technology->CommonLanguages->Java->ORM->Hibernate }",
                "myBatis { index = 0.0.1.3.2, path = Technology->CommonLanguages->Java->ORM->myBatis }",
                "JPA { index = 0.0.1.3.3, path = Technology->CommonLanguages->Java->ORM->JPA }",
                "RMI { index = 0.0.1.4, path = Technology->CommonLanguages->Java->RMI }",
                "EJB { index = 0.0.1.5, path = Technology->CommonLanguages->Java->EJB }",
                "EJB2.1 { index = 0.0.1.5.0, path = Technology->CommonLanguages->Java->EJB->EJB2.1 }",
                "EJB3 { index = 0.0.1.5.1, path = Technology->CommonLanguages->Java->EJB->EJB3 }",
                "WebService { index = 0.0.1.6, path = Technology->CommonLanguages->Java->WebService }",
                "Spring { index = 0.0.1.7, path = Technology->CommonLanguages->Java->Spring }",
                "Swing { index = 0.0.1.9, path = Technology->CommonLanguages->Java->Swing }",
                "SWT { index = 0.0.1.10, path = Technology->CommonLanguages->Java->SWT }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "WinForm { index = 0.0.2.0, path = Technology->CommonLanguages->.NET->WinForm }",
                "WPF { index = 0.0.2.1, path = Technology->CommonLanguages->.NET->WPF }",
                "ASP.NET { index = 0.0.2.2, path = Technology->CommonLanguages->.NET->ASP.NET }",
                "ADO.NET { index = 0.0.2.4, path = Technology->CommonLanguages->.NET->ADO.NET }",
                "COM+ { index = 0.0.2.5, path = Technology->CommonLanguages->.NET->COM+ }",
                ".NET Remoting { index = 0.0.2.6, path = Technology->CommonLanguages->.NET->.NET Remoting }",
                "WebService { index = 0.0.2.7, path = Technology->CommonLanguages->.NET->WebService }",
                "Spring.NET { index = 0.0.2.8, path = Technology->CommonLanguages->.NET->Spring.NET }",
                "ORM { index = 0.0.2.10, path = Technology->CommonLanguages->.NET->ORM }",
                "NHibernate { index = 0.0.2.10.0, path = Technology->CommonLanguages->.NET->ORM->NHibernate }",
                "ADO.NET EntityFramework { index = 0.0.2.10.2, path = Technology->CommonLanguages->.NET->ORM->ADO.NET EntityFramework }",
                "Database { index = 0.1, path = Technology->Database }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "Oracle11g { index = 0.1.0.0, path = Technology->Database->Oracle->Oracle11g }",
                "Oracle11g SQL { index = 0.1.0.0.0, path = Technology->Database->Oracle->Oracle11g->Oracle11g SQL }",
                "Oracle11g RMAN { index = 0.1.0.0.2, path = Technology->Database->Oracle->Oracle11g->Oracle11g RMAN }",
                "Oracle11g DBA { index = 0.1.0.0.3, path = Technology->Database->Oracle->Oracle11g->Oracle11g DBA }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "XML { index = 0.2, path = Technology->XML }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }");
        assertList(
                afterList,
                "Win32 { index = 0.0.0.0, path = Technology->CommonLanguages->C++->Win32 }",
                "Socket { index = 0.0.0.1, path = Technology->CommonLanguages->C++->Socket }",
                "MFC { index = 0.0.0.3, path = Technology->CommonLanguages->C++->MFC }",
                "ATL { index = 0.0.0.4, path = Technology->CommonLanguages->C++->ATL }",
                "Direct3D { index = 0.0.0.6.0, path = Technology->CommonLanguages->C++->DirectX->Direct3D }",
                "DirectDraw { index = 0.0.0.6.1, path = Technology->CommonLanguages->C++->DirectX->DirectDraw }",
                "DirectInput { index = 0.0.0.6.2, path = Technology->CommonLanguages->C++->DirectX->DirectInput }",
                "DirectX { index = 0.0.0.6, path = Technology->CommonLanguages->C++->DirectX }",
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "CGLIB { index = 0.0.1.0, path = Technology->CommonLanguages->Java->CGLIB }",
                "ASM { index = 0.0.1.1, path = Technology->CommonLanguages->Java->ASM }",
                "Hibernate { index = 0.0.1.3.0, path = Technology->CommonLanguages->Java->ORM->Hibernate }",
                "myBatis { index = 0.0.1.3.2, path = Technology->CommonLanguages->Java->ORM->myBatis }",
                "JPA { index = 0.0.1.3.3, path = Technology->CommonLanguages->Java->ORM->JPA }",
                "ORM { index = 0.0.1.3, path = Technology->CommonLanguages->Java->ORM }",
                "RMI { index = 0.0.1.4, path = Technology->CommonLanguages->Java->RMI }",
                "EJB2.1 { index = 0.0.1.5.0, path = Technology->CommonLanguages->Java->EJB->EJB2.1 }",
                "EJB3 { index = 0.0.1.5.1, path = Technology->CommonLanguages->Java->EJB->EJB3 }",
                "EJB { index = 0.0.1.5, path = Technology->CommonLanguages->Java->EJB }",
                "WebService { index = 0.0.1.6, path = Technology->CommonLanguages->Java->WebService }",
                "Spring { index = 0.0.1.7, path = Technology->CommonLanguages->Java->Spring }",
                "Swing { index = 0.0.1.9, path = Technology->CommonLanguages->Java->Swing }",
                "SWT { index = 0.0.1.10, path = Technology->CommonLanguages->Java->SWT }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                "WinForm { index = 0.0.2.0, path = Technology->CommonLanguages->.NET->WinForm }",
                "WPF { index = 0.0.2.1, path = Technology->CommonLanguages->.NET->WPF }",
                "ASP.NET { index = 0.0.2.2, path = Technology->CommonLanguages->.NET->ASP.NET }",
                "ADO.NET { index = 0.0.2.4, path = Technology->CommonLanguages->.NET->ADO.NET }",
                "COM+ { index = 0.0.2.5, path = Technology->CommonLanguages->.NET->COM+ }",
                ".NET Remoting { index = 0.0.2.6, path = Technology->CommonLanguages->.NET->.NET Remoting }",
                "WebService { index = 0.0.2.7, path = Technology->CommonLanguages->.NET->WebService }",
                "Spring.NET { index = 0.0.2.8, path = Technology->CommonLanguages->.NET->Spring.NET }",
                "NHibernate { index = 0.0.2.10.0, path = Technology->CommonLanguages->.NET->ORM->NHibernate }",
                "ADO.NET EntityFramework { index = 0.0.2.10.2, path = Technology->CommonLanguages->.NET->ORM->ADO.NET EntityFramework }",
                "ORM { index = 0.0.2.10, path = Technology->CommonLanguages->.NET->ORM }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "Oracle11g SQL { index = 0.1.0.0.0, path = Technology->Database->Oracle->Oracle11g->Oracle11g SQL }",
                "Oracle11g RMAN { index = 0.1.0.0.2, path = Technology->Database->Oracle->Oracle11g->Oracle11g RMAN }",
                "Oracle11g DBA { index = 0.1.0.0.3, path = Technology->Database->Oracle->Oracle11g->Oracle11g DBA }",
                "Oracle11g { index = 0.1.0.0, path = Technology->Database->Oracle->Oracle11g }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "Database { index = 0.1, path = Technology->Database }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "XML { index = 0.2, path = Technology->XML }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                "Technology { index = 0, path = Technology }");
    }
    
    @Test
    public void testTreeBreadthFirst() {
        List<String> beforeList = new ArrayList<String>();
        List<String> afterList = new ArrayList<String>();
        new TreeTraveler(beforeList, afterList).breadthFirstTravel(createTree());
        Assert.assertEquals(56, beforeList.size());
        Assert.assertEquals(56, afterList.size());
        assertList(
                beforeList,
                "Technology { index = 0, path = Technology }",
                
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "Database { index = 0.1, path = Technology->Database }",
                "XML { index = 0.2, path = Technology->XML }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }",
                
                "Win32 { index = 0.0.0.0, path = Technology->CommonLanguages->C++->Win32 }",
                "Socket { index = 0.0.0.1, path = Technology->CommonLanguages->C++->Socket }",
                "MFC { index = 0.0.0.3, path = Technology->CommonLanguages->C++->MFC }",
                "ATL { index = 0.0.0.4, path = Technology->CommonLanguages->C++->ATL }",
                "DirectX { index = 0.0.0.6, path = Technology->CommonLanguages->C++->DirectX }",
                "CGLIB { index = 0.0.1.0, path = Technology->CommonLanguages->Java->CGLIB }",
                "ASM { index = 0.0.1.1, path = Technology->CommonLanguages->Java->ASM }",
                "ORM { index = 0.0.1.3, path = Technology->CommonLanguages->Java->ORM }",
                "RMI { index = 0.0.1.4, path = Technology->CommonLanguages->Java->RMI }",
                "EJB { index = 0.0.1.5, path = Technology->CommonLanguages->Java->EJB }",
                "WebService { index = 0.0.1.6, path = Technology->CommonLanguages->Java->WebService }",
                "Spring { index = 0.0.1.7, path = Technology->CommonLanguages->Java->Spring }",
                "Swing { index = 0.0.1.9, path = Technology->CommonLanguages->Java->Swing }",
                "SWT { index = 0.0.1.10, path = Technology->CommonLanguages->Java->SWT }",
                "WinForm { index = 0.0.2.0, path = Technology->CommonLanguages->.NET->WinForm }",
                "WPF { index = 0.0.2.1, path = Technology->CommonLanguages->.NET->WPF }",
                "ASP.NET { index = 0.0.2.2, path = Technology->CommonLanguages->.NET->ASP.NET }",
                "ADO.NET { index = 0.0.2.4, path = Technology->CommonLanguages->.NET->ADO.NET }",
                "COM+ { index = 0.0.2.5, path = Technology->CommonLanguages->.NET->COM+ }",
                ".NET Remoting { index = 0.0.2.6, path = Technology->CommonLanguages->.NET->.NET Remoting }",
                "WebService { index = 0.0.2.7, path = Technology->CommonLanguages->.NET->WebService }",
                "Spring.NET { index = 0.0.2.8, path = Technology->CommonLanguages->.NET->Spring.NET }",
                "ORM { index = 0.0.2.10, path = Technology->CommonLanguages->.NET->ORM }",
                "Oracle11g { index = 0.1.0.0, path = Technology->Database->Oracle->Oracle11g }",
                
                "Direct3D { index = 0.0.0.6.0, path = Technology->CommonLanguages->C++->DirectX->Direct3D }",
                "DirectDraw { index = 0.0.0.6.1, path = Technology->CommonLanguages->C++->DirectX->DirectDraw }",
                "DirectInput { index = 0.0.0.6.2, path = Technology->CommonLanguages->C++->DirectX->DirectInput }",
                "Hibernate { index = 0.0.1.3.0, path = Technology->CommonLanguages->Java->ORM->Hibernate }",
                "myBatis { index = 0.0.1.3.2, path = Technology->CommonLanguages->Java->ORM->myBatis }",
                "JPA { index = 0.0.1.3.3, path = Technology->CommonLanguages->Java->ORM->JPA }",
                "EJB2.1 { index = 0.0.1.5.0, path = Technology->CommonLanguages->Java->EJB->EJB2.1 }",
                "EJB3 { index = 0.0.1.5.1, path = Technology->CommonLanguages->Java->EJB->EJB3 }",
                "NHibernate { index = 0.0.2.10.0, path = Technology->CommonLanguages->.NET->ORM->NHibernate }",
                "ADO.NET EntityFramework { index = 0.0.2.10.2, path = Technology->CommonLanguages->.NET->ORM->ADO.NET EntityFramework }",
                "Oracle11g SQL { index = 0.1.0.0.0, path = Technology->Database->Oracle->Oracle11g->Oracle11g SQL }",
                "Oracle11g RMAN { index = 0.1.0.0.2, path = Technology->Database->Oracle->Oracle11g->Oracle11g RMAN }",
                "Oracle11g DBA { index = 0.1.0.0.3, path = Technology->Database->Oracle->Oracle11g->Oracle11g DBA }");
        assertList(
                afterList,
                
                "Direct3D { index = 0.0.0.6.0, path = Technology->CommonLanguages->C++->DirectX->Direct3D }",
                "DirectDraw { index = 0.0.0.6.1, path = Technology->CommonLanguages->C++->DirectX->DirectDraw }",
                "DirectInput { index = 0.0.0.6.2, path = Technology->CommonLanguages->C++->DirectX->DirectInput }",
                "Hibernate { index = 0.0.1.3.0, path = Technology->CommonLanguages->Java->ORM->Hibernate }",
                "myBatis { index = 0.0.1.3.2, path = Technology->CommonLanguages->Java->ORM->myBatis }",
                "JPA { index = 0.0.1.3.3, path = Technology->CommonLanguages->Java->ORM->JPA }",
                "EJB2.1 { index = 0.0.1.5.0, path = Technology->CommonLanguages->Java->EJB->EJB2.1 }",
                "EJB3 { index = 0.0.1.5.1, path = Technology->CommonLanguages->Java->EJB->EJB3 }",
                "NHibernate { index = 0.0.2.10.0, path = Technology->CommonLanguages->.NET->ORM->NHibernate }",
                "ADO.NET EntityFramework { index = 0.0.2.10.2, path = Technology->CommonLanguages->.NET->ORM->ADO.NET EntityFramework }",
                "Oracle11g SQL { index = 0.1.0.0.0, path = Technology->Database->Oracle->Oracle11g->Oracle11g SQL }",
                "Oracle11g RMAN { index = 0.1.0.0.2, path = Technology->Database->Oracle->Oracle11g->Oracle11g RMAN }",
                "Oracle11g DBA { index = 0.1.0.0.3, path = Technology->Database->Oracle->Oracle11g->Oracle11g DBA }",
                
                "Win32 { index = 0.0.0.0, path = Technology->CommonLanguages->C++->Win32 }",
                "Socket { index = 0.0.0.1, path = Technology->CommonLanguages->C++->Socket }",
                "MFC { index = 0.0.0.3, path = Technology->CommonLanguages->C++->MFC }",
                "ATL { index = 0.0.0.4, path = Technology->CommonLanguages->C++->ATL }",
                "DirectX { index = 0.0.0.6, path = Technology->CommonLanguages->C++->DirectX }",
                "CGLIB { index = 0.0.1.0, path = Technology->CommonLanguages->Java->CGLIB }",
                "ASM { index = 0.0.1.1, path = Technology->CommonLanguages->Java->ASM }",
                "ORM { index = 0.0.1.3, path = Technology->CommonLanguages->Java->ORM }",
                "RMI { index = 0.0.1.4, path = Technology->CommonLanguages->Java->RMI }",
                "EJB { index = 0.0.1.5, path = Technology->CommonLanguages->Java->EJB }",
                "WebService { index = 0.0.1.6, path = Technology->CommonLanguages->Java->WebService }",
                "Spring { index = 0.0.1.7, path = Technology->CommonLanguages->Java->Spring }",
                "Swing { index = 0.0.1.9, path = Technology->CommonLanguages->Java->Swing }",
                "SWT { index = 0.0.1.10, path = Technology->CommonLanguages->Java->SWT }",
                "WinForm { index = 0.0.2.0, path = Technology->CommonLanguages->.NET->WinForm }",
                "WPF { index = 0.0.2.1, path = Technology->CommonLanguages->.NET->WPF }",
                "ASP.NET { index = 0.0.2.2, path = Technology->CommonLanguages->.NET->ASP.NET }",
                "ADO.NET { index = 0.0.2.4, path = Technology->CommonLanguages->.NET->ADO.NET }",
                "COM+ { index = 0.0.2.5, path = Technology->CommonLanguages->.NET->COM+ }",
                ".NET Remoting { index = 0.0.2.6, path = Technology->CommonLanguages->.NET->.NET Remoting }",
                "WebService { index = 0.0.2.7, path = Technology->CommonLanguages->.NET->WebService }",
                "Spring.NET { index = 0.0.2.8, path = Technology->CommonLanguages->.NET->Spring.NET }",
                "ORM { index = 0.0.2.10, path = Technology->CommonLanguages->.NET->ORM }",
                "Oracle11g { index = 0.1.0.0, path = Technology->Database->Oracle->Oracle11g }",
                
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }",
                
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "Database { index = 0.1, path = Technology->Database }",
                "XML { index = 0.2, path = Technology->XML }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                
                "Technology { index = 0, path = Technology }");
    }
    
    @Test
    public void testNullableTreeDepthFirst() {
        List<String> beforeList = new ArrayList<String>();
        List<String> afterList = new ArrayList<String>();
        new NullableTreeTraveler(beforeList, afterList).depthFirstTravel(createTree());
        Assert.assertEquals(70, beforeList.size());
        Assert.assertEquals(70, afterList.size());
        assertList(
                beforeList,
                "Technology { index = 0, path = Technology }",
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "Win32 { index = 0.0.0.0, path = Technology->CommonLanguages->C++->Win32 }",
                "Socket { index = 0.0.0.1, path = Technology->CommonLanguages->C++->Socket }",
                "null { index = 0.0.0.2, path = Technology->CommonLanguages->C++->null }",
                "MFC { index = 0.0.0.3, path = Technology->CommonLanguages->C++->MFC }",
                "ATL { index = 0.0.0.4, path = Technology->CommonLanguages->C++->ATL }",
                "null { index = 0.0.0.5, path = Technology->CommonLanguages->C++->null }",
                "DirectX { index = 0.0.0.6, path = Technology->CommonLanguages->C++->DirectX }",
                "Direct3D { index = 0.0.0.6.0, path = Technology->CommonLanguages->C++->DirectX->Direct3D }",
                "DirectDraw { index = 0.0.0.6.1, path = Technology->CommonLanguages->C++->DirectX->DirectDraw }",
                "DirectInput { index = 0.0.0.6.2, path = Technology->CommonLanguages->C++->DirectX->DirectInput }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                "CGLIB { index = 0.0.1.0, path = Technology->CommonLanguages->Java->CGLIB }",
                "ASM { index = 0.0.1.1, path = Technology->CommonLanguages->Java->ASM }",
                "null { index = 0.0.1.2, path = Technology->CommonLanguages->Java->null }",
                "ORM { index = 0.0.1.3, path = Technology->CommonLanguages->Java->ORM }",
                "Hibernate { index = 0.0.1.3.0, path = Technology->CommonLanguages->Java->ORM->Hibernate }",
                "null { index = 0.0.1.3.1, path = Technology->CommonLanguages->Java->ORM->null }",
                "myBatis { index = 0.0.1.3.2, path = Technology->CommonLanguages->Java->ORM->myBatis }",
                "JPA { index = 0.0.1.3.3, path = Technology->CommonLanguages->Java->ORM->JPA }",
                "RMI { index = 0.0.1.4, path = Technology->CommonLanguages->Java->RMI }",
                "EJB { index = 0.0.1.5, path = Technology->CommonLanguages->Java->EJB }",
                "EJB2.1 { index = 0.0.1.5.0, path = Technology->CommonLanguages->Java->EJB->EJB2.1 }",
                "EJB3 { index = 0.0.1.5.1, path = Technology->CommonLanguages->Java->EJB->EJB3 }",
                "WebService { index = 0.0.1.6, path = Technology->CommonLanguages->Java->WebService }",
                "Spring { index = 0.0.1.7, path = Technology->CommonLanguages->Java->Spring }",
                "null { index = 0.0.1.8, path = Technology->CommonLanguages->Java->null }",
                "Swing { index = 0.0.1.9, path = Technology->CommonLanguages->Java->Swing }",
                "SWT { index = 0.0.1.10, path = Technology->CommonLanguages->Java->SWT }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "WinForm { index = 0.0.2.0, path = Technology->CommonLanguages->.NET->WinForm }",
                "WPF { index = 0.0.2.1, path = Technology->CommonLanguages->.NET->WPF }",
                "ASP.NET { index = 0.0.2.2, path = Technology->CommonLanguages->.NET->ASP.NET }",
                "null { index = 0.0.2.3, path = Technology->CommonLanguages->.NET->null }",
                "ADO.NET { index = 0.0.2.4, path = Technology->CommonLanguages->.NET->ADO.NET }",
                "COM+ { index = 0.0.2.5, path = Technology->CommonLanguages->.NET->COM+ }",
                ".NET Remoting { index = 0.0.2.6, path = Technology->CommonLanguages->.NET->.NET Remoting }",
                "WebService { index = 0.0.2.7, path = Technology->CommonLanguages->.NET->WebService }",
                "Spring.NET { index = 0.0.2.8, path = Technology->CommonLanguages->.NET->Spring.NET }",
                "null { index = 0.0.2.9, path = Technology->CommonLanguages->.NET->null }",
                "ORM { index = 0.0.2.10, path = Technology->CommonLanguages->.NET->ORM }",
                "NHibernate { index = 0.0.2.10.0, path = Technology->CommonLanguages->.NET->ORM->NHibernate }",
                "null { index = 0.0.2.10.1, path = Technology->CommonLanguages->.NET->ORM->null }",
                "ADO.NET EntityFramework { index = 0.0.2.10.2, path = Technology->CommonLanguages->.NET->ORM->ADO.NET EntityFramework }",
                "Database { index = 0.1, path = Technology->Database }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "Oracle11g { index = 0.1.0.0, path = Technology->Database->Oracle->Oracle11g }",
                "Oracle11g SQL { index = 0.1.0.0.0, path = Technology->Database->Oracle->Oracle11g->Oracle11g SQL }",
                "null { index = 0.1.0.0.1, path = Technology->Database->Oracle->Oracle11g->null }",
                "Oracle11g RMAN { index = 0.1.0.0.2, path = Technology->Database->Oracle->Oracle11g->Oracle11g RMAN }",
                "Oracle11g DBA { index = 0.1.0.0.3, path = Technology->Database->Oracle->Oracle11g->Oracle11g DBA }",
                "null { index = 0.1.1, path = Technology->Database->null }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "XML { index = 0.2, path = Technology->XML }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "null { index = 0.2.2, path = Technology->XML->null }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "null { index = 0.3, path = Technology->null }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "null { index = 0.4.1, path = Technology->WebClient->null }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "null { index = 0.4.4, path = Technology->WebClient->null }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }");
        assertList(
                afterList,
                "Win32 { index = 0.0.0.0, path = Technology->CommonLanguages->C++->Win32 }",
                "Socket { index = 0.0.0.1, path = Technology->CommonLanguages->C++->Socket }",
                "null { index = 0.0.0.2, path = Technology->CommonLanguages->C++->null }",
                "MFC { index = 0.0.0.3, path = Technology->CommonLanguages->C++->MFC }",
                "ATL { index = 0.0.0.4, path = Technology->CommonLanguages->C++->ATL }",
                "null { index = 0.0.0.5, path = Technology->CommonLanguages->C++->null }",
                "Direct3D { index = 0.0.0.6.0, path = Technology->CommonLanguages->C++->DirectX->Direct3D }",
                "DirectDraw { index = 0.0.0.6.1, path = Technology->CommonLanguages->C++->DirectX->DirectDraw }",
                "DirectInput { index = 0.0.0.6.2, path = Technology->CommonLanguages->C++->DirectX->DirectInput }",
                "DirectX { index = 0.0.0.6, path = Technology->CommonLanguages->C++->DirectX }",
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "CGLIB { index = 0.0.1.0, path = Technology->CommonLanguages->Java->CGLIB }",
                "ASM { index = 0.0.1.1, path = Technology->CommonLanguages->Java->ASM }",
                "null { index = 0.0.1.2, path = Technology->CommonLanguages->Java->null }",
                "Hibernate { index = 0.0.1.3.0, path = Technology->CommonLanguages->Java->ORM->Hibernate }",
                "null { index = 0.0.1.3.1, path = Technology->CommonLanguages->Java->ORM->null }",
                "myBatis { index = 0.0.1.3.2, path = Technology->CommonLanguages->Java->ORM->myBatis }",
                "JPA { index = 0.0.1.3.3, path = Technology->CommonLanguages->Java->ORM->JPA }",
                "ORM { index = 0.0.1.3, path = Technology->CommonLanguages->Java->ORM }",
                "RMI { index = 0.0.1.4, path = Technology->CommonLanguages->Java->RMI }",
                "EJB2.1 { index = 0.0.1.5.0, path = Technology->CommonLanguages->Java->EJB->EJB2.1 }",
                "EJB3 { index = 0.0.1.5.1, path = Technology->CommonLanguages->Java->EJB->EJB3 }",
                "EJB { index = 0.0.1.5, path = Technology->CommonLanguages->Java->EJB }",
                "WebService { index = 0.0.1.6, path = Technology->CommonLanguages->Java->WebService }",
                "Spring { index = 0.0.1.7, path = Technology->CommonLanguages->Java->Spring }",
                "null { index = 0.0.1.8, path = Technology->CommonLanguages->Java->null }",
                "Swing { index = 0.0.1.9, path = Technology->CommonLanguages->Java->Swing }",
                "SWT { index = 0.0.1.10, path = Technology->CommonLanguages->Java->SWT }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                "WinForm { index = 0.0.2.0, path = Technology->CommonLanguages->.NET->WinForm }",
                "WPF { index = 0.0.2.1, path = Technology->CommonLanguages->.NET->WPF }",
                "ASP.NET { index = 0.0.2.2, path = Technology->CommonLanguages->.NET->ASP.NET }",
                "null { index = 0.0.2.3, path = Technology->CommonLanguages->.NET->null }",
                "ADO.NET { index = 0.0.2.4, path = Technology->CommonLanguages->.NET->ADO.NET }",
                "COM+ { index = 0.0.2.5, path = Technology->CommonLanguages->.NET->COM+ }",
                ".NET Remoting { index = 0.0.2.6, path = Technology->CommonLanguages->.NET->.NET Remoting }",
                "WebService { index = 0.0.2.7, path = Technology->CommonLanguages->.NET->WebService }",
                "Spring.NET { index = 0.0.2.8, path = Technology->CommonLanguages->.NET->Spring.NET }",
                "null { index = 0.0.2.9, path = Technology->CommonLanguages->.NET->null }",
                "NHibernate { index = 0.0.2.10.0, path = Technology->CommonLanguages->.NET->ORM->NHibernate }",
                "null { index = 0.0.2.10.1, path = Technology->CommonLanguages->.NET->ORM->null }",
                "ADO.NET EntityFramework { index = 0.0.2.10.2, path = Technology->CommonLanguages->.NET->ORM->ADO.NET EntityFramework }",
                "ORM { index = 0.0.2.10, path = Technology->CommonLanguages->.NET->ORM }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "Oracle11g SQL { index = 0.1.0.0.0, path = Technology->Database->Oracle->Oracle11g->Oracle11g SQL }",
                "null { index = 0.1.0.0.1, path = Technology->Database->Oracle->Oracle11g->null }",
                "Oracle11g RMAN { index = 0.1.0.0.2, path = Technology->Database->Oracle->Oracle11g->Oracle11g RMAN }",
                "Oracle11g DBA { index = 0.1.0.0.3, path = Technology->Database->Oracle->Oracle11g->Oracle11g DBA }",
                "Oracle11g { index = 0.1.0.0, path = Technology->Database->Oracle->Oracle11g }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "null { index = 0.1.1, path = Technology->Database->null }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "Database { index = 0.1, path = Technology->Database }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "null { index = 0.2.2, path = Technology->XML->null }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "XML { index = 0.2, path = Technology->XML }",
                "null { index = 0.3, path = Technology->null }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "null { index = 0.4.1, path = Technology->WebClient->null }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "null { index = 0.4.4, path = Technology->WebClient->null }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                "Technology { index = 0, path = Technology }");
    }
    
    @Test
    public void testNullableTreeBreadthFirst() {
        List<String> beforeList = new ArrayList<String>();
        List<String> afterList = new ArrayList<String>();
        new NullableTreeTraveler(beforeList, afterList).breadthFirstTravel(createTree());
        Assert.assertEquals(70, beforeList.size());
        Assert.assertEquals(70, afterList.size());
        assertList(
                beforeList,
                "Technology { index = 0, path = Technology }",
                
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "Database { index = 0.1, path = Technology->Database }",
                "XML { index = 0.2, path = Technology->XML }",
                "null { index = 0.3, path = Technology->null }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "null { index = 0.1.1, path = Technology->Database->null }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "null { index = 0.2.2, path = Technology->XML->null }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "null { index = 0.4.1, path = Technology->WebClient->null }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "null { index = 0.4.4, path = Technology->WebClient->null }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }",
                
                "Win32 { index = 0.0.0.0, path = Technology->CommonLanguages->C++->Win32 }",
                "Socket { index = 0.0.0.1, path = Technology->CommonLanguages->C++->Socket }",
                "null { index = 0.0.0.2, path = Technology->CommonLanguages->C++->null }",
                "MFC { index = 0.0.0.3, path = Technology->CommonLanguages->C++->MFC }",
                "ATL { index = 0.0.0.4, path = Technology->CommonLanguages->C++->ATL }",
                "null { index = 0.0.0.5, path = Technology->CommonLanguages->C++->null }",
                "DirectX { index = 0.0.0.6, path = Technology->CommonLanguages->C++->DirectX }",
                "CGLIB { index = 0.0.1.0, path = Technology->CommonLanguages->Java->CGLIB }",
                "ASM { index = 0.0.1.1, path = Technology->CommonLanguages->Java->ASM }",
                "null { index = 0.0.1.2, path = Technology->CommonLanguages->Java->null }",
                "ORM { index = 0.0.1.3, path = Technology->CommonLanguages->Java->ORM }",
                "RMI { index = 0.0.1.4, path = Technology->CommonLanguages->Java->RMI }",
                "EJB { index = 0.0.1.5, path = Technology->CommonLanguages->Java->EJB }",
                "WebService { index = 0.0.1.6, path = Technology->CommonLanguages->Java->WebService }",
                "Spring { index = 0.0.1.7, path = Technology->CommonLanguages->Java->Spring }",
                "null { index = 0.0.1.8, path = Technology->CommonLanguages->Java->null }",
                "Swing { index = 0.0.1.9, path = Technology->CommonLanguages->Java->Swing }",
                "SWT { index = 0.0.1.10, path = Technology->CommonLanguages->Java->SWT }",
                "WinForm { index = 0.0.2.0, path = Technology->CommonLanguages->.NET->WinForm }",
                "WPF { index = 0.0.2.1, path = Technology->CommonLanguages->.NET->WPF }",
                "ASP.NET { index = 0.0.2.2, path = Technology->CommonLanguages->.NET->ASP.NET }",
                "null { index = 0.0.2.3, path = Technology->CommonLanguages->.NET->null }",
                "ADO.NET { index = 0.0.2.4, path = Technology->CommonLanguages->.NET->ADO.NET }",
                "COM+ { index = 0.0.2.5, path = Technology->CommonLanguages->.NET->COM+ }",
                ".NET Remoting { index = 0.0.2.6, path = Technology->CommonLanguages->.NET->.NET Remoting }",
                "WebService { index = 0.0.2.7, path = Technology->CommonLanguages->.NET->WebService }",
                "Spring.NET { index = 0.0.2.8, path = Technology->CommonLanguages->.NET->Spring.NET }",
                "null { index = 0.0.2.9, path = Technology->CommonLanguages->.NET->null }",
                "ORM { index = 0.0.2.10, path = Technology->CommonLanguages->.NET->ORM }",
                "Oracle11g { index = 0.1.0.0, path = Technology->Database->Oracle->Oracle11g }",
                
                "Direct3D { index = 0.0.0.6.0, path = Technology->CommonLanguages->C++->DirectX->Direct3D }",
                "DirectDraw { index = 0.0.0.6.1, path = Technology->CommonLanguages->C++->DirectX->DirectDraw }",
                "DirectInput { index = 0.0.0.6.2, path = Technology->CommonLanguages->C++->DirectX->DirectInput }",
                "Hibernate { index = 0.0.1.3.0, path = Technology->CommonLanguages->Java->ORM->Hibernate }",
                "null { index = 0.0.1.3.1, path = Technology->CommonLanguages->Java->ORM->null }",
                "myBatis { index = 0.0.1.3.2, path = Technology->CommonLanguages->Java->ORM->myBatis }",
                "JPA { index = 0.0.1.3.3, path = Technology->CommonLanguages->Java->ORM->JPA }",
                "EJB2.1 { index = 0.0.1.5.0, path = Technology->CommonLanguages->Java->EJB->EJB2.1 }",
                "EJB3 { index = 0.0.1.5.1, path = Technology->CommonLanguages->Java->EJB->EJB3 }",
                "NHibernate { index = 0.0.2.10.0, path = Technology->CommonLanguages->.NET->ORM->NHibernate }",
                "null { index = 0.0.2.10.1, path = Technology->CommonLanguages->.NET->ORM->null }",
                "ADO.NET EntityFramework { index = 0.0.2.10.2, path = Technology->CommonLanguages->.NET->ORM->ADO.NET EntityFramework }",
                "Oracle11g SQL { index = 0.1.0.0.0, path = Technology->Database->Oracle->Oracle11g->Oracle11g SQL }",
                "null { index = 0.1.0.0.1, path = Technology->Database->Oracle->Oracle11g->null }",
                "Oracle11g RMAN { index = 0.1.0.0.2, path = Technology->Database->Oracle->Oracle11g->Oracle11g RMAN }",
                "Oracle11g DBA { index = 0.1.0.0.3, path = Technology->Database->Oracle->Oracle11g->Oracle11g DBA }");
        assertList(
                afterList,
                
                "Direct3D { index = 0.0.0.6.0, path = Technology->CommonLanguages->C++->DirectX->Direct3D }",
                "DirectDraw { index = 0.0.0.6.1, path = Technology->CommonLanguages->C++->DirectX->DirectDraw }",
                "DirectInput { index = 0.0.0.6.2, path = Technology->CommonLanguages->C++->DirectX->DirectInput }",
                "Hibernate { index = 0.0.1.3.0, path = Technology->CommonLanguages->Java->ORM->Hibernate }",
                "null { index = 0.0.1.3.1, path = Technology->CommonLanguages->Java->ORM->null }",
                "myBatis { index = 0.0.1.3.2, path = Technology->CommonLanguages->Java->ORM->myBatis }",
                "JPA { index = 0.0.1.3.3, path = Technology->CommonLanguages->Java->ORM->JPA }",
                "EJB2.1 { index = 0.0.1.5.0, path = Technology->CommonLanguages->Java->EJB->EJB2.1 }",
                "EJB3 { index = 0.0.1.5.1, path = Technology->CommonLanguages->Java->EJB->EJB3 }",
                "NHibernate { index = 0.0.2.10.0, path = Technology->CommonLanguages->.NET->ORM->NHibernate }",
                "null { index = 0.0.2.10.1, path = Technology->CommonLanguages->.NET->ORM->null }",
                "ADO.NET EntityFramework { index = 0.0.2.10.2, path = Technology->CommonLanguages->.NET->ORM->ADO.NET EntityFramework }",
                "Oracle11g SQL { index = 0.1.0.0.0, path = Technology->Database->Oracle->Oracle11g->Oracle11g SQL }",
                "null { index = 0.1.0.0.1, path = Technology->Database->Oracle->Oracle11g->null }",
                "Oracle11g RMAN { index = 0.1.0.0.2, path = Technology->Database->Oracle->Oracle11g->Oracle11g RMAN }",
                "Oracle11g DBA { index = 0.1.0.0.3, path = Technology->Database->Oracle->Oracle11g->Oracle11g DBA }",
                
                "Win32 { index = 0.0.0.0, path = Technology->CommonLanguages->C++->Win32 }",
                "Socket { index = 0.0.0.1, path = Technology->CommonLanguages->C++->Socket }",
                "null { index = 0.0.0.2, path = Technology->CommonLanguages->C++->null }",
                "MFC { index = 0.0.0.3, path = Technology->CommonLanguages->C++->MFC }",
                "ATL { index = 0.0.0.4, path = Technology->CommonLanguages->C++->ATL }",
                "null { index = 0.0.0.5, path = Technology->CommonLanguages->C++->null }",
                "DirectX { index = 0.0.0.6, path = Technology->CommonLanguages->C++->DirectX }",
                "CGLIB { index = 0.0.1.0, path = Technology->CommonLanguages->Java->CGLIB }",
                "ASM { index = 0.0.1.1, path = Technology->CommonLanguages->Java->ASM }",
                "null { index = 0.0.1.2, path = Technology->CommonLanguages->Java->null }",
                "ORM { index = 0.0.1.3, path = Technology->CommonLanguages->Java->ORM }",
                "RMI { index = 0.0.1.4, path = Technology->CommonLanguages->Java->RMI }",
                "EJB { index = 0.0.1.5, path = Technology->CommonLanguages->Java->EJB }",
                "WebService { index = 0.0.1.6, path = Technology->CommonLanguages->Java->WebService }",
                "Spring { index = 0.0.1.7, path = Technology->CommonLanguages->Java->Spring }",
                "null { index = 0.0.1.8, path = Technology->CommonLanguages->Java->null }",
                "Swing { index = 0.0.1.9, path = Technology->CommonLanguages->Java->Swing }",
                "SWT { index = 0.0.1.10, path = Technology->CommonLanguages->Java->SWT }",
                "WinForm { index = 0.0.2.0, path = Technology->CommonLanguages->.NET->WinForm }",
                "WPF { index = 0.0.2.1, path = Technology->CommonLanguages->.NET->WPF }",
                "ASP.NET { index = 0.0.2.2, path = Technology->CommonLanguages->.NET->ASP.NET }",
                "null { index = 0.0.2.3, path = Technology->CommonLanguages->.NET->null }",
                "ADO.NET { index = 0.0.2.4, path = Technology->CommonLanguages->.NET->ADO.NET }",
                "COM+ { index = 0.0.2.5, path = Technology->CommonLanguages->.NET->COM+ }",
                ".NET Remoting { index = 0.0.2.6, path = Technology->CommonLanguages->.NET->.NET Remoting }",
                "WebService { index = 0.0.2.7, path = Technology->CommonLanguages->.NET->WebService }",
                "Spring.NET { index = 0.0.2.8, path = Technology->CommonLanguages->.NET->Spring.NET }",
                "null { index = 0.0.2.9, path = Technology->CommonLanguages->.NET->null }",
                "ORM { index = 0.0.2.10, path = Technology->CommonLanguages->.NET->ORM }",
                "Oracle11g { index = 0.1.0.0, path = Technology->Database->Oracle->Oracle11g }",
                
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "null { index = 0.1.1, path = Technology->Database->null }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "null { index = 0.2.2, path = Technology->XML->null }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "null { index = 0.4.1, path = Technology->WebClient->null }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "null { index = 0.4.4, path = Technology->WebClient->null }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }",
                
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "Database { index = 0.1, path = Technology->Database }",
                "XML { index = 0.2, path = Technology->XML }",
                "null { index = 0.3, path = Technology->null }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                
                "Technology { index = 0, path = Technology }");
    }
    
    @Test
    public void testNoSocketSiblingNodesTreeDepthFirst() {
        List<String> beforeList = new ArrayList<String>();
        List<String> afterList = new ArrayList<String>();
        new NoSocketSiblingNodesTreeTraveler(beforeList, afterList).depthFirstTravel(createTree());
        Assert.assertEquals(beforeList.size(), afterList.size());
        Assert.assertEquals(50, beforeList.size());
        Assert.assertEquals(50, afterList.size());
        assertList(
                beforeList,
                "Technology { index = 0, path = Technology }",
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "Win32 { index = 0.0.0.0, path = Technology->CommonLanguages->C++->Win32 }",
                "Socket { index = 0.0.0.1, path = Technology->CommonLanguages->C++->Socket }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                "CGLIB { index = 0.0.1.0, path = Technology->CommonLanguages->Java->CGLIB }",
                "ASM { index = 0.0.1.1, path = Technology->CommonLanguages->Java->ASM }",
                "ORM { index = 0.0.1.3, path = Technology->CommonLanguages->Java->ORM }",
                "Hibernate { index = 0.0.1.3.0, path = Technology->CommonLanguages->Java->ORM->Hibernate }",
                "myBatis { index = 0.0.1.3.2, path = Technology->CommonLanguages->Java->ORM->myBatis }",
                "JPA { index = 0.0.1.3.3, path = Technology->CommonLanguages->Java->ORM->JPA }",
                "RMI { index = 0.0.1.4, path = Technology->CommonLanguages->Java->RMI }",
                "EJB { index = 0.0.1.5, path = Technology->CommonLanguages->Java->EJB }",
                "EJB2.1 { index = 0.0.1.5.0, path = Technology->CommonLanguages->Java->EJB->EJB2.1 }",
                "EJB3 { index = 0.0.1.5.1, path = Technology->CommonLanguages->Java->EJB->EJB3 }",
                "WebService { index = 0.0.1.6, path = Technology->CommonLanguages->Java->WebService }",
                "Spring { index = 0.0.1.7, path = Technology->CommonLanguages->Java->Spring }",
                "Swing { index = 0.0.1.9, path = Technology->CommonLanguages->Java->Swing }",
                "SWT { index = 0.0.1.10, path = Technology->CommonLanguages->Java->SWT }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "WinForm { index = 0.0.2.0, path = Technology->CommonLanguages->.NET->WinForm }",
                "WPF { index = 0.0.2.1, path = Technology->CommonLanguages->.NET->WPF }",
                "ASP.NET { index = 0.0.2.2, path = Technology->CommonLanguages->.NET->ASP.NET }",
                "ADO.NET { index = 0.0.2.4, path = Technology->CommonLanguages->.NET->ADO.NET }",
                "COM+ { index = 0.0.2.5, path = Technology->CommonLanguages->.NET->COM+ }",
                ".NET Remoting { index = 0.0.2.6, path = Technology->CommonLanguages->.NET->.NET Remoting }",
                "WebService { index = 0.0.2.7, path = Technology->CommonLanguages->.NET->WebService }",
                "Spring.NET { index = 0.0.2.8, path = Technology->CommonLanguages->.NET->Spring.NET }",
                "ORM { index = 0.0.2.10, path = Technology->CommonLanguages->.NET->ORM }",
                "NHibernate { index = 0.0.2.10.0, path = Technology->CommonLanguages->.NET->ORM->NHibernate }",
                "ADO.NET EntityFramework { index = 0.0.2.10.2, path = Technology->CommonLanguages->.NET->ORM->ADO.NET EntityFramework }",
                "Database { index = 0.1, path = Technology->Database }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "Oracle11g { index = 0.1.0.0, path = Technology->Database->Oracle->Oracle11g }",
                "Oracle11g SQL { index = 0.1.0.0.0, path = Technology->Database->Oracle->Oracle11g->Oracle11g SQL }",
                "Oracle11g RMAN { index = 0.1.0.0.2, path = Technology->Database->Oracle->Oracle11g->Oracle11g RMAN }",
                "Oracle11g DBA { index = 0.1.0.0.3, path = Technology->Database->Oracle->Oracle11g->Oracle11g DBA }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "XML { index = 0.2, path = Technology->XML }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }");
        assertList(
                afterList,
                "Win32 { index = 0.0.0.0, path = Technology->CommonLanguages->C++->Win32 }",
                "Socket { index = 0.0.0.1, path = Technology->CommonLanguages->C++->Socket }",
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "CGLIB { index = 0.0.1.0, path = Technology->CommonLanguages->Java->CGLIB }",
                "ASM { index = 0.0.1.1, path = Technology->CommonLanguages->Java->ASM }",
                "Hibernate { index = 0.0.1.3.0, path = Technology->CommonLanguages->Java->ORM->Hibernate }",
                "myBatis { index = 0.0.1.3.2, path = Technology->CommonLanguages->Java->ORM->myBatis }",
                "JPA { index = 0.0.1.3.3, path = Technology->CommonLanguages->Java->ORM->JPA }",
                "ORM { index = 0.0.1.3, path = Technology->CommonLanguages->Java->ORM }",
                "RMI { index = 0.0.1.4, path = Technology->CommonLanguages->Java->RMI }",
                "EJB2.1 { index = 0.0.1.5.0, path = Technology->CommonLanguages->Java->EJB->EJB2.1 }",
                "EJB3 { index = 0.0.1.5.1, path = Technology->CommonLanguages->Java->EJB->EJB3 }",
                "EJB { index = 0.0.1.5, path = Technology->CommonLanguages->Java->EJB }",
                "WebService { index = 0.0.1.6, path = Technology->CommonLanguages->Java->WebService }",
                "Spring { index = 0.0.1.7, path = Technology->CommonLanguages->Java->Spring }",
                "Swing { index = 0.0.1.9, path = Technology->CommonLanguages->Java->Swing }",
                "SWT { index = 0.0.1.10, path = Technology->CommonLanguages->Java->SWT }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                "WinForm { index = 0.0.2.0, path = Technology->CommonLanguages->.NET->WinForm }",
                "WPF { index = 0.0.2.1, path = Technology->CommonLanguages->.NET->WPF }",
                "ASP.NET { index = 0.0.2.2, path = Technology->CommonLanguages->.NET->ASP.NET }",
                "ADO.NET { index = 0.0.2.4, path = Technology->CommonLanguages->.NET->ADO.NET }",
                "COM+ { index = 0.0.2.5, path = Technology->CommonLanguages->.NET->COM+ }",
                ".NET Remoting { index = 0.0.2.6, path = Technology->CommonLanguages->.NET->.NET Remoting }",
                "WebService { index = 0.0.2.7, path = Technology->CommonLanguages->.NET->WebService }",
                "Spring.NET { index = 0.0.2.8, path = Technology->CommonLanguages->.NET->Spring.NET }",
                "NHibernate { index = 0.0.2.10.0, path = Technology->CommonLanguages->.NET->ORM->NHibernate }",
                "ADO.NET EntityFramework { index = 0.0.2.10.2, path = Technology->CommonLanguages->.NET->ORM->ADO.NET EntityFramework }",
                "ORM { index = 0.0.2.10, path = Technology->CommonLanguages->.NET->ORM }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "Oracle11g SQL { index = 0.1.0.0.0, path = Technology->Database->Oracle->Oracle11g->Oracle11g SQL }",
                "Oracle11g RMAN { index = 0.1.0.0.2, path = Technology->Database->Oracle->Oracle11g->Oracle11g RMAN }",
                "Oracle11g DBA { index = 0.1.0.0.3, path = Technology->Database->Oracle->Oracle11g->Oracle11g DBA }",
                "Oracle11g { index = 0.1.0.0, path = Technology->Database->Oracle->Oracle11g }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "Database { index = 0.1, path = Technology->Database }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "XML { index = 0.2, path = Technology->XML }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                "Technology { index = 0, path = Technology }");
    }
    
    @Test
    public void testNoSocketSiblingTreeBreadthFirst() {
        List<String> beforeList = new ArrayList<String>();
        List<String> afterList = new ArrayList<String>();
        new NoSocketSiblingNodesTreeTraveler(beforeList, afterList).breadthFirstTravel(createTree());
        Assert.assertEquals(50, beforeList.size());
        Assert.assertEquals(50, afterList.size());
        assertList(
                beforeList,
                "Technology { index = 0, path = Technology }",
                
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "Database { index = 0.1, path = Technology->Database }",
                "XML { index = 0.2, path = Technology->XML }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }",
                
                "Win32 { index = 0.0.0.0, path = Technology->CommonLanguages->C++->Win32 }",
                "Socket { index = 0.0.0.1, path = Technology->CommonLanguages->C++->Socket }",
                "CGLIB { index = 0.0.1.0, path = Technology->CommonLanguages->Java->CGLIB }",
                "ASM { index = 0.0.1.1, path = Technology->CommonLanguages->Java->ASM }",
                "ORM { index = 0.0.1.3, path = Technology->CommonLanguages->Java->ORM }",
                "RMI { index = 0.0.1.4, path = Technology->CommonLanguages->Java->RMI }",
                "EJB { index = 0.0.1.5, path = Technology->CommonLanguages->Java->EJB }",
                "WebService { index = 0.0.1.6, path = Technology->CommonLanguages->Java->WebService }",
                "Spring { index = 0.0.1.7, path = Technology->CommonLanguages->Java->Spring }",
                "Swing { index = 0.0.1.9, path = Technology->CommonLanguages->Java->Swing }",
                "SWT { index = 0.0.1.10, path = Technology->CommonLanguages->Java->SWT }",
                "WinForm { index = 0.0.2.0, path = Technology->CommonLanguages->.NET->WinForm }",
                "WPF { index = 0.0.2.1, path = Technology->CommonLanguages->.NET->WPF }",
                "ASP.NET { index = 0.0.2.2, path = Technology->CommonLanguages->.NET->ASP.NET }",
                "ADO.NET { index = 0.0.2.4, path = Technology->CommonLanguages->.NET->ADO.NET }",
                "COM+ { index = 0.0.2.5, path = Technology->CommonLanguages->.NET->COM+ }",
                ".NET Remoting { index = 0.0.2.6, path = Technology->CommonLanguages->.NET->.NET Remoting }",
                "WebService { index = 0.0.2.7, path = Technology->CommonLanguages->.NET->WebService }",
                "Spring.NET { index = 0.0.2.8, path = Technology->CommonLanguages->.NET->Spring.NET }",
                "ORM { index = 0.0.2.10, path = Technology->CommonLanguages->.NET->ORM }",
                "Oracle11g { index = 0.1.0.0, path = Technology->Database->Oracle->Oracle11g }",
                
                "Hibernate { index = 0.0.1.3.0, path = Technology->CommonLanguages->Java->ORM->Hibernate }",
                "myBatis { index = 0.0.1.3.2, path = Technology->CommonLanguages->Java->ORM->myBatis }",
                "JPA { index = 0.0.1.3.3, path = Technology->CommonLanguages->Java->ORM->JPA }",
                "EJB2.1 { index = 0.0.1.5.0, path = Technology->CommonLanguages->Java->EJB->EJB2.1 }",
                "EJB3 { index = 0.0.1.5.1, path = Technology->CommonLanguages->Java->EJB->EJB3 }",
                "NHibernate { index = 0.0.2.10.0, path = Technology->CommonLanguages->.NET->ORM->NHibernate }",
                "ADO.NET EntityFramework { index = 0.0.2.10.2, path = Technology->CommonLanguages->.NET->ORM->ADO.NET EntityFramework }",
                "Oracle11g SQL { index = 0.1.0.0.0, path = Technology->Database->Oracle->Oracle11g->Oracle11g SQL }",
                "Oracle11g RMAN { index = 0.1.0.0.2, path = Technology->Database->Oracle->Oracle11g->Oracle11g RMAN }",
                "Oracle11g DBA { index = 0.1.0.0.3, path = Technology->Database->Oracle->Oracle11g->Oracle11g DBA }");
        assertList(
                afterList,
                
                "Hibernate { index = 0.0.1.3.0, path = Technology->CommonLanguages->Java->ORM->Hibernate }",
                "myBatis { index = 0.0.1.3.2, path = Technology->CommonLanguages->Java->ORM->myBatis }",
                "JPA { index = 0.0.1.3.3, path = Technology->CommonLanguages->Java->ORM->JPA }",
                "EJB2.1 { index = 0.0.1.5.0, path = Technology->CommonLanguages->Java->EJB->EJB2.1 }",
                "EJB3 { index = 0.0.1.5.1, path = Technology->CommonLanguages->Java->EJB->EJB3 }",
                "NHibernate { index = 0.0.2.10.0, path = Technology->CommonLanguages->.NET->ORM->NHibernate }",
                "ADO.NET EntityFramework { index = 0.0.2.10.2, path = Technology->CommonLanguages->.NET->ORM->ADO.NET EntityFramework }",
                "Oracle11g SQL { index = 0.1.0.0.0, path = Technology->Database->Oracle->Oracle11g->Oracle11g SQL }",
                "Oracle11g RMAN { index = 0.1.0.0.2, path = Technology->Database->Oracle->Oracle11g->Oracle11g RMAN }",
                "Oracle11g DBA { index = 0.1.0.0.3, path = Technology->Database->Oracle->Oracle11g->Oracle11g DBA }",
                
                "Win32 { index = 0.0.0.0, path = Technology->CommonLanguages->C++->Win32 }",
                "Socket { index = 0.0.0.1, path = Technology->CommonLanguages->C++->Socket }",
                "CGLIB { index = 0.0.1.0, path = Technology->CommonLanguages->Java->CGLIB }",
                "ASM { index = 0.0.1.1, path = Technology->CommonLanguages->Java->ASM }",
                "ORM { index = 0.0.1.3, path = Technology->CommonLanguages->Java->ORM }",
                "RMI { index = 0.0.1.4, path = Technology->CommonLanguages->Java->RMI }",
                "EJB { index = 0.0.1.5, path = Technology->CommonLanguages->Java->EJB }",
                "WebService { index = 0.0.1.6, path = Technology->CommonLanguages->Java->WebService }",
                "Spring { index = 0.0.1.7, path = Technology->CommonLanguages->Java->Spring }",
                "Swing { index = 0.0.1.9, path = Technology->CommonLanguages->Java->Swing }",
                "SWT { index = 0.0.1.10, path = Technology->CommonLanguages->Java->SWT }",
                "WinForm { index = 0.0.2.0, path = Technology->CommonLanguages->.NET->WinForm }",
                "WPF { index = 0.0.2.1, path = Technology->CommonLanguages->.NET->WPF }",
                "ASP.NET { index = 0.0.2.2, path = Technology->CommonLanguages->.NET->ASP.NET }",
                "ADO.NET { index = 0.0.2.4, path = Technology->CommonLanguages->.NET->ADO.NET }",
                "COM+ { index = 0.0.2.5, path = Technology->CommonLanguages->.NET->COM+ }",
                ".NET Remoting { index = 0.0.2.6, path = Technology->CommonLanguages->.NET->.NET Remoting }",
                "WebService { index = 0.0.2.7, path = Technology->CommonLanguages->.NET->WebService }",
                "Spring.NET { index = 0.0.2.8, path = Technology->CommonLanguages->.NET->Spring.NET }",
                "ORM { index = 0.0.2.10, path = Technology->CommonLanguages->.NET->ORM }",
                "Oracle11g { index = 0.1.0.0, path = Technology->Database->Oracle->Oracle11g }",
                
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }",
                
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "Database { index = 0.1, path = Technology->Database }",
                "XML { index = 0.2, path = Technology->XML }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                
                "Technology { index = 0, path = Technology }");
    }
    
    @Test
    public void testNoDotNetNeighborNodesTreeDepthFirst() {
        List<String> beforeList = new ArrayList<String>();
        List<String> afterList = new ArrayList<String>();
        new NoDotNetNeighborNodesTreeTraveler(beforeList, afterList).depthFirstTravel(createTree());
        Assert.assertEquals(45, beforeList.size());
        Assert.assertEquals(45, afterList.size());
        assertList(
                beforeList,
                "Technology { index = 0, path = Technology }",
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "Win32 { index = 0.0.0.0, path = Technology->CommonLanguages->C++->Win32 }",
                "Socket { index = 0.0.0.1, path = Technology->CommonLanguages->C++->Socket }",
                "MFC { index = 0.0.0.3, path = Technology->CommonLanguages->C++->MFC }",
                "ATL { index = 0.0.0.4, path = Technology->CommonLanguages->C++->ATL }",
                "DirectX { index = 0.0.0.6, path = Technology->CommonLanguages->C++->DirectX }",
                "Direct3D { index = 0.0.0.6.0, path = Technology->CommonLanguages->C++->DirectX->Direct3D }",
                "DirectDraw { index = 0.0.0.6.1, path = Technology->CommonLanguages->C++->DirectX->DirectDraw }",
                "DirectInput { index = 0.0.0.6.2, path = Technology->CommonLanguages->C++->DirectX->DirectInput }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                "CGLIB { index = 0.0.1.0, path = Technology->CommonLanguages->Java->CGLIB }",
                "ASM { index = 0.0.1.1, path = Technology->CommonLanguages->Java->ASM }",
                "ORM { index = 0.0.1.3, path = Technology->CommonLanguages->Java->ORM }",
                "Hibernate { index = 0.0.1.3.0, path = Technology->CommonLanguages->Java->ORM->Hibernate }",
                "myBatis { index = 0.0.1.3.2, path = Technology->CommonLanguages->Java->ORM->myBatis }",
                "JPA { index = 0.0.1.3.3, path = Technology->CommonLanguages->Java->ORM->JPA }",
                "RMI { index = 0.0.1.4, path = Technology->CommonLanguages->Java->RMI }",
                "EJB { index = 0.0.1.5, path = Technology->CommonLanguages->Java->EJB }",
                "EJB2.1 { index = 0.0.1.5.0, path = Technology->CommonLanguages->Java->EJB->EJB2.1 }",
                "EJB3 { index = 0.0.1.5.1, path = Technology->CommonLanguages->Java->EJB->EJB3 }",
                "WebService { index = 0.0.1.6, path = Technology->CommonLanguages->Java->WebService }",
                "Spring { index = 0.0.1.7, path = Technology->CommonLanguages->Java->Spring }",
                "Swing { index = 0.0.1.9, path = Technology->CommonLanguages->Java->Swing }",
                "SWT { index = 0.0.1.10, path = Technology->CommonLanguages->Java->SWT }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "Database { index = 0.1, path = Technology->Database }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "Oracle11g { index = 0.1.0.0, path = Technology->Database->Oracle->Oracle11g }",
                "Oracle11g SQL { index = 0.1.0.0.0, path = Technology->Database->Oracle->Oracle11g->Oracle11g SQL }",
                "Oracle11g RMAN { index = 0.1.0.0.2, path = Technology->Database->Oracle->Oracle11g->Oracle11g RMAN }",
                "Oracle11g DBA { index = 0.1.0.0.3, path = Technology->Database->Oracle->Oracle11g->Oracle11g DBA }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "XML { index = 0.2, path = Technology->XML }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }");
        assertList(
                afterList,
                "Win32 { index = 0.0.0.0, path = Technology->CommonLanguages->C++->Win32 }",
                "Socket { index = 0.0.0.1, path = Technology->CommonLanguages->C++->Socket }",
                "MFC { index = 0.0.0.3, path = Technology->CommonLanguages->C++->MFC }",
                "ATL { index = 0.0.0.4, path = Technology->CommonLanguages->C++->ATL }",
                "Direct3D { index = 0.0.0.6.0, path = Technology->CommonLanguages->C++->DirectX->Direct3D }",
                "DirectDraw { index = 0.0.0.6.1, path = Technology->CommonLanguages->C++->DirectX->DirectDraw }",
                "DirectInput { index = 0.0.0.6.2, path = Technology->CommonLanguages->C++->DirectX->DirectInput }",
                "DirectX { index = 0.0.0.6, path = Technology->CommonLanguages->C++->DirectX }",
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "CGLIB { index = 0.0.1.0, path = Technology->CommonLanguages->Java->CGLIB }",
                "ASM { index = 0.0.1.1, path = Technology->CommonLanguages->Java->ASM }",
                "Hibernate { index = 0.0.1.3.0, path = Technology->CommonLanguages->Java->ORM->Hibernate }",
                "myBatis { index = 0.0.1.3.2, path = Technology->CommonLanguages->Java->ORM->myBatis }",
                "JPA { index = 0.0.1.3.3, path = Technology->CommonLanguages->Java->ORM->JPA }",
                "ORM { index = 0.0.1.3, path = Technology->CommonLanguages->Java->ORM }",
                "RMI { index = 0.0.1.4, path = Technology->CommonLanguages->Java->RMI }",
                "EJB2.1 { index = 0.0.1.5.0, path = Technology->CommonLanguages->Java->EJB->EJB2.1 }",
                "EJB3 { index = 0.0.1.5.1, path = Technology->CommonLanguages->Java->EJB->EJB3 }",
                "EJB { index = 0.0.1.5, path = Technology->CommonLanguages->Java->EJB }",
                "WebService { index = 0.0.1.6, path = Technology->CommonLanguages->Java->WebService }",
                "Spring { index = 0.0.1.7, path = Technology->CommonLanguages->Java->Spring }",
                "Swing { index = 0.0.1.9, path = Technology->CommonLanguages->Java->Swing }",
                "SWT { index = 0.0.1.10, path = Technology->CommonLanguages->Java->SWT }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "Oracle11g SQL { index = 0.1.0.0.0, path = Technology->Database->Oracle->Oracle11g->Oracle11g SQL }",
                "Oracle11g RMAN { index = 0.1.0.0.2, path = Technology->Database->Oracle->Oracle11g->Oracle11g RMAN }",
                "Oracle11g DBA { index = 0.1.0.0.3, path = Technology->Database->Oracle->Oracle11g->Oracle11g DBA }",
                "Oracle11g { index = 0.1.0.0, path = Technology->Database->Oracle->Oracle11g }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "Database { index = 0.1, path = Technology->Database }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "XML { index = 0.2, path = Technology->XML }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                "Technology { index = 0, path = Technology }");
    }
    
    @Test
    public void testNoDotNetNeighborNodesTreeBreadthFirst() {
        List<String> beforeList = new ArrayList<String>();
        List<String> afterList = new ArrayList<String>();
        new NoDotNetNeighborNodesTreeTraveler(beforeList, afterList).breadthFirstTravel(createTree());
        Assert.assertEquals(45, beforeList.size());
        Assert.assertEquals(45, afterList.size());
        assertList(
                beforeList,
                "Technology { index = 0, path = Technology }",
                
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "Database { index = 0.1, path = Technology->Database }",
                "XML { index = 0.2, path = Technology->XML }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }",
                
                "Win32 { index = 0.0.0.0, path = Technology->CommonLanguages->C++->Win32 }",
                "Socket { index = 0.0.0.1, path = Technology->CommonLanguages->C++->Socket }",
                "MFC { index = 0.0.0.3, path = Technology->CommonLanguages->C++->MFC }",
                "ATL { index = 0.0.0.4, path = Technology->CommonLanguages->C++->ATL }",
                "DirectX { index = 0.0.0.6, path = Technology->CommonLanguages->C++->DirectX }",
                "CGLIB { index = 0.0.1.0, path = Technology->CommonLanguages->Java->CGLIB }",
                "ASM { index = 0.0.1.1, path = Technology->CommonLanguages->Java->ASM }",
                "ORM { index = 0.0.1.3, path = Technology->CommonLanguages->Java->ORM }",
                "RMI { index = 0.0.1.4, path = Technology->CommonLanguages->Java->RMI }",
                "EJB { index = 0.0.1.5, path = Technology->CommonLanguages->Java->EJB }",
                "WebService { index = 0.0.1.6, path = Technology->CommonLanguages->Java->WebService }",
                "Spring { index = 0.0.1.7, path = Technology->CommonLanguages->Java->Spring }",
                "Swing { index = 0.0.1.9, path = Technology->CommonLanguages->Java->Swing }",
                "SWT { index = 0.0.1.10, path = Technology->CommonLanguages->Java->SWT }",
                "Oracle11g { index = 0.1.0.0, path = Technology->Database->Oracle->Oracle11g }",
                
                "Direct3D { index = 0.0.0.6.0, path = Technology->CommonLanguages->C++->DirectX->Direct3D }",
                "DirectDraw { index = 0.0.0.6.1, path = Technology->CommonLanguages->C++->DirectX->DirectDraw }",
                "DirectInput { index = 0.0.0.6.2, path = Technology->CommonLanguages->C++->DirectX->DirectInput }",
                "Hibernate { index = 0.0.1.3.0, path = Technology->CommonLanguages->Java->ORM->Hibernate }",
                "myBatis { index = 0.0.1.3.2, path = Technology->CommonLanguages->Java->ORM->myBatis }",
                "JPA { index = 0.0.1.3.3, path = Technology->CommonLanguages->Java->ORM->JPA }",
                "EJB2.1 { index = 0.0.1.5.0, path = Technology->CommonLanguages->Java->EJB->EJB2.1 }",
                "EJB3 { index = 0.0.1.5.1, path = Technology->CommonLanguages->Java->EJB->EJB3 }",
                "Oracle11g SQL { index = 0.1.0.0.0, path = Technology->Database->Oracle->Oracle11g->Oracle11g SQL }",
                "Oracle11g RMAN { index = 0.1.0.0.2, path = Technology->Database->Oracle->Oracle11g->Oracle11g RMAN }",
                "Oracle11g DBA { index = 0.1.0.0.3, path = Technology->Database->Oracle->Oracle11g->Oracle11g DBA }");
        assertList(
                afterList,
                
                "Direct3D { index = 0.0.0.6.0, path = Technology->CommonLanguages->C++->DirectX->Direct3D }",
                "DirectDraw { index = 0.0.0.6.1, path = Technology->CommonLanguages->C++->DirectX->DirectDraw }",
                "DirectInput { index = 0.0.0.6.2, path = Technology->CommonLanguages->C++->DirectX->DirectInput }",
                "Hibernate { index = 0.0.1.3.0, path = Technology->CommonLanguages->Java->ORM->Hibernate }",
                "myBatis { index = 0.0.1.3.2, path = Technology->CommonLanguages->Java->ORM->myBatis }",
                "JPA { index = 0.0.1.3.3, path = Technology->CommonLanguages->Java->ORM->JPA }",
                "EJB2.1 { index = 0.0.1.5.0, path = Technology->CommonLanguages->Java->EJB->EJB2.1 }",
                "EJB3 { index = 0.0.1.5.1, path = Technology->CommonLanguages->Java->EJB->EJB3 }",
                "Oracle11g SQL { index = 0.1.0.0.0, path = Technology->Database->Oracle->Oracle11g->Oracle11g SQL }",
                "Oracle11g RMAN { index = 0.1.0.0.2, path = Technology->Database->Oracle->Oracle11g->Oracle11g RMAN }",
                "Oracle11g DBA { index = 0.1.0.0.3, path = Technology->Database->Oracle->Oracle11g->Oracle11g DBA }",
                
                "Win32 { index = 0.0.0.0, path = Technology->CommonLanguages->C++->Win32 }",
                "Socket { index = 0.0.0.1, path = Technology->CommonLanguages->C++->Socket }",
                "MFC { index = 0.0.0.3, path = Technology->CommonLanguages->C++->MFC }",
                "ATL { index = 0.0.0.4, path = Technology->CommonLanguages->C++->ATL }",
                "DirectX { index = 0.0.0.6, path = Technology->CommonLanguages->C++->DirectX }",
                "CGLIB { index = 0.0.1.0, path = Technology->CommonLanguages->Java->CGLIB }",
                "ASM { index = 0.0.1.1, path = Technology->CommonLanguages->Java->ASM }",
                "ORM { index = 0.0.1.3, path = Technology->CommonLanguages->Java->ORM }",
                "RMI { index = 0.0.1.4, path = Technology->CommonLanguages->Java->RMI }",
                "EJB { index = 0.0.1.5, path = Technology->CommonLanguages->Java->EJB }",
                "WebService { index = 0.0.1.6, path = Technology->CommonLanguages->Java->WebService }",
                "Spring { index = 0.0.1.7, path = Technology->CommonLanguages->Java->Spring }",
                "Swing { index = 0.0.1.9, path = Technology->CommonLanguages->Java->Swing }",
                "SWT { index = 0.0.1.10, path = Technology->CommonLanguages->Java->SWT }",
                "Oracle11g { index = 0.1.0.0, path = Technology->Database->Oracle->Oracle11g }",
                
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }",
                
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "Database { index = 0.1, path = Technology->Database }",
                "XML { index = 0.2, path = Technology->XML }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                
                "Technology { index = 0, path = Technology }");
    }
    
    @Test
    public void testThreeLayerTreeDepthFirst() {
        List<String> beforeList = new ArrayList<String>();
        List<String> afterList = new ArrayList<String>();
        new ThreeLayerTreeTraveler(beforeList, afterList).depthFirstTravel(createTree());
        Assert.assertEquals(19, beforeList.size());
        Assert.assertEquals(19, afterList.size());
        assertList(
                beforeList,
                "Technology { index = 0, path = Technology }",
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "Database { index = 0.1, path = Technology->Database }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "XML { index = 0.2, path = Technology->XML }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }");
        assertList(
                afterList,
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "Database { index = 0.1, path = Technology->Database }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "XML { index = 0.2, path = Technology->XML }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                "Technology { index = 0, path = Technology }");
    }
    
    @Test
    public void testThreeLayerTreeBreadthFirst() {
        List<String> beforeList = new ArrayList<String>();
        List<String> afterList = new ArrayList<String>();
        new ThreeLayerTreeTraveler(beforeList, afterList).breadthFirstTravel(createTree());
        Assert.assertEquals(19, beforeList.size());
        Assert.assertEquals(19, afterList.size());
        assertList(
                beforeList,
                "Technology { index = 0, path = Technology }",
                
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "Database { index = 0.1, path = Technology->Database }",
                "XML { index = 0.2, path = Technology->XML }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }");
        assertList(
                afterList,
                
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }",
                
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "Database { index = 0.1, path = Technology->Database }",
                "XML { index = 0.2, path = Technology->XML }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                
                "Technology { index = 0, path = Technology }");
    }
    
    @Test
    public void testBreakAtASPDotNetTreeDepthFirst() {
        List<String> beforeList = new ArrayList<String>();
        List<String> afterList = new ArrayList<String>();
        new BreakAtASPDotNetTreeTraveler(beforeList, afterList).depthFirstTravel(createTree());
        Assert.assertEquals(30, beforeList.size());
        Assert.assertEquals(30, afterList.size());
        assertList(
                beforeList,
                "Technology { index = 0, path = Technology }",
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "Win32 { index = 0.0.0.0, path = Technology->CommonLanguages->C++->Win32 }",
                "Socket { index = 0.0.0.1, path = Technology->CommonLanguages->C++->Socket }",
                "MFC { index = 0.0.0.3, path = Technology->CommonLanguages->C++->MFC }",
                "ATL { index = 0.0.0.4, path = Technology->CommonLanguages->C++->ATL }",
                "DirectX { index = 0.0.0.6, path = Technology->CommonLanguages->C++->DirectX }",
                "Direct3D { index = 0.0.0.6.0, path = Technology->CommonLanguages->C++->DirectX->Direct3D }",
                "DirectDraw { index = 0.0.0.6.1, path = Technology->CommonLanguages->C++->DirectX->DirectDraw }",
                "DirectInput { index = 0.0.0.6.2, path = Technology->CommonLanguages->C++->DirectX->DirectInput }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                "CGLIB { index = 0.0.1.0, path = Technology->CommonLanguages->Java->CGLIB }",
                "ASM { index = 0.0.1.1, path = Technology->CommonLanguages->Java->ASM }",
                "ORM { index = 0.0.1.3, path = Technology->CommonLanguages->Java->ORM }",
                "Hibernate { index = 0.0.1.3.0, path = Technology->CommonLanguages->Java->ORM->Hibernate }",
                "myBatis { index = 0.0.1.3.2, path = Technology->CommonLanguages->Java->ORM->myBatis }",
                "JPA { index = 0.0.1.3.3, path = Technology->CommonLanguages->Java->ORM->JPA }",
                "RMI { index = 0.0.1.4, path = Technology->CommonLanguages->Java->RMI }",
                "EJB { index = 0.0.1.5, path = Technology->CommonLanguages->Java->EJB }",
                "EJB2.1 { index = 0.0.1.5.0, path = Technology->CommonLanguages->Java->EJB->EJB2.1 }",
                "EJB3 { index = 0.0.1.5.1, path = Technology->CommonLanguages->Java->EJB->EJB3 }",
                "WebService { index = 0.0.1.6, path = Technology->CommonLanguages->Java->WebService }",
                "Spring { index = 0.0.1.7, path = Technology->CommonLanguages->Java->Spring }",
                "Swing { index = 0.0.1.9, path = Technology->CommonLanguages->Java->Swing }",
                "SWT { index = 0.0.1.10, path = Technology->CommonLanguages->Java->SWT }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "WinForm { index = 0.0.2.0, path = Technology->CommonLanguages->.NET->WinForm }",
                "WPF { index = 0.0.2.1, path = Technology->CommonLanguages->.NET->WPF }",
                "ASP.NET { index = 0.0.2.2, path = Technology->CommonLanguages->.NET->ASP.NET }");
        assertList(
                afterList,
                "Win32 { index = 0.0.0.0, path = Technology->CommonLanguages->C++->Win32 }",
                "Socket { index = 0.0.0.1, path = Technology->CommonLanguages->C++->Socket }",
                "MFC { index = 0.0.0.3, path = Technology->CommonLanguages->C++->MFC }",
                "ATL { index = 0.0.0.4, path = Technology->CommonLanguages->C++->ATL }",
                "Direct3D { index = 0.0.0.6.0, path = Technology->CommonLanguages->C++->DirectX->Direct3D }",
                "DirectDraw { index = 0.0.0.6.1, path = Technology->CommonLanguages->C++->DirectX->DirectDraw }",
                "DirectInput { index = 0.0.0.6.2, path = Technology->CommonLanguages->C++->DirectX->DirectInput }",
                "DirectX { index = 0.0.0.6, path = Technology->CommonLanguages->C++->DirectX }",
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "CGLIB { index = 0.0.1.0, path = Technology->CommonLanguages->Java->CGLIB }",
                "ASM { index = 0.0.1.1, path = Technology->CommonLanguages->Java->ASM }",
                "Hibernate { index = 0.0.1.3.0, path = Technology->CommonLanguages->Java->ORM->Hibernate }",
                "myBatis { index = 0.0.1.3.2, path = Technology->CommonLanguages->Java->ORM->myBatis }",
                "JPA { index = 0.0.1.3.3, path = Technology->CommonLanguages->Java->ORM->JPA }",
                "ORM { index = 0.0.1.3, path = Technology->CommonLanguages->Java->ORM }",
                "RMI { index = 0.0.1.4, path = Technology->CommonLanguages->Java->RMI }",
                "EJB2.1 { index = 0.0.1.5.0, path = Technology->CommonLanguages->Java->EJB->EJB2.1 }",
                "EJB3 { index = 0.0.1.5.1, path = Technology->CommonLanguages->Java->EJB->EJB3 }",
                "EJB { index = 0.0.1.5, path = Technology->CommonLanguages->Java->EJB }",
                "WebService { index = 0.0.1.6, path = Technology->CommonLanguages->Java->WebService }",
                "Spring { index = 0.0.1.7, path = Technology->CommonLanguages->Java->Spring }",
                "Swing { index = 0.0.1.9, path = Technology->CommonLanguages->Java->Swing }",
                "SWT { index = 0.0.1.10, path = Technology->CommonLanguages->Java->SWT }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                "WinForm { index = 0.0.2.0, path = Technology->CommonLanguages->.NET->WinForm }",
                "WPF { index = 0.0.2.1, path = Technology->CommonLanguages->.NET->WPF }",
                "ASP.NET { index = 0.0.2.2, path = Technology->CommonLanguages->.NET->ASP.NET }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "Technology { index = 0, path = Technology }");
    }
    
    @Test
    public void testBreakAtASPDotNetTreeBreadthFirst() {
        List<String> beforeList = new ArrayList<String>();
        List<String> afterList = new ArrayList<String>();
        new BreakAtASPDotNetTreeTraveler(beforeList, afterList).breadthFirstTravel(createTree());
        Assert.assertEquals(36, beforeList.size());
        Assert.assertEquals(36, afterList.size());
        assertList(
                beforeList,
                "Technology { index = 0, path = Technology }",
                
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "Database { index = 0.1, path = Technology->Database }",
                "XML { index = 0.2, path = Technology->XML }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }",
                
                "Win32 { index = 0.0.0.0, path = Technology->CommonLanguages->C++->Win32 }",
                "Socket { index = 0.0.0.1, path = Technology->CommonLanguages->C++->Socket }",
                "MFC { index = 0.0.0.3, path = Technology->CommonLanguages->C++->MFC }",
                "ATL { index = 0.0.0.4, path = Technology->CommonLanguages->C++->ATL }",
                "DirectX { index = 0.0.0.6, path = Technology->CommonLanguages->C++->DirectX }",
                "CGLIB { index = 0.0.1.0, path = Technology->CommonLanguages->Java->CGLIB }",
                "ASM { index = 0.0.1.1, path = Technology->CommonLanguages->Java->ASM }",
                "ORM { index = 0.0.1.3, path = Technology->CommonLanguages->Java->ORM }",
                "RMI { index = 0.0.1.4, path = Technology->CommonLanguages->Java->RMI }",
                "EJB { index = 0.0.1.5, path = Technology->CommonLanguages->Java->EJB }",
                "WebService { index = 0.0.1.6, path = Technology->CommonLanguages->Java->WebService }",
                "Spring { index = 0.0.1.7, path = Technology->CommonLanguages->Java->Spring }",
                "Swing { index = 0.0.1.9, path = Technology->CommonLanguages->Java->Swing }",
                "SWT { index = 0.0.1.10, path = Technology->CommonLanguages->Java->SWT }",
                "WinForm { index = 0.0.2.0, path = Technology->CommonLanguages->.NET->WinForm }",
                "WPF { index = 0.0.2.1, path = Technology->CommonLanguages->.NET->WPF }",
                "ASP.NET { index = 0.0.2.2, path = Technology->CommonLanguages->.NET->ASP.NET }");
        
        assertList(
                afterList,
                
                "Win32 { index = 0.0.0.0, path = Technology->CommonLanguages->C++->Win32 }",
                "Socket { index = 0.0.0.1, path = Technology->CommonLanguages->C++->Socket }",
                "MFC { index = 0.0.0.3, path = Technology->CommonLanguages->C++->MFC }",
                "ATL { index = 0.0.0.4, path = Technology->CommonLanguages->C++->ATL }",
                "DirectX { index = 0.0.0.6, path = Technology->CommonLanguages->C++->DirectX }",
                "CGLIB { index = 0.0.1.0, path = Technology->CommonLanguages->Java->CGLIB }",
                "ASM { index = 0.0.1.1, path = Technology->CommonLanguages->Java->ASM }",
                "ORM { index = 0.0.1.3, path = Technology->CommonLanguages->Java->ORM }",
                "RMI { index = 0.0.1.4, path = Technology->CommonLanguages->Java->RMI }",
                "EJB { index = 0.0.1.5, path = Technology->CommonLanguages->Java->EJB }",
                "WebService { index = 0.0.1.6, path = Technology->CommonLanguages->Java->WebService }",
                "Spring { index = 0.0.1.7, path = Technology->CommonLanguages->Java->Spring }",
                "Swing { index = 0.0.1.9, path = Technology->CommonLanguages->Java->Swing }",
                "SWT { index = 0.0.1.10, path = Technology->CommonLanguages->Java->SWT }",
                "WinForm { index = 0.0.2.0, path = Technology->CommonLanguages->.NET->WinForm }",
                "WPF { index = 0.0.2.1, path = Technology->CommonLanguages->.NET->WPF }",
                "ASP.NET { index = 0.0.2.2, path = Technology->CommonLanguages->.NET->ASP.NET }",
                
                "C++ { index = 0.0.0, path = Technology->CommonLanguages->C++ }",
                "Java { index = 0.0.1, path = Technology->CommonLanguages->Java }",
                ".NET { index = 0.0.2, path = Technology->CommonLanguages->.NET }",
                "Oracle { index = 0.1.0, path = Technology->Database->Oracle }",
                "Microsoft SQL Server { index = 0.1.2, path = Technology->Database->Microsoft SQL Server }",
                "MySQL { index = 0.1.3, path = Technology->Database->MySQL }",
                "DTD { index = 0.2.0, path = Technology->XML->DTD }",
                "Schema { index = 0.2.1, path = Technology->XML->Schema }",
                "XPath { index = 0.2.3, path = Technology->XML->XPath }",
                "XSLT { index = 0.2.4, path = Technology->XML->XSLT }",
                "Javascript { index = 0.4.0, path = Technology->WebClient->Javascript }",
                "Prototype { index = 0.4.2, path = Technology->WebClient->Prototype }",
                "jQuery { index = 0.4.3, path = Technology->WebClient->jQuery }",
                "GWT { index = 0.4.5, path = Technology->WebClient->GWT }",
                
                "CommonLanguages { index = 0.0, path = Technology->CommonLanguages }",
                "Database { index = 0.1, path = Technology->Database }",
                "XML { index = 0.2, path = Technology->XML }",
                "WebClient { index = 0.4, path = Technology->WebClient }",
                
                "Technology { index = 0, path = Technology }");
    }
    
    private static Node createTree() {
        return new Node(
                "Technology",
                new Node("CommonLanguages",
                        new Node("C++",
                                new Node("Win32"),
                                new Node("Socket"),
                                null,
                                new Node("MFC"),
                                new Node("ATL"),
                                null,
                                new Node("DirectX",
                                        new Node("Direct3D"),
                                        new Node("DirectDraw"),
                                        new Node("DirectInput"))),
                        new Node("Java",
                                new Node("CGLIB"),
                                new Node("ASM"),
                                null,
                                new Node("ORM",
                                        new Node("Hibernate"),
                                        null,
                                        new Node("myBatis"),
                                        new Node("JPA")),
                                new Node("RMI"),
                                new Node("EJB",
                                        new Node("EJB2.1"),
                                        new Node("EJB3")),
                                new Node("WebService"),
                                new Node("Spring"),
                                null,
                                new Node("Swing"),
                                new Node("SWT")),
                        new Node(".NET",
                                new Node("WinForm"),
                                new Node("WPF"),
                                new Node("ASP.NET"),
                                null,
                                new Node("ADO.NET"),
                                new Node("COM+"),
                                new Node(".NET Remoting"),
                                new Node("WebService"),
                                new Node("Spring.NET"),
                                null,
                                new Node("ORM",
                                        new Node("NHibernate"),
                                        null,
                                        new Node("ADO.NET EntityFramework")))),
                new Node("Database",
                        new Node("Oracle",
                                new Node("Oracle11g",
                                        new Node("Oracle11g SQL"),
                                        null,
                                        new Node("Oracle11g RMAN"),
                                        new Node("Oracle11g DBA"))),
                        null,
                        new Node("Microsoft SQL Server"),
                        new Node("MySQL")),
                new Node("XML",
                        new Node("DTD"),
                        new Node("Schema"),
                        null,
                        new Node("XPath"),
                        new Node("XSLT")),
                null,
                new Node("WebClient",
                        new Node("Javascript"),
                        null,
                        new Node("Prototype"),
                        new Node("jQuery"),
                        null,
                        new Node("GWT")));
    }
    
    @SuppressWarnings("unchecked")
    private static <E> void assertList(List<E> list, E ... elements) {
        Assert.assertEquals(elements.length, list.size());
        int index = 0;
        for (E e : list) {
            E element = elements[index++];
            if (element == null) {
                Assert.assertNull(e);
            } else {
                Assert.assertEquals(element, e);
            }
        }
    }
    
    static class Node {
        
        String name;
        
        Node[] childNodes;
        
        public Node(String name, Node ... childNodes) {
            this.name = name;
            this.childNodes = childNodes;
        }

        @Override
        public String toString() {
            return this.name;
        }
        
    }
    
    static class TreeTraveler extends GraphTraveler<Node> {
        
        List<String> beforeList;
        
        List<String> afterList;

        TreeTraveler(List<String> beforeList, List<String> afterList) {
            this.beforeList = beforeList;
            this.afterList = afterList;
        }

        @Override
        protected Iterator<Node> getNeighborNodeIterator(Node node) {
            return MACollections.wrap(node.childNodes).iterator();
        }

        @Override
        protected void preTravelNeighborNodes(
                GraphTravelContext<Node> ctx,
                GraphTravelAction<Node> optionalGraphTravelAction) {
            super.preTravelNeighborNodes(ctx, optionalGraphTravelAction);
            validateCtx(ctx);
            this.beforeList.add(getCtxString(ctx));
        }

        @Override
        protected void postTravelNeighborNodes(
                GraphTravelContext<Node> ctx,
                GraphTravelAction<Node> optionalGraphTravelAction) {
            super.postTravelNeighborNodes(ctx, optionalGraphTravelAction);
            validateCtx(ctx);
            this.afterList.add(getCtxString(ctx));
        }
        
        private void validateCtx(GraphTravelContext<Node> ctx) {
            Assert.assertEquals(ctx.getDepth(), ctx.getBranchNodes().size() - 1);
            Assert.assertEquals(ctx.getDepth(), ctx.getBranchNodeIndexes().size() - 1);
            Assert.assertEquals(ctx.getNode(), ctx.getBranchNodes().get(ctx.getDepth()));
        }
        
        private static String getCtxString(GraphTravelContext<Node> ctx) {
            StringBuilder builder = new StringBuilder();
            builder
            .append(ctx.getNode() == null ? null : ctx.getNode().name)
            .append(" { index = ");
            Strings.join(ctx.getBranchNodeIndexes(), ".", builder);
            builder.append(", path = ");
            Strings.join(ctx.getBranchNodes(), "->", builder);
            builder.append(" }");
            return builder.toString();
        }
    }
    
    static class NullableTreeTraveler extends TreeTraveler {

        NullableTreeTraveler(List<String> beforeList, List<String> afterList) {
            super(beforeList, afterList);
        }

        @Override
        protected boolean travelNull() {
            return true;
        }
        
    }
    
    static class NoSocketSiblingNodesTreeTraveler extends TreeTraveler {

        NoSocketSiblingNodesTreeTraveler(List<String> beforeList, List<String> afterList) {
            super(beforeList, afterList);
        }

        @Override
        protected void preTravelNeighborNodes(
                GraphTravelContext<Node> ctx,
                GraphTravelAction<Node> optionalGraphTravelAction) {
            if (ctx.getNode().name.equals("Socket")) {
                ctx.stopTravelSiblingNodes();
            }
            super.preTravelNeighborNodes(ctx, optionalGraphTravelAction);
        }
        
    }
    
    static class NoDotNetNeighborNodesTreeTraveler extends TreeTraveler {

        NoDotNetNeighborNodesTreeTraveler(List<String> beforeList, List<String> afterList) {
            super(beforeList, afterList); 
        }

        @Override
        protected void preTravelNeighborNodes(
                GraphTravelContext<Node> ctx,
                GraphTravelAction<Node> optionalGraphTravelAction) {
            if (ctx.getNode().name.equals(".NET")) {
                ctx.stopTravelNeighborNodes();
            }
            super.preTravelNeighborNodes(ctx, optionalGraphTravelAction);
        }
        
    }
    
    static class ThreeLayerTreeTraveler extends TreeTraveler {
    
        ThreeLayerTreeTraveler(List<String> beforeList, List<String> afterList) {
            super(beforeList, afterList);
        }
    
        @Override
        protected void preTravelNeighborNodes(
                GraphTravelContext<Node> ctx,
                GraphTravelAction<Node> optionalGraphTravelAction) {
            if (ctx.getDepth() == 2) {
                ctx.stopTravelNeighborNodes();
            }
            super.preTravelNeighborNodes(ctx, optionalGraphTravelAction);
        }
        
    }

    static class BreakAtASPDotNetTreeTraveler extends TreeTraveler {

        BreakAtASPDotNetTreeTraveler(List<String> beforeList, List<String> afterList) {
            super(beforeList, afterList);
        }

        @Override
        protected void preTravelNeighborNodes(
                GraphTravelContext<Node> ctx,
                GraphTravelAction<Node> optionalGraphTravelAction) {
            if (ctx.getNode().name == "ASP.NET") {
                ctx.stopTravel();
            }
            super.preTravelNeighborNodes(ctx, optionalGraphTravelAction);
        }
        
    }
    
}
