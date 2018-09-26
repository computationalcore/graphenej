package cy.agorise.graphenej.network;

import cy.agorise.graphenej.stats.ExponentialMovingAverage;

/**
 * Class that represents a full node and is used to keep track of its round-trip time measured in milliseconds.
 */
public class FullNode implements Comparable {

    private String mUrl;
    private ExponentialMovingAverage latency;
    private boolean isConnected;

    private FullNode(){}

    public FullNode(String url){
        latency = new ExponentialMovingAverage(ExponentialMovingAverage.DEFAULT_ALPHA);
        this.mUrl = url;
    }

    /**
     * Full node URL getter
     * @return
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * Full node URL setter
     * @param mUrl
     */
    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    /**
     *
     * @return  The exponential moving average object instance
     */
    public ExponentialMovingAverage getLatencyAverage(){
        return latency;
    }

    /**
     *
     * @return  The latest latency average value
     */
    public double getLatencyValue() {
        return latency.getAverage();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    /**
     * Method that updates the latency average with a new value.
     * @param latency   Most recent latency sample to be added to the exponential average
     */
    public void addLatencyValue(double latency) {
        this.latency.updateValue(latency);
    }

    @Override
    public int compareTo(Object o) {
        FullNode node = (FullNode) o;
        return (int) Math.ceil(latency.getAverage() - node.getLatencyValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FullNode fullNode = (FullNode) o;
        return mUrl.equals(fullNode.getUrl());
    }

    @Override
    public int hashCode() {
        return mUrl.hashCode();
    }
}
