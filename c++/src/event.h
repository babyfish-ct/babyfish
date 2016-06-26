/*
 * A small tool for C++ published with
 * BabyFish(Object Model Framework for Java and JPA)
 * together.
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
 
#ifndef __EVENT_H__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__
#define __EVENT_H__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__

/*
 * This tiny library supports the functionalities that are very similar
 * with the "delegate" and event" of .NET framework.
 * 
 * Its first version is designed too early(2004, when I was a student) so that it 
 * doesn't support the lambda of C+11, comparing with the first version, 
 * this version does not have big enhancement because time is not enough.
 * Maybe lambda will be supported in the future.
 *
 * @author Tao Chen(&#38472;&#28059;)
 */
 
#include "delegate.h"

namespace babyfish
{
    // This struct should NOT contain virtual methods
    // so that the address of the object equals the address
    // of the member. That is very important!!!
    template <typename T>
    struct event_wrapper
    {
    public:
        event_wrapper(T &delegate_ref) : delegate_ref(delegate_ref)
        {
        }

        void operator += (const T &delegate_to_add)
        { this->delegate_ref += (delegate_to_add); }

        void operator -= (const T &delegate_to_add)
        { this->delegate_ref -= (delegate_to_add); }
    private:
        T &delegate_ref;
    };

    class event_args
    {
    public:
        static event_args &empty()
        {
            static event_args empty;
            return empty;
        }
    };

    /*
     *  template <typename E = babyfish::event_args>
     *  delegate void event_handler(void *sender, E &e);
     */
    template <typename E = babyfish::event_args>
    class event_handler : public delegate_base
    {
    private:
        typedef event_handler<E> self_type;
        members_for_delegate_void(event_handler, self_type, (void *sender, E &e), (sender, e))
    };
}

#define declare_event(delegate_type, event_field, delegate_field) \
    babyfish::event_wrapper< delegate_type > event_field() \
    { return babyfish::event_wrapper< delegate_type >(delegate_field); }

#endif //__EVENT_H__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__

