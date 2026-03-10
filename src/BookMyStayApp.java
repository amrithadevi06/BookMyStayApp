/**
 * Book My Stay - Hotel Booking Management System
 * Version 3.1
 * Implements:
 *   UC1: Application Entry & Welcome Message
 *   UC2: Basic Room Types & Static Availability
 *   UC3: Centralized Room Inventory Management
 */

import java.util.*;

public class BookMyStayApp {

    public static void main(String[] args) {

        // ===== UC1: Welcome Message =====
        System.out.println("=================================");
        System.out.println("Welcome to Book My Stay");
        System.out.println("Hotel Booking Management System");
        System.out.println("Version 3.1");
        System.out.println("=================================");
        System.out.println("Application started successfully.");

        // ===== UC2: Room Objects =====
        Room single = new SingleRoom(50.0);
        Room doubleR = new DoubleRoom(80.0);
        Room suite = new SuiteRoom(150.0);

        // ===== UC3: Centralized Inventory =====
        RoomInventory inventory = new RoomInventory();
        inventory.registerRoom(single.type, 5);
        inventory.registerRoom(doubleR.type, 3);
        inventory.registerRoom(suite.type, 2);

        // Display room details and availability
        System.out.println("\nRoom Details and Availability:");
        single.displayDetails();
        System.out.println("Available: " + inventory.getAvailability(single.type));

        doubleR.displayDetails();
        System.out.println("Available: " + inventory.getAvailability(doubleR.type));

        suite.displayDetails();
        System.out.println("Available: " + inventory.getAvailability(suite.type));

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

// ===== UC3: Centralized Room Inventory Management =====
class RoomInventory {
    private HashMap<String, Integer> inventory;

    public RoomInventory() {
        inventory = new HashMap<>();
    }

    // Register a room type with its available count
    public void registerRoom(String roomType, int count) {
        inventory.put(roomType, count);
    }

    // Get current availability
    public int getAvailability(String roomType) {
        return inventory.getOrDefault(roomType, 0);
    }

    // Update availability (e.g., after a booking)
    public void updateAvailability(String roomType, int newCount) {
        if (inventory.containsKey(roomType)) {
            inventory.put(roomType, newCount);
        }
    }
}