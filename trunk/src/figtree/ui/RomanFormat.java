/*
 RomanFormat.java (5.1k) is a Java class for converting to/from Roman numerals.

 Whilst there are a number of styles for writing Roman numerals (several of which
 are implemented in the above code), there appears to be a simple algorithm that
 can correctly parse almost any 'reasonable'-looking Roman number (sufficiently
 simple that I'm inclined to treat it as a test for whether a number is valid or
 not).

 All this code is licensed under a BSD-like license.

 http://www.hawaga.org.uk/ben/tech/roman.html
*/

package figtree.ui;

import java.text.*;

/**
 *  This NumberFormat converts long integers to and from Roman Numeral notation.
 *  Once an instance has been created, the format and parse methods may be used
 *  as defined in java.text.NumberFormat.
 *
 * @author Ben Clifford
 * @version $Id$
 *
 * $HeadURL$
 *
 * $LastChangedBy$
 * $LastChangedDate$
 * $LastChangedRevision$
 */

public class RomanFormat extends NumberFormat
{


    /** This method returns null. I have found no meaningful translation
     of floating point numbers to Roman numerals, however the NumberFormat
     method requires that it is implemented. Perhaps should just cast
     the double to a long and format accordingly.
     */

    public StringBuffer format(double n, StringBuffer a, FieldPosition p)
    {
        return format((long)n, a, p);
    }


    /** This nested class is used to map Roman symbols onto their numerical
     values. Used in the Roman class.
     */

    public static class SymTab
    {
        /** Roman symbol */
        char symbol;

        /** Numerical value */
        long value;

        /** Constructor to build a SymTab from supplied symbol and value
         @param s Roman symbol
         @param v Numerical value
         */
        public SymTab(char s, long v) { this.symbol=s; this.value=v; }
    };


    /** This table maps individual Roman symbols onto their numerical
     values.<br>
     Unfortunately, JavaDoc JDK 1.1 does not create documentation
     for the inner class Roman.SymTab, so the reader
     cannot see the definition.
     */

    public static RomanFormat.SymTab syms[]= {
            new RomanFormat.SymTab('M',1000),
            new RomanFormat.SymTab('D',500),
            new RomanFormat.SymTab('C',100),
            new RomanFormat.SymTab('L',50),
            new RomanFormat.SymTab('X',10),
            new RomanFormat.SymTab('V',5),
            new RomanFormat.SymTab('I',1)
    };


    /** This method converts a Roman Numeral string to a long integer. It does
     not check that the string is in the correct format - for some incorrectly
     formatted numbers, i.e. iix, it will produce a number. For others,
     it will throw an exception.

     @param text string of Roman Numerals
     @param parsePosition the place to start parsing
     @return A Long object containing the parsed Roman numeral
     */

    public Number parse(String text, ParsePosition parsePosition)
    {

        String s = text.substring(parsePosition.getIndex());

        long tot=0,max=0;
        char ch[]=s.toUpperCase().toCharArray();
        int i,p;
        for(p=ch.length-1;p>=0;p--)
        {
            for(i=0;i<syms.length;i++)
            {
                if(syms[i].symbol==ch[p])
                {
                    if(syms[i].value>=max)
                        tot+= (max = syms[i].value);
                    else
                        tot-= syms[i].value;
                }
            }
        }

        // say that we parsed the whole string
        parsePosition.setIndex(s.length());
        return new Long(tot);
    }


    /** This method converts a Roman Numeral string to a long integer. It does
     not check that the string is in the correct format - for some incorrectly
     formatted numbers, i.e. iix, it will produce a number. For others,
     it will throw an exception.

     @param s string of Roman Numerals
     @return The integer representation of the Numerals
     */

    public static long toLong(String s)
    {
        long tot=0,max=0;
        char ch[]=s.toUpperCase().toCharArray();
        int i,p;
        for(p=ch.length-1;p>=0;p--)
        {
            for(i=0;i<syms.length;i++)
            {
                if(syms[i].symbol==ch[p])
                {
                    if(syms[i].value>=max)
                        tot+= (max = syms[i].value);
                    else
                        tot-= syms[i].value;
                }
            }
        }
        return tot;
    };


    /** This method converts the supplied long into capitalised Roman
     numerals.<br>
     BUG: the method does not take account of the
     <code>FieldPosition p</code> parameter.

     @param n The number to be converted into Roman numerals
     @param s The StringBuffer into which the output is to be placed.
     @return The StringBuffer s
     */

    public StringBuffer format(long n, StringBuffer s, FieldPosition p)
    {
        int i;
        while(n>0)
        {
            for(i=0;i<syms.length;i++)
            {
                if(syms[i].value<=n)
                {
                    int shift=i+(i%2);
                    if( i>0 && shift<syms.length &&
                            (syms[i-1].value-syms[shift].value) <= n )
                    {
                        s.append( syms[shift].symbol);
                        s.append( syms[i-1].symbol);
                        n= n - syms[i-1].value + syms[shift].value;

                        i=-1;
                    }
                    else
                    {
                        s.append(syms[i].symbol);
                        n-=syms[i].value;
                        i=-1;
                    }
                }
            }
        }
        return s;
    }


    /** This method converts a long integer to capitalised Roman notation.

     @param n The integer to convert to Roman Numerals.
     @return A String object containing the Roman Numerals.

     */

    public static String toRoman(long n)
    {
        int i;
        String s;
        s="";
        while(n>0)
        {
            for(i=0;i<syms.length;i++)
            {
                if(syms[i].value<=n)
                {
                    int shift=i+(i%2);
                    if( i>0 && shift<syms.length &&
                            (syms[i-1].value-syms[shift].value) <= n )
                    {
                        s= s+ syms[shift].symbol + syms[i-1].symbol;
                        n= n - syms[i-1].value + syms[shift].value;

                        i=-1;


                    }
                    else
                    {
                        s+=syms[i].symbol;
                        n-=syms[i].value;
                        i=-1;
                    }
                }
            }
        }
        return s;
    }

}
