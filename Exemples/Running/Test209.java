// Tableaux d'entiers
class Test219 {
  public static void main(String[] a) {
    {
      System.out.println(new Test1().start()); // 42
      System.out.println(new Test2().test2(1, false)); // 42
      System.out.println(new Test2().test2(0, true) - 6); // 42
    }
  }
}

class Test1 {
  Test1 test;
  int[] tab;

  public int start() {
    tab = new int[10];
    tab[tab.length - 1] = 6;
    test = this.next(tab);
    return tab[0];
  }

  public Test1 next(int[] t) {
    t[0] = tab[tab.length - 1] * 7;
    return test;
  }
}

class Test2 extends Test4 {
  int d;
  int[] e;

  public int test2(int y, boolean z) {
    int f;
    e = new int[1 + 2];
    a = 2;
    d = 4;
    c = z;
    if (!c)
      e[0] = a;
    else
      e[0] = d;
    e[1] = d;
    e[1 + 1] = e[0] + e[1 * 1];
    return 2 * e.length * (y + e[2]);
  }
}

class Test4 {
  int a;
  boolean c;
}
