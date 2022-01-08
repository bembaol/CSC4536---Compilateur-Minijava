class Factorial {
  public static void main(String[] a) {
    System.out.println(new Fac().f(4, 1, 2, 3, 4));
  }
}

class Fac {

  public int ComputeFac(int num) {
    int num_aux;
    if (num < 1)
      num_aux = 1;
    else
      num_aux = num * (this.ComputeFac(num - 1));
    return num_aux;
  }
  
  public int f (int a, int b, int c, int d, int e) { // f (4, 1, 2, 3, 4)=10
	  int res;
	  if (a<1) res = 0;
	  else res = e + this.f(a-1, e, b, c, d);
	  return res;
	}

}
