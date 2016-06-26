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

#ifndef __DELEGATE_H__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__
#define __DELEGATE_H__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__

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

#include <stdexcept>
 
/*
 * Declare the delegate, which can contain several elements,
 * each element can be:
 * (1) Global/Static Function pointer
 * (2) A C++ object of ANY class and 
 *     one member function pointer of this object.
 *
 * My time is not enough, may be C++11 lambda expression 
 * will be supported in next version.
 */
namespace babyfish
{
    class delegate_base
    {
    public:
        ~delegate_base() { this->set_nodes(0); }

        int available() { return this->nodes != 0; }

        delegate_base(const delegate_base &to_clone) 
        { throw std::logic_error("\"babyfish::delegate_base\" does not support the copy constructor"); }

        delegate_base &operator = (const delegate_base &to_clone)
        { throw std::logic_error("\"babyfish::delegate_base\" does not support the operator = "); }

        bool is_nil() 
        { return this->nodes == 0; }

    protected:
        struct method_ptr
        {
            /*
             * In visual C++, sizeof(member function pointer) == sizeof(other pointer);
             * but in G++, sizeof(member function pointer) = 2 * sizeof(other pointer).
             */
            void *e[sizeof(void (delegate_base::*)()) / sizeof(void*)];
        };

    protected:
        delegate_base() : nodes(0) {}

        void add(void *target, method_ptr method)
        {
            if (!method.e[0])
            {
                throw std::invalid_argument("\"method\" can not be null");
            }
            if (this->nodes)
            {
                data_node node = { target, method };
                this->set_nodes(this->nodes->add(&node, 1));
            }
            else
            {
                this->set_nodes(data_nodes::create(target, method));
            }
        }

        void add(const delegate_base &to_add)
        {
            if (this->nodes)
            {
                this->set_nodes(this->nodes->add(to_add.nodes->node_ptr, to_add.nodes->length));
            }
            else
            {
                this->set_nodes(to_add.nodes);
            }
        }

        void remove(void *target, void *method)
        {
            if (this->nodes)
            {
                data_node node = { target, method };
                this->set_nodes(this->nodes->remove(&node, 1));
            }
        }

        void remove(const delegate_base &to_remove)
        {
            if (this->nodes)
            {
                this->set_nodes(
                    this->nodes->remove(
                        to_remove.nodes->node_ptr, 
                        to_remove.nodes->length
                    )
                );
            }
        }

        void assign(const delegate_base &to_clone)
        {
            this->set_nodes(to_clone.nodes);
        }

    private:
        struct data_nodes;

    protected:
        class snapshot
        {
        public:
            snapshot(const delegate_base &owner)
            { 
                this->nodes = owner.nodes; 
                if (owner.nodes)
                    owner.nodes->add_ref();
            }

            ~snapshot()
            {
                data_nodes *nodes = this->nodes;
                if (nodes)
                {
                    this->nodes = 0;
                    nodes->release();
                }
            }

            int node_at(int index, void *&target, method_ptr &method)
            {
                if (!this->nodes)
                {
                    return 0;
                }
                if (index < 0 || index >= this->nodes->length)
                {
                    return invalid_node;
                }
                target = this->nodes->node_ptr[index].target;
                method = this->nodes->node_ptr[index].method;
                int ret = valid_node;
                if (index == 0)
                {
                    ret |= first_node;
                }
                if (index + 1 == this->nodes->length)
                {
                    ret |= last_node;
                }
                return ret;
            }

        private:
            data_nodes *nodes;
        };

        enum
        {
            invalid_node = 0,
            valid_node = 1 << 0,
            first_node = 1 << 1,
            last_node = 1 << 2
        };

    private:
        struct data_node
        {
            void *target;
            method_ptr method;
        };

        struct data_nodes
        {
            int ref_count;
            int length;
            data_node node_ptr[1];

            static data_nodes *create(void *target, method_ptr method)
            {
                data_nodes *nodes = create(1);
                nodes->node_ptr[0].target = target;
                nodes->node_ptr[0].method = method;
                return nodes;
            }

            static data_nodes *create(int length)
            {
                char *mem = new char[sizeof(data_nodes) + (length - 1) * sizeof(data_node)];
                data_nodes *nodes = reinterpret_cast<data_nodes*>(mem);
                nodes->ref_count = 0;
                nodes->length = length;
                return nodes;
            }

            void dispose()
            {
                char *mem = reinterpret_cast<char*>(this);
                delete [] mem;
            }

            void add_ref() { this->ref_count++; }

            void release()
            {
                if (0 == --this->ref_count)
                {
                    this->dispose();
                }
            }
            
            data_nodes *add(data_node *to_add_node_ptr, int to_add_length) const
            {
                if (to_add_length)
                {
                    int i;
                    int this_length = this->length;
                    int new_length = this_length + to_add_length;
                    data_nodes *new_nodes = create(new_length);
                    data_node *new_node_ptr = new_nodes->node_ptr;
                    const data_node *this_node_ptr = this->node_ptr;
                    for (i = this_length - 1; i >= 0; i--)
                    {
                        new_node_ptr[i] = this_node_ptr[i];
                    }
                    for (i = to_add_length - 1; i >= 0; i--)
                    {
                        new_node_ptr[this_length + i] = to_add_node_ptr[i];
                    }
                    return new_nodes;
                }
                return const_cast<data_nodes*>(this);
            }

            data_nodes *remove(data_node *to_remove_node_ptr, int to_remove_length) const
            {
                if (this->node_ptr == to_remove_node_ptr && this->length == to_remove_length)
                {
                    return 0;
                }
                if (!to_remove_node_ptr || !to_remove_length)
                {
                    return const_cast<data_nodes*>(this);
                }
                
                int this_length = this->length;
                int new_length = 0;
                const data_node *this_node_ptr = this->node_ptr;
                data_nodes *new_nodes = create(this_length);
                data_node *new_node_ptr = new_nodes->node_ptr;
                
                char *to_remove_applied_flags = new char[this_length];
                int i;
                for (i = this_length - 1; i >= 0; i--)
                {
                    to_remove_applied_flags[i] = 0;
                }
                for (i = 0; i < this_length; i++)
                {
                    int index_of_to_remove = node_index_of(to_remove_node_ptr, to_remove_length, this_node_ptr[i]);
                    if (index_of_to_remove != -1 && !to_remove_applied_flags[index_of_to_remove])
                    {
                        to_remove_applied_flags[index_of_to_remove] = 1;
                    }
                    else
                    {
                        new_node_ptr[new_length].target = this_node_ptr[i].target;
                        new_node_ptr[new_length].method = this_node_ptr[i].method;
                        new_length++;
                    }
                }
                delete [] to_remove_applied_flags;

                if (new_length == this_length)
                {
                    new_nodes->dispose();
                    return const_cast<data_nodes*>(this);
                }
                if (new_length == 0)
                {
                    new_nodes->dispose();
                    return 0;
                }
                new_nodes->length = new_length;
                return new_nodes;
            }

        } *nodes;

    private:
        void set_nodes(data_nodes *new_nodes)
        {
            data_nodes *old_nodes = this->nodes;
            if (old_nodes != new_nodes)
            {
                if (old_nodes)
                {
                    old_nodes->release();
                }
                if (new_nodes)
                {
                    new_nodes->add_ref();
                }
                this->nodes = new_nodes;
            }
        }

        static int node_index_of(const data_node *node_arr, int length, const data_node &single_node)
        {
            for (int i = 0; i < length; i++)
            {
                if (node_arr[i].method.e[0] == single_node.method.e[0] && 
                    node_arr[i].target == single_node.target)
                {
                    return i;
                }
            }
            return -1;
        }
    };
}

#define delegate_void(delegate_name, parameters, arguments) \
    class delegate_name : public babyfish::delegate_base \
    { \
        members_for_delegate_void(delegate_name, delegate_name, parameters, arguments) \
    };

#define delegate_not_void(delegate_name, return_type, default_return_value, parameters, arguments) \
    class delegate_name : public babyfish::delegate_base \
    { \
        members_for_delegate_not_void(delegate_name, delegate_name, return_type, default_return_value, parameters, arguments) \
    };

