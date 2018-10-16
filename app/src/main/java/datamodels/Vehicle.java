package datamodels;

/**
 * Created by Shamyyoun on 4/27/2015.
 */
public class Vehicle {
    private String id;
    private String password;
    private String name;
    private String purpose;
    private int licenceNumber;
    private int number;
    private String color;
    private String model;
    private int year;
    private String brand;
    private int trackingInterval;
    private String username;
    private boolean trackingTrip; // used to know if there is current trip to trcak or not

    public Vehicle(String id) {
        this.id = id;
    }

    public Vehicle(String id, String password, String name, String purpose, int licenceNumber,
                   int number, String color, String model, int year, String brand, int trackingInterval, String username) {
        this.id = id;
        this.password = password;
        this.name = name;
        this.purpose = purpose;
        this.licenceNumber = licenceNumber;
        this.number = number;
        this.color = color;
        this.model = model;
        this.year = year;
        this.brand = brand;
        this.trackingInterval = trackingInterval;
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public int getLicenceNumber() {
        return licenceNumber;
    }

    public void setLicenceNumber(int licenceNumber) {
        this.licenceNumber = licenceNumber;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getTrackingInterval() {
        return trackingInterval;
    }

    public void setTrackingInterval(int trackingInterval) {
        this.trackingInterval = trackingInterval;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTrackingTrip(boolean trackingTrip) {
        this.trackingTrip = trackingTrip;
    }

    public boolean getTrackingTrip() {
        return trackingTrip;
    }
}
