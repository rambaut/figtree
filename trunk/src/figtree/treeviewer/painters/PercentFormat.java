package figtree.treeviewer.painters;

import java.text.*;

/**
 <p>
 This NumberFormat converts numbers to and from percent notation.
 Once an instance has been created, the format and parse methods may be used
 as defined in java.text.NumberFormat.
 </p>

 @author Andrew Rambaut
 */

public class PercentFormat extends NumberFormat
{
    private final NumberFormat nf;

    public PercentFormat() {
        this.nf = new DecimalFormat();
    }


    /**
     * Returns the maximum number of digits allowed in the fraction portion of a
     * number.
     *
     * @see #setMaximumFractionDigits
     */
    @Override
    public int getMaximumFractionDigits() {
        return nf.getMaximumFractionDigits();
    }

    /**
     * Returns the maximum number of digits allowed in the integer portion of a
     * number.
     *
     * @see #setMaximumIntegerDigits
     */
    @Override
    public int getMaximumIntegerDigits() {
        return nf.getMaximumIntegerDigits();
    }

    /**
     * Returns the minimum number of digits allowed in the fraction portion of a
     * number.
     *
     * @see #setMinimumFractionDigits
     */
    @Override
    public int getMinimumFractionDigits() {
        return nf.getMinimumFractionDigits();
    }

    /**
     * Returns the minimum number of digits allowed in the integer portion of a
     * number.
     *
     * @see #setMinimumIntegerDigits
     */
    @Override
    public int getMinimumIntegerDigits() {
        return nf.getMinimumIntegerDigits();
    }

    /**
     * Sets the maximum number of digits allowed in the fraction portion of a
     * number. maximumFractionDigits must be >= minimumFractionDigits.  If the
     * new value for maximumFractionDigits is less than the current value
     * of minimumFractionDigits, then minimumFractionDigits will also be set to
     * the new value.
     *
     * @param newValue the maximum number of fraction digits to be shown; if
     *                 less than zero, then zero is used. The concrete subclass may enforce an
     *                 upper limit to this value appropriate to the numeric type being formatted.
     * @see #getMaximumFractionDigits
     */
    @Override
    public void setMaximumFractionDigits(int newValue) {
        nf.setMaximumFractionDigits(newValue);
    }

    /**
     * Sets the minimum number of digits allowed in the integer portion of a
     * number. minimumIntegerDigits must be <= maximumIntegerDigits.  If the
     * new value for minimumIntegerDigits exceeds the current value
     * of maximumIntegerDigits, then maximumIntegerDigits will also be set to
     * the new value
     *
     * @param newValue the minimum number of integer digits to be shown; if
     *                 less than zero, then zero is used. The concrete subclass may enforce an
     *                 upper limit to this value appropriate to the numeric type being formatted.
     * @see #getMinimumIntegerDigits
     */
    @Override
    public void setMinimumIntegerDigits(int newValue) {
        nf.setMinimumIntegerDigits(newValue);
    }

    /**
     * Sets the minimum number of digits allowed in the fraction portion of a
     * number. minimumFractionDigits must be <= maximumFractionDigits.  If the
     * new value for minimumFractionDigits exceeds the current value
     * of maximumFractionDigits, then maximumIntegerDigits will also be set to
     * the new value
     *
     * @param newValue the minimum number of fraction digits to be shown; if
     *                 less than zero, then zero is used. The concrete subclass may enforce an
     *                 upper limit to this value appropriate to the numeric type being formatted.
     * @see #getMinimumFractionDigits
     */
    @Override
    public void setMinimumFractionDigits(int newValue) {
        nf.setMinimumFractionDigits(newValue);
    }

    /**
     * Sets the maximum number of digits allowed in the integer portion of a
     * number. maximumIntegerDigits must be >= minimumIntegerDigits.  If the
     * new value for maximumIntegerDigits is less than the current value
     * of minimumIntegerDigits, then minimumIntegerDigits will also be set to
     * the new value.
     *
     * @param newValue the maximum number of integer digits to be shown; if
     *                 less than zero, then zero is used. The concrete subclass may enforce an
     *                 upper limit to this value appropriate to the numeric type being formatted.
     * @see #getMaximumIntegerDigits
     */
    @Override
    public void setMaximumIntegerDigits(int newValue) {
        nf.setMaximumIntegerDigits(newValue);
    }

    /**
     * Specialization of format.
     *
     * @see java.text.Format#format
     */
     public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
        return nf.format(number * 100.0, toAppendTo, pos).append("%");
    }

    /**
     * Specialization of format.
     *
     * @see java.text.Format#format
     */
     public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
        return nf.format(number * 100, toAppendTo, pos).append("%");
    }

    /**
     * @see java.text.Format#parseObject
     */
     public Number parse(String source, ParsePosition parsePosition) {
        return nf.parse(source, parsePosition).doubleValue() / 100.0;
    }
}