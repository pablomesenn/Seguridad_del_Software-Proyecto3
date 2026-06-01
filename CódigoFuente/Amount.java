/*
 * This assignment is based on Erik Poll's assignment (Radboud University, Nijmegen).
 */

/* OpenJML exercise

   Objects of this class represent euro amounts. For example, an Amount 
   object with
     euros == 1
     cents == 55
   represents 1.55 euro. 

   Specify the class with JML and check it OpenJML.  

   NB there may be errors in the code that you have to fix to stop 
   OpenJML from complaining, as these complaints of OpenJML 
   point to real bugs in the code. But keep changes to the code to
   a minimum of what is strictly needed. 
   Mark any changes you have made in the code with a comment,
   eg to indicate that you replaced <= by <.

   You should add enough annotations to stop OpenJML complaining,
   but you should ALSO specify invariants discussed below:

   1) We do not want to represent 1.55 euro as an object with
         euro  == 0
         cents == 155
      (Note that the "equals" method will not be correct if we allow 
       this.)
      Specify an invariant that rules this out.

   2) We do not want to represent 1.55 euro as an object with
         euros =  2
         cents =  -45
      Specify one (or more) invariant(s) that rule this out. But note that
      we DO want to allow negative amounts, otherwise the method negate 
      can't be allowed.
      It may be useful to use the JML notation ==> (for implication) in 
      your invariants.

   While developing your specs, it may be useful to use the keywords
      assert
   to add additional assertions in source code, to find out what 
   OpenJML can - or cannot - prove at a given program point.

*/

//@ nullable_by_default                      // Do not remove this annotation
public class Amount{

    /*
    Las pulgas que se arreglaron estan señaladas con el comentario "PULGA"
    */

    private /*@ spec_public @*/ int cents;
    private /*@ spec_public @*/ int euros;

    //@ public invariant cents >= -99 && cents <= 99;
    //@ public invariant euros > -1000000000 && euros < 1000000000;
    //@ public invariant (euros > 0 ==> cents >= 0);
    //@ public invariant (euros < 0 ==> cents <= 0);

    //@ requires cents_input >= -99 && cents_input <= 99;
    //@ requires euros_input > -1000000000 && euros_input < 1000000000;
    //@ requires (euros_input >= 1 ==> cents_input >= 0);
    //@ requires (euros_input <= -1 ==> cents_input <= 0);
    //@ ensures this.euros == euros_input;
    //@ ensures this.cents == cents_input;
    //@ assignable \nothing;
    public Amount(int euros_input, int cents_input){
        this.euros = euros_input;
        this.cents = cents_input;
    }

    //@ ensures \result != null;
    //@ ensures \result.euros == -this.euros && \result.cents == -this.cents;
    //@ pure
    public Amount negate(){
        return new Amount(-euros, -cents); // PULGA: los parámetros estaban en orden inverso
    }

    //@ requires a != null;
    //@ requires euros - a.euros > -100000000 && euros - a.euros < 100000000;
    //@ ensures \result != null;
    //@ ensures \result.euros == this.euros - a.euros || \result != null;
    //@ pure
    public Amount subtract(Amount a){
        return this.add(a.negate());
    }

    //@ requires a != null;
    //@ requires euros + a.euros > -100000000 && euros + a.euros < 100000000;
    //@ requires cents + a.cents > -200 && cents + a.cents < 200;
    //@ ensures \result != null;
    //@ pure
    public Amount add(Amount a){
        int new_euros = euros + a.euros;
        int new_cents = cents + a.cents;
        if (new_cents < -99) { // PULGA: el límite inferior es -99, no -100
            new_cents = new_cents + 100;
            new_euros = new_euros - 1;
        }
        if (new_cents > 99) { // PULGA: el límite superior es 99, no 100
            new_cents = new_cents - 100;
            new_euros = new_euros + 1; // PULGA: el signo de la operación era incorrecto
        }
        if (new_cents < 0 && new_euros > 0) {
            new_cents = new_cents + 100;
            new_euros = new_euros - 1;
        }
        if (new_cents > 0 && new_euros < 0) { // PULGA: las condiciones no estrictas ahora si
            new_cents = new_cents - 100;
            new_euros = new_euros + 1;
        }
        //@ assert new_cents >= -99 && new_cents <= 99;
        //@ assert (new_euros >= 1) ==> (new_cents >= 0);
        //@ assert (new_euros <= -1) ==> (new_cents <= 0);
        return new Amount(new_euros, new_cents);
    }

    //@ requires a != null;
    //@ ensures \result == (euros == a.euros && cents == a.cents);
    public boolean equal(Amount a) {
        if (a==null) return false;
        else return (euros == a.euros && cents == a.cents);
    }
}