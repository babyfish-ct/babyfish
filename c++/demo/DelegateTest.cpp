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
#include "../src/delegate.h"
using namespace std;

/*
 * Declare a delegate "ArithmeticHandler", 
 * you can create an instance of this delegate via
 * (1) Global/Static Function pointer
 * (2) A C++ object of ANY class and 
 *     one member function pointer of this object.
 * 
 * My time is not enough, maybe in next version, 
 * you can use lambda expression of  C++11
 */
delegate_not_void(ArithmeticHandler, double, 0, (double x, double y), (x, y))

class Arithmetic
{
public:
    Arithmetic(wostringstream &builder) : builder(builder) 
    {
    }

    double sum(double x, double y)
    {
        this->builder << x << L" + " << y << L" = " << x + y << endl;
        return x + y;
    }

    double diff(double x, double y)
    {
        this->builder << x << L" - " << y << L" = " << x - y << endl;
        return x - y;
    }

    double prod(double x, double y)
    {
        this->builder << x << L" * " << y << L" = " << x * y << endl;
        return x * y;
    }

    double quot(double x, double y)
    {
        this->builder << x << L" / " << y << L" = " << x / y << endl;
        return x / y;
    }
private:
    wostringstream &builder;
};

/*
 * The output is: 
 *
 * The current app is 64 bits
 * __________________________
 * ArithmeticHandler handler = sum + diff + prod + quot
 * The result of handler(60, 12) is 5
 * The event history is:
 * 60 + 12 = 72
 * 60 - 12 = 48
 * 60 * 12 = 720
 * 60 / 12 = 5
 * 
 * handler -= prod + quot
 * The result of handler(60, 12) is 48
 * The event history is:
 * 60 + 12 = 72
 * 60 - 12 = 48
 * 
 * handler -= sum + diff
 * handler is nil
 * The result of handler(60, 12) is 0
 * The event history is:
 * 
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

    wostringstream builder;
    Arithmetic arithmetic(builder);
    
    /*
     * In this demo, we create the instances of delegate
     * via C++ object and its member functions.
     *
     * Actually, global/static/"member static" functions
     * can also be used to create the instances of delegate.
     *
     * It don't use them to be demo, 
     * so I ONLY give the examples in this comment, like this:
     *
     * ArithmeticHandler handler = 
     *      ArithmeticHandler(globalFunction) +
     *      ArithmeticHandler(staticFunction) +
     *      ArithmeticHandler(SomeClass::staticMemberFunction);
     */
    ArithmeticHandler handler =
        ArithmeticHandler(&arithmetic, &Arithmetic::sum) +
        ArithmeticHandler(&arithmetic, &Arithmetic::diff) +
        ArithmeticHandler(&arithmetic, &Arithmetic::prod) +
        ArithmeticHandler(&arithmetic, &Arithmetic::quot);
    wcout << L"ArithmeticHandler handler = sum + diff + prod + quot" << endl;
    wcout << L"The result of handler(60, 12) is " << handler(60, 12) << endl;
    wcout << L"The event history is: " << endl;
    wcout << builder.str() << endl;

    builder.str(L"");

    handler -= 
        ArithmeticHandler(&arithmetic, &Arithmetic::prod) +
        ArithmeticHandler(&arithmetic, &Arithmetic::quot);
    wcout << L"handler -= prod + quot" << endl;
    wcout << L"The result of handler(60, 12) is " << handler(60, 12) << endl;
    wcout << L"The event history is: " << endl;
    wcout << builder.str() << endl;

    builder.str(L"");

    handler -= 
        ArithmeticHandler(&arithmetic, &Arithmetic::sum) +
        ArithmeticHandler(&arithmetic, &Arithmetic::diff);
    wcout << L"handler -= sum + diff" << endl;
    if (handler.is_nil())
    {
        wcout << L"handler is nil" << endl;
    }
    wcout << L"The result of handler(60, 12) is " << handler(60, 12) << endl;
    wcout << L"The event history is: " << endl;
    wcout << builder.str() << endl;

    return 0;
}

