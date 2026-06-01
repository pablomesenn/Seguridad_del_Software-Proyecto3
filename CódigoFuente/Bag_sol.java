/*
 * This assignment is based on Erik Poll's assignment (Radboud University, Nijmegen).
 */

/* OpenJML Exercise: 
   
   This class implements a Bag of integers, using an array.

   Add JML specs to this class, to stop OpenJML from complaining.

   NB there may be errors in the code that you have to fix to stop 
   OpenJML from complaining, as these complaints of OpenJML 
   point to real bugs in the code. But keep changes to the code to
   a minimum of what is strictly needed. 
   Mark any changes you have made in the code with a comment,
   eg to indicate that you replaced <= by <.

   While developing your specs, it may be useful to use the keywords
      assert
   to add additional assertions in source code, to find out what 
   OpenJML can - or cannot - prove at a given program point.
  
*/

//@ nullable_by_default                      // Do not remove this annotation
class Bag {

    int[] contents;
    int   n;

    //@ invariant contents != null;
    //@ invariant 0 <= n && n <= contents.length;
    //@ invariant n <= Integer.MAX_VALUE / 2;

    //@ requires input != null;
    //@ requires input.length <= Integer.MAX_VALUE / 2;
    //@ assignable \nothing;
    //@ ensures n == input.length;
    Bag(int[] input) {
        n = input.length;
        contents = new int[n];
        arraycopy(input, 0, contents, 0, n);
    }

    //@ assignable \nothing;
    //@ ensures n == 0;
    Bag() {
        n = 0;
        contents = new int[0];
    }

    //@ assignable n, contents[*];
    //@ ensures n == \old(n) || n == \old(n) - 1;
    void removeOnce(int elt) {
        /*@ loop_invariant 0 <= i && i <= n;
          @ loop_invariant 0 <= n && n <= contents.length;
          @ loop_invariant n == \old(n);
          @ loop_invariant n <= Integer.MAX_VALUE / 2;
          @ decreases n - i;
          @*/
        for (int i = 0; i < n; i++) {        // antes i <= n  (i == n lee contents[n], fuera de limites)
            if (contents[i] == elt) {
                n--;
                contents[i] = contents[n];
                return;
            }
        }
    }

    //@ assignable n, contents[*];
    //@ ensures n <= \old(n);
    void removeAll(int elt) {
        /*@ loop_invariant 0 <= i && i <= n + 1;   // i puede sobrepasar n en 1 cuando se elimina el ultimo elemento
          @ loop_invariant 0 <= n && n <= contents.length;
          @ loop_invariant n <= \old(n);
          @ loop_invariant n <= Integer.MAX_VALUE / 2;
          @ decreases n - i;
          @*/
        for (int i = 0; i < n; i++) {        // era i <= n (i == n lee contents[n], fuera de limites)
            if (contents[i] == elt) {
                n--;
                contents[i] = contents[n];
            }
        }
    }

    // NB: a functional postcondition such as
    //        ensures \result == (\num_of int i; 0 <= i && i < n; contents[i] == elt);
    //     is NOT used here: OpenJML's SMT backend does not support \num_of for
    //     static checking. getCount is left with a purity/safety spec only.
    //@ ensures \result >= 0;
    //@ pure
    int getCount(int elt) {
        int count = 0;
        /*@ loop_invariant 0 <= i && i <= n;
          @ loop_invariant 0 <= count && count <= i;
          @ decreases n - i;
          @*/
        for (int i = 0; i < n; i++)          // era i <= n (i == n lee contents[n], fuera de limites)
            if (contents[i] == elt) count++;
        return count;
    }

    //@ requires n < Integer.MAX_VALUE / 2;
    //@ assignable contents, contents[*], n;
    //@ ensures n == \old(n) + 1;
    void add(int elt) {
        if (n == contents.length) {
            int[] new_contents = new int[2*n + 1];   // era 2*n; para la bolsa vacia (n==0) esta longitud se mantuvo en 0, por lo que contents[n] luego estaba fuera de limites
            arraycopy(contents, 0, new_contents, 0, n);
            contents = new_contents;
        }
        contents[n] = elt;
        n++;
    }

    //@ requires b != null;
    //@ requires b.n <= Integer.MAX_VALUE / 2 - n;
    //@ assignable contents, n;
    //@ ensures n == \old(n) + \old(b.n);
    void add(Bag b) {
        int[] new_contents = new int[n + b.n];
        arraycopy(contents, 0, new_contents, 0, n);
        arraycopy(b.contents, 0, new_contents, n, b.n);   // el offset era n+1, lo que dejo un hueco en el indice n y escribio un elemento mas alla del final de new_contents
        contents = new_contents;
        n = n + b.n;                                      // n nunca se actualizo, por lo que los elementos agregados, eran invisibles para getCount
    }

    //@ requires a != null;
    //@ requires a.length <= Integer.MAX_VALUE / 2 - n;
    //@ assignable contents, n;
    //@ ensures n == \old(n) + a.length;
    void add(int[] a) {
        this.add(new Bag(a));
    }

    //@ requires b != null;
    //@ assignable \nothing;
    //@ ensures n == b.n;
    Bag(Bag b) {
        n = 0;                    // los campos no estaban inicializados antes de que se llamara a this.add(b);
        contents = new int[0];    // contents esta por defecto en nulo y luego se pasa a arraycopy
        this.add(b);
    }

    //@ requires src != null && dest != null;
    //@ requires 0 <= srcOff && 0 <= destOff && 0 <= length;
    //@ requires srcOff + length <= src.length;
    //@ requires destOff + length <= dest.length;
    //@ assignable dest[*];
    private static void arraycopy(int[] src,
                                  int   srcOff,
                                  int[] dest,
                                  int   destOff,
                                  int   length) {
        /*@ loop_invariant 0 <= i && i <= length;
          @ decreases length - i;
          @*/
        for (int i = 0; i < length; i++) {
            dest[destOff + i] = src[srcOff + i];
        }
    }
}