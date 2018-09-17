package cy.agorise.graphenej.stats;

/**
 * Class used to compute the Exponential Moving Average of a sequence of values.
 * For more details see <a href="https://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average">here</a>.
 */
public class ExponentialMovingAverage {
    public static final double DEFAULT_ALPHA = 0.5;
    private double alpha;
    private Double accumulatedValue;

    /**
     * Constructor, which takes only the alpha parameter as an argument.
     *
     * @param alpha The coefficient alpha represents the degree of weighting decrease, a constant
     *              smoothing factor between 0 and 1. A higher alpha discounts older observations faster.
     */
    public ExponentialMovingAverage(double alpha) {
        this.alpha = alpha;
    }

    /**
     * Method that updates the average with a new sample
     * @param value New value
     * @return      The updated average value
     */
    public double updateValue(double value) {
        if (accumulatedValue == null) {
            accumulatedValue = value;
            return value;
        }
        double newValue = accumulatedValue + alpha * (value - accumulatedValue);
        accumulatedValue = newValue;
        return newValue;
    }

    /**
     *
     * @return  Returns the current average value
     */
    public double getAverage(){
        return accumulatedValue == null ? 0 : accumulatedValue;
    }

    public void setAlpha(double alpha){
        this.alpha = alpha;
        this.accumulatedValue = null;
    }
}