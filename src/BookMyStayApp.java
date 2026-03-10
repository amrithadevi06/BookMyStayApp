/**
 * Book My Stay - Hotel Booking Management System
 * Version 2.1
 * Implements:
 *   UC1: Application Entry & Welcome Message
 *   UC2: Basic Room Types & Static Availability
 */

import java.util.*;

public class BookMyStayApp {

    public static void main(String[] args) {

        // ===== UC1: Welcome Message =====
        System.out.println("=================================");
        System.out.println("Welcome to Book My Stay");
        System.out.println("Hotel Booking Management System");
        System.out.println("Version 2.1");
        System.out.println("=================================");
        System.out.println("Application started successfully.");

        // ===== UC2: Basic Room Types & Static Availability =====

        // Initialize room objects
        Room single = new SingleRoom(50.0);
        Room doubleR = new DoubleRoom(80.0);
        Room suite = new SuiteRoom(150.0);

        // Static availability
        int singleAvailable = 5;
        int doubleAvailable = 3;
        int suiteAvailable = 2;

        // Display room details and availability
        System.out.println("\nRoom Details and Availability:");
        single.displayDetails();
        System.out.println("Available: " + singleAvailable);

        doubleR.displayDetails();
        System.out.println("Available: " + doubleAvailable);

        suite.displayDetails();
        System.out.println("Available: " + suiteAvailable);

        System.out.println("\nApplication execution completed.");
    }
}

// ===== UC2: Room Classes =====
abstract class Room {
    protected String type;
    protected int beds;
    protected double price;

    public Room(String type, int beds, double price) {
        this.type = type;
        this.beds = beds;
        this.price = price;
    }

    public abstract void displayDetails();
}

class SingleRoom extends Room {
    public SingleRoom(double price) { super("Single Room", 1, price); }
    public void displayDetails() { System.out.println(type + " | Beds: " + beds + " | Price: $" + price); }
}

class DoubleRoom extends Room {
    public DoubleRoom(double price) { super("Double Room", 2, price); }
    public void displayDetails() { System.out.println(type + " | Beds: " + beds + " | Price: $" + price); }
}

class SuiteRoom extends Room {
    public SuiteRoom(double price) { super("Suite Room", 3, price); }
    public void displayDetails() { System.out.println(type + " | Beds: " + beds + " | Price: $" + price); }
}