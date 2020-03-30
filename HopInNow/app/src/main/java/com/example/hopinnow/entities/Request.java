package com.example.hopinnow.entities;

import java.util.Date;

/**
 * Author: Shuwei Wang
 * Version: 1.0.0
 * request class which records the unfinished trip
 */
public class Request extends Ride implements Comparable<Request>{
    private Double estimatedFare;
    private String requestID;
    private boolean pickedUp;
    // manhatton distance to driver
    private double mdToDriver;
    // is arrived at destination
    private boolean arrivedAtDest;
    private boolean complete;
    private Double rating;
    private int acceptStatus;

    /**
     * empty constructor
     */
    public Request(){}

    /**
     * request constructor
     * @param driver
     *      driver of this request
     * @param rider
     *      rider of this request
     * @param pickUpLoc
     *      pickuplocation of class Latlong
     * @param dropOffLoc
     *      dropOffLoc of class Latlong
     * @param pickUpLocName
     *      name of the pickup location
     * @param dropOffLocName
     *      name of the drop off location
     * @param pickUpDateTime
     *      date time of pick up
     * @param car
     *      car information of this request
     * @param estimatedFare
     *      fee needed to pay estimation
     */
    public Request (String driver, String rider, LatLong pickUpLoc, LatLong dropOffLoc,
                    String pickUpLocName, String dropOffLocName, Date pickUpDateTime,
                    Car car, Double estimatedFare){
        super(driver,rider,pickUpLoc,dropOffLoc,pickUpLocName, dropOffLocName,pickUpDateTime,car);
        try {
            this.estimatedFare = estimatedFare;
            this.pickedUp = false;
            this.arrivedAtDest = false;
            this.complete = false;
            this.rating = -1.0;
            this.acceptStatus = 0;
        } catch(Exception e){
            throw e;
        }
    }

    /**
     * get estimated fare
     * @return
     */
    public Double getEstimatedFare() {
        if (estimatedFare == null){
            throw new NullPointerException();
        }
        else{
            return estimatedFare;
        }
    }

    /**
     * set the estimated fare
     * @param estimatedFare
     */
    public void setEstimatedFare(Double estimatedFare) {
        try{
            this.estimatedFare = estimatedFare;
        }
        catch (Exception e){
            throw e;
        }
    }

    /**
     * get the reqeust ID
     * can be null
     * @return
     */
    public String getRequestID() {
        return requestID;
    }

    /**
     * set request ID
     * @param requestID
     */
    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }


    public double getMdToDriver() {
        return mdToDriver;
    }

    public void setMdToDriver(double mdToDriver) {
        this.mdToDriver = mdToDriver;
    }

    @Override
    public int compareTo(Request request) {
        return (Double.compare(this.getMdToDriver(), request.getMdToDriver()));
    }

    /**
     * get isComplete
     * @return iscomplete
     *      return if the request is complete
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * set complete
     * @param complete
     *      indicate if the request if complete:
     */
    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    /**
     * get the rating
     * @return rating
     *      return the rating of this request
     */
    public Double getRating() {
        return rating;
    }

    /**
     * set the rating of the current request
     */
    public void setRating(Double rating) {
        this.rating = rating;
    }

    public int getAcceptStatus() {
        return this.acceptStatus;
    }

    public void setAcceptStatus(int acceptStatus) {
        this.acceptStatus = acceptStatus;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    public void setPickedUp(boolean pickedUp) {
        this.pickedUp = pickedUp;
    }

    public boolean isArrivedAtDest() {
        return arrivedAtDest;
    }

    public void setArrivedAtDest(boolean arrivedAtDest) {
        this.arrivedAtDest = arrivedAtDest;
    }
}
