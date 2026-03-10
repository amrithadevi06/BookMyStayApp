import java.util.*;

// ================= UC1 → Application Entry & Welcome Message =================
/**
 * Book My Stay - Hotel Booking Management System
 * Version 3.1
 * Demonstrates UC1 → UC5 incrementally
 */
public class BookMyStayApp {

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("Welcome to Book My Stay");
        System.out.println("Hotel Booking Management System");
        System.out.println("Version 3.1");
        System.out.println("=================================");
        System.out.println("Application started successfully.\n");

        // ================= UC2 → Basic Room Types & Static Availability =================
        Room single = new SingleRoom(50.0);
        Room doubleR = new DoubleRoom(80.0);
        Room suite = new SuiteRoom(150.0);

        System.out.println("Room Details and Availability:");
        single.displayDetails();
        System.out.println("Available: 5");
        doubleR.displayDetails();
        System.out.println("Available: 3");
        suite.displayDetails();
        System.out.println("Available: 2");

        // ================= UC3 → Centralized Room Inventory Management =================
        RoomInventory inventory = new RoomInventory();
        inventory.registerRoom("Single Room", 5);
        inventory.registerRoom("Double Room", 3);
        inventory.registerRoom("Suite Room", 2);

        System.out.println("\n--- Inventory State ---");
        inventory.displayInventory();

        // ================= UC4 → Room Search & Availability Check =================
        System.out.println("\n--- Room Search (Available Only) ---");
        for (Room room : Arrays.asList(single, doubleR, suite)) {
            int available = inventory.getAvailability(room.getName());
            if (available > 0) {
                room.displayDetails();
                System.out.println("Available: " + available);
            }
        }

        // ================= UC5 → Booking Request (First-Come-First-Served) =================
        BookingRequestQueue bookingQueue = new BookingRequestQueue();

        // Sample booking requests
        bookingQueue.addRequest(new Reservation("Alice", "Single Room"));
        bookingQueue.addRequest(new Reservation("Bob", "Double Room"));
        bookingQueue.addRequest(new Reservation("Charlie", "Suite Room"));
        bookingQueue.addRequest(new Reservation("Diana", "Single Room"));

        System.out.println("\n--- Booking Request Queue ---");
        bookingQueue.displayQueue();

        System.out.println("\nApplication execution completed.");
    }
}

// ================= UC2 → Room Classes =================
abstract class Room {
    protected double price;
    public Room(double price) { this.price = price; }
    public abstract void displayDetails();
    public abstract String getName();
}

class SingleRoom extends Room {
    public SingleRoom(double price) { super(price); }
    public void displayDetails() { System.out.println("Single Room | Beds: 1 | Price: $" + price); }
    public String getName() { return "Single Room"; }
}

class DoubleRoom extends Room {
    public DoubleRoom(double price) { super(price); }
    public void displayDetails() { System.out.println("Double Room | Beds: 2 | Price: $" + price); }
    public String getName() { return "Double Room"; }
}

class SuiteRoom extends Room {
    public SuiteRoom(double price) { super(price); }
    public void displayDetails() { System.out.println("Suite Room | Beds: 3 | Price: $" + price); }
    public String getName() { return "Suite Room"; }
}

// ================= UC3 → Inventory Management =================
class RoomInventory {
    private Map<String, Integer> inventory = new HashMap<>();
    public void registerRoom(String roomName, int count) { inventory.put(roomName, count); }
    public int getAvailability(String roomName) { return inventory.getOrDefault(roomName, 0); }
    public void displayInventory() {
        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + " rooms available");
        }
    }
}

// ================= UC5 → Booking Request =================
class Reservation {
    private String guestName;
    private String roomType;
    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }
    public String toString() { return guestName + " requested " + roomType; }
}

class BookingRequestQueue {
    private Queue<Reservation> queue = new LinkedList<>();
    public void addRequest(Reservation r) { queue.offer(r); }
    public void displayQueue() {
        for (Reservation r : queue) {
            System.out.println(r);
        }
    }
}