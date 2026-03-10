import java.util.*;

public class BookMyStayApp {

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("Welcome to Book My Stay");
        System.out.println("Hotel Booking Management System");
        System.out.println("Version 3.1");
        System.out.println("=================================");
        System.out.println("Application started successfully.\n");

        // --- Room Setup ---
        Room single = new SingleRoom(50.0);
        Room doubleR = new DoubleRoom(80.0);
        Room suite = new SuiteRoom(150.0);

        RoomInventory inventory = new RoomInventory();
        inventory.registerRoom(single.type, 5);
        inventory.registerRoom(doubleR.type, 3);
        inventory.registerRoom(suite.type, 2);

        // --- UC2 Display Rooms ---
        System.out.println("Room Details and Availability:");
        displayRoomWithAvailability(single, inventory);
        displayRoomWithAvailability(doubleR, inventory);
        displayRoomWithAvailability(suite, inventory);

        // --- UC4 Room Search (Read-Only) ---
        System.out.println("\n--- Room Search (Available Only) ---");
        Room[] rooms = {single, doubleR, suite};
        searchAvailableRooms(rooms, inventory);

        System.out.println("\nApplication execution completed.");
    }

    // Display room details and current availability
    public static void displayRoomWithAvailability(Room room, RoomInventory inventory) {
        room.displayDetails();
        System.out.println("Available: " + inventory.getAvailability(room.type));
    }

    // UC4: Search available rooms
    public static void searchAvailableRooms(Room[] rooms, RoomInventory inventory) {
        for (Room room : rooms) {
            int available = inventory.getAvailability(room.type);
            if (available > 0) { // Only show rooms with availability
                room.displayDetails();
                System.out.println("Available: " + available);
            }
        }
    }
}

// --- Room classes ---

abstract class Room {
    String type;
    int beds;
    double price;

    public Room(String type, int beds, double price) {
        this.type = type;
        this.beds = beds;
        this.price = price;
    }

    public void displayDetails() {
        System.out.println(type + " | Beds: " + beds + " | Price: $" + price);
    }
}

class SingleRoom extends Room {
    public SingleRoom(double price) {
        super("Single Room", 1, price);
    }
}

class DoubleRoom extends Room {
    public DoubleRoom(double price) {
        super("Double Room", 2, price);
    }
}

class SuiteRoom extends Room {
    public SuiteRoom(double price) {
        super("Suite Room", 3, price);
    }
}

// --- Inventory class ---

class RoomInventory {
    private final Map<String, Integer> inventory = new HashMap<>();

    public void registerRoom(String type, int count) {
        inventory.put(type, count);
    }

    public int getAvailability(String type) {
        return inventory.getOrDefault(type, 0);
    }

    public void updateAvailability(String type, int count) {
        inventory.put(type, count);
    }
}