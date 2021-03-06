// Test sémantique : Héritage de classe
class Test403 {
  public static void main(String[] a) {
    System.out.println(new A().start());
  }
}

class G extends H { } // FAIL Class Loop
class H extends G { } // Fail Class Loop
class F extends F { } // Fail Class Loop
class E extends Unk { } // Fail Class Undef
class D extends Object { int x; int y; }
class C extends B { int x; } //May be warning field override
class B extends A {}
class B {}       // Fail Duplicated class
class Test403 {} // Fail Duplicated class (may be not if mainKlass not in Symbol table!)
  
class A {
  A a;
  B b;
  C c;

  public int start() {
    a = b;
    a = c;
    b = c;
    c = a; // FAIL Type
    c = b; // FAIL Type
    a = new C(); // valid upcast , checktype OK, but executioon ???
    c = new A(); // FAIL Type
    a = new D(); // FAIL Type
    a = this;
    b = this; // FAIL Type
    c = this; // FAIL Type
    return 42;
  }
  
  public int start(){return 0;} // Fail Duplicated method
}


