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
    int n;

    Bag(int[] input) {
        n = input.length;
        contents = new int[n];
        arraycopy(input, 0, contents, 0, n);
    }

    Bag() {
        n =0;
        contents = new int[0];
    }

    void removeOnce(int elt) {
        for (int i = 0; i <= n; i++) {  
            if (contents[i] == elt ) {
                n--;
                contents[i] = contents[n];
                return;
          }
        }
    }

    void removeAll(int elt) {
        for (int i = 0; i <= n; i++) {   
            if (contents[i] == elt ) {
                n--;
                contents[i] = contents[n];
            }
        }
    }

    int getCount(int elt) {
        int count = 0;
        for (int i = 0; i <= n; i++) 
            if (contents[i] == elt) count++; 
        return count;
    }

    void add(int elt) {
        if (n == contents.length) {
            int[] new_contents = new int[2*n]; 
            arraycopy(contents, 0, new_contents, 0, n);
            contents = new_contents;
        }
        contents[n]=elt;
        n++;
    }

    void add(Bag b) {
        int[] new_contents = new int[n + b.n];
        arraycopy(contents, 0, new_contents, 0, n);
        arraycopy(b.contents, 0, new_contents, n+1, b.n);
        contents = new_contents;
    }

    void add(int[] a) {
        this.add(new Bag(a));
    }

    Bag(Bag b) {
        this.add(b);    
    }

    private static void arraycopy(int[] src,
                                int   srcOff,
                                int[] dest,
                                int   destOff,
                                int   length) {
        for( int i=0 ; i<length; i++) {
            dest[destOff+i] = src[srcOff+i];
        }
    }
  
}
