/*
 * Be different with Maven of Java, C++ does not have a central
 * dependencies managment solutation.
 *
 * In order to make the C++ demo to be simple, we print the output
 * to the console directly and don't use any test framework such as 
 * GTEST. So this demo depends on nothing except the header files 
 * of babyfish
 */
#include <iostream>
#include <string>
#include <sstream>
#include "../src/event.h"
using namespace std;
using namespace babyfish;

class PropertyChangedEventArgs : public event_args
{
public:
    PropertyChangedEventArgs(
        const wchar_t *propertyName,
        const wstring &oldValue,
        const wstring &newValue)
        : propertyName(propertyName), 
        oldValue(oldValue), 
        newValue(newValue)
    {
    }

    const wchar_t *getPropertyName() const 
    { return this->propertyName.c_str(); }

    const wchar_t *getOldValue() const 
    { return this->oldValue.c_str(); }

    const wchar_t *getNewValue() const
    { return this->newValue.c_str(); }

private:
    wstring propertyName;
    wstring oldValue;
    wstring newValue;
};

/*
 * In demo, we use the delegate "event_handler<PropertyChangedEventArgs>" 
 * declared by babyfish. 
 * (
 *      If you don't like event_handler<PropertyChangedEventArgs>,
 *      you can also choose to declare the delegate by yourself, such as
 *      delegate_void(
 *          PropertyChangedHandler, 
 *          (void *sender, PropertyChangedEventArgs &e), 
 *          (sender, e)
 *      )
 * )
 * You can create an instance of delegate via
 * (1) Global/Static Function pointer
 * (2) A C++ object of ANY class and 
 *     one member function pointer of this object.
 * 
 * My time is not enough, may be in next version, 
 * you can use lambda expression of  C++11
 */

class Book
{
public:
    Book(const wchar_t *name, double price)
        : name(name), price(price)
    {
    }

    const wchar_t *getName() const
    { return this->name.c_str(); }

    void setName(const wchar_t *name)
    {
        if (this->name != name)
        {
            wstring oldName = this->name;
            this->name = name;
            if (!this->proprtyChangedHandler.is_nil())
            {
                PropertyChangedEventArgs e(L"name", oldName, this->name);
                this->proprtyChangedHandler(this, e);
            }
        }
    }

    double getPrice() const
    { return this->price; }

    void setPrice(double price)
    {
        if (this->price != price)
        {
            double oldPrice = this->price;
            this->price = price;
            if (!this->proprtyChangedHandler.is_nil())
            {
                PropertyChangedEventArgs e(
                    L"price", 
                    val(oldPrice),
                    val(price)
                );
                this->proprtyChangedHandler(this, e);
            }
        }
    }

    /*
     * Be different with the private member "proprtyChangedHandler",
     * this public member "propertyChange()" is event, not delegate.
     * You can do nothing except "+=" and "-=".
     *
     * You can consider this event to be the C# event
     *      public event System.EventHandler<PropertyChangedEventArgs> PropertyChanged
     *      {
     *          add { this.proprtyChangedHandler += value; } // "add" means "+="
     *          remove { this.proprtyChangedHandler -= value; } // "remove" means "-="
     *      }
     */
    declare_event(
        event_handler<PropertyChangedEventArgs>, 
        propertyChanged, 
        proprtyChangedHandler
    )

private:
    wstring name;
    double price;
    /*
     * This private member, it is delegate, not event.
     * You can do every thing: such as "=", "+=", "-=", "is_nil()" 
     * or call it via "operator()(...args...)".
     */
    event_handler<PropertyChangedEventArgs> proprtyChangedHandler;

private:
    static wstring val(double value)
    {
        wostringstream builder;
        builder << value;
        return builder.str();
    }
};

/*
 * In order to make this demo to be more OO,
 * we don't use global/static functions, but
 * member methods of annotation class to handle 
 * the event of Book
 */
class BookEventHandlerImpl
{
public:
    BookEventHandlerImpl() {}

    void once(void *sender, PropertyChangedEventArgs &e)
    {
        /*
         * Important functionality of babyfish event!
         * You can change the event during the event handling.
         * This event has removed itself, so it will be
         * triggered ONLY ONCE.
         */
        ((Book*)sender)->propertyChanged() 
            -= 
            event_handler<PropertyChangedEventArgs>(
                this, &BookEventHandlerImpl::once
            );

        wcout << L"[ONLY-ONCE-HANDLER]: ";
        this->print(e);
    }

    void forever(void *sender, PropertyChangedEventArgs &e)
    {
        wcout << L"[FOREVER-HANDLER]: ";
        this->print(e);
    }

private:
    void print(const PropertyChangedEventArgs &e)
    {
        wcout
            << L"{\n\tpropertyName: "
            << e.getPropertyName()
            << L",\n\toldValue: "
            << e.getOldValue()
            << L",\n\tnewValue: "
            << e.getNewValue()
            << L"\n}\n";
    }
};

/*
 * The output is: 
 *
 * The current app is 64 bits
 * __________________________
 * [ONLY-ONCE-HANDLER]: {
 *         propertyName: name,
 *         oldValue: NodeJS,
 *         newValue: AngularJS
 * }
 * [FOREVER-HANDLER]: {
 *         propertyName: name,
 *         oldValue: NodeJS,
 *         newValue: AngularJS
 * }
 * [FOREVER-HANDLER]: {
 *         propertyName: price,
 *         oldValue: 40,
 *         newValue: 43
 * }
 */
int main(int argc, char *argv[])
{
    wcout 
        << L"The current app is " 
        << (sizeof(void*) == 8 ? 64 : 32)
        << L" bits" 
        << endl
        << L"__________________________"
        << endl;

    Book book(L"NodeJS", 40);
    BookEventHandlerImpl bookEventHandlerImpl;
    book.propertyChanged() += event_handler<PropertyChangedEventArgs>(
        &bookEventHandlerImpl, &BookEventHandlerImpl::once
    );
    book.propertyChanged() += event_handler<PropertyChangedEventArgs>(
        &bookEventHandlerImpl, &BookEventHandlerImpl::forever
    );

    book.setName(L"AngularJS");
    book.setPrice(43);

    return 0;
}