#define members_for_delegate_void(delegate_class_name, delegate_type_name, parameters, arguments) \
    public: \
        ____members_except_invoke_for_delegate(delegate_class_name, delegate_type_name, void, parameters, arguments) \
         \
        void operator() parameters const \
        { \
            int index__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__ = 0; \
            union \
            { \
                void *target__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__; \
                babyfish::delegate_base *instance__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__; \
            }; \
            union \
            { \
                method_ptr method__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__; \
                void (*static_method__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__)parameters; \
                void (babyfish::delegate_base::*instance_method__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__)parameters; \
            }; \
            snapshot snapshot__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__(*this); \
            while (snapshot__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__.node_at( \
                    index__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__++, \
                    target__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__ , \
                    method__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__)) \
            { \
                if (target__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__) \
                { \
                    (instance__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__ \
                    ->* \
                    instance_method__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__) \
                    arguments; \
                } \
                else \
                { \
                    static_method__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__ arguments; \
                } \
            } \
        }

#define members_for_delegate_not_void(delegate_class_name, delegate_type_name, return_type, default_return_value, parameters, arguments) \
    public: \
        ____members_except_invoke_for_delegate(delegate_class_name, delegate_type_name, return_type, parameters, arguments) \
         \
        return_type operator() parameters const \
        { \
            int index__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__ = 0; \
            union \
            { \
                void *target__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__; \
                babyfish::delegate_base *instance__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__; \
            }; \
            union \
            { \
                method_ptr method__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__; \
                return_type (*static_method__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__)parameters; \
                return_type (babyfish::delegate_base::*instance_method__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__)parameters; \
            }; \
            int node_status__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__; \
            snapshot snapshot__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__(*this); \
            while (node_status__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__ = \
                snapshot__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__.node_at( \
                    index__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__++, \
                    target__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__ , \
                    method__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__)) \
            { \
                if (target__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__) \
                { \
                    if (node_status__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__ &last_node) \
                    return (instance__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__ \
                        ->* \
                        instance_method__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__) \
                        arguments; \
                    (instance__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__ \
                    ->* \
                    instance_method__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__) \
                    arguments; \
                } \
                else \
                { \
                    if (node_status__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__ &last_node) \
                        return static_method__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__ arguments; \
                    static_method__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__ arguments; \
                } \
            } \
            return default_return_value; \
        }

#define ____members_except_invoke_for_delegate(delegate_class_name, delegate_type_name, return_type, parameters, arguments) \
        delegate_class_name() {} \
         \
        template<typename T__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__> \
        delegate_class_name(const T__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__ *target, \
            return_type (T__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__::*method)parameters) \
        { \
            union \
            { \
                method_ptr method_void_ptr; \
                return_type (T__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__::*method_func_ptr)parameters; \
            }; \
            if (!target) \
            { \
                throw std::invalid_argument("\"target\" can not be null"); \
            } \
            method_func_ptr = method; \
            this->add(const_cast<T__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__*>(target), method_void_ptr); \
        } \
         \
        delegate_class_name(return_type (*method)parameters) \
        { \
            union \
            { \
                method_ptr method_void_ptr; \
                return_type (*method_func_ptr)parameters; \
            }; \
            method_func_ptr = method; \
            this->add(0, method_void_ptr); \
        } \
         \
        delegate_class_name(const delegate_type_name &to_clone) \
        { this->assign(to_clone); } \
         \
        delegate_type_name &operator = (const delegate_type_name &to_assign) \
        { this->assign(to_assign); return *this;} \
         \
        delegate_type_name &operator += (const delegate_type_name &to_add) \
        { this->add(to_add); return *this;} \
         \
        delegate_type_name &operator -= (const delegate_type_name &to_remove) \
        { this->remove(to_remove); return *this;} \
         \
        delegate_type_name operator + (const delegate_type_name &right) const \
        { \
            delegate_type_name ret = *this; \
            ret += right; \
            return ret; \
        } \
         \
        delegate_type_name operator - (const delegate_type_name &right) const \
        { \
            delegate_type_name ret = *this; \
            ret -= right; \
            return ret; \
        }

#endif //__DELEGATE_H__00F51F00_9EBA_4bd7_AC78_1819E0CE4D34__

