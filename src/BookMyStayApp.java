import java.util.*;

// ================= UC1 → Application Entry & Welcome Message =================
public class BookMyStayApp {

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("Welcome to Book My Stay");
        System.out.println("Hotel Booking Management System");
        System.out.println("Version 3.2");
        System.out.println("=================================");
        System.out.println("Application started successfully.\n");

        // ================= UC2 → Basic Room Types =================
        Room single = new SingleRoom(50.0);
        Room doubleR = new DoubleRoom(80.0);
        Room suite = new SuiteRoom(150.0);

        // ================= UC3 → Inventory =================
        RoomInventory inventory = new RoomInventory();
        inventory.registerRoom(single.getName(), 5);
        inventory.registerRoom(doubleR.getName(), 3);
        inventory.registerRoom(suite.getName(), 2);

        // ================= UC5 → Booking Queue =================
        BookingRequestQueue bookingQueue = new BookingRequestQueue();

        Reservation r1 = new Reservation("Alice", "Single Room");
        Reservation r2 = new Reservation("Bob", "Double Room");
        Reservation r3 = new Reservation("Charlie", "Suite Room");

        bookingQueue.addRequest(r1);
        bookingQueue.addRequest(r2);
        bookingQueue.addRequest(r3);

        // ================= UC6 → Booking Service =================
        BookingService bookingService = new BookingService(inventory);

        System.out.println("\n--- Reservation Confirmation ---");
        bookingService.processQueue(bookingQueue);

        // ================= UC7 → Add-On Service =================
        AddOnServiceManager serviceManager = new AddOnServiceManager();

        AddOnService wifi = new AddOnService("WiFi", 10);
        AddOnService breakfast = new AddOnService("Breakfast", 20);
        AddOnService spa = new AddOnService("Spa", 50);

        System.out.println("\n--- Add-On Services ---");

        serviceManager.addService(r1.getReservationId(), wifi);
        serviceManager.addService(r1.getReservationId(), breakfast);

        serviceManager.addService(r2.getReservationId(), spa);

        serviceManager.displayServices(r1.getReservationId());
        serviceManager.displayServices(r2.getReservationId());

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
    public void displayDetails() { System.out.println("Single Room | Price: $" + price); }
    public String getName() { return "Single Room"; }
}

class DoubleRoom extends Room {
    public DoubleRoom(double price) { super(price); }
    public void displayDetails() { System.out.println("Double Room | Price: $" + price); }
    public String getName() { return "Double Room"; }
}

class SuiteRoom extends Room {
    public SuiteRoom(double price) { super(price); }
    public void displayDetails() { System.out.println("Suite Room | Price: $" + price); }
    public String getName() { return "Suite Room"; }
}

// ================= UC3 → Inventory =================
class RoomInventory {
    private Map<String, Integer> inventory = new HashMap<>();
    public void registerRoom(String roomName, int count) { inventory.put(roomName, count); }
    public int getAvailability(String roomName) { return inventory.getOrDefault(roomName, 0); }
    public boolean allocateRoom(String roomName) {
        int avail = getAvailability(roomName);
        if (avail > 0) {
            inventory.put(roomName, avail - 1);
            return true;
        }
        return false;
    }
}

// ================= Reservation =================
class Reservation {
    private String guestName;
    private String roomType;
    private String reservationId;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
        this.reservationId = guestName.substring(0,2).toUpperCase() + System.nanoTime()%1000;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
    public String getReservationId() { return reservationId; }
}

// ================= UC5 → Booking Queue =================
class BookingRequestQueue {
    private Queue<Reservation> queue = new LinkedList<>();
    public void addRequest(Reservation r) { queue.offer(r); }
    public Reservation getNextRequest() { return queue.poll(); }
    public boolean hasNext() { return !queue.isEmpty(); }
}

// ================= UC6 → Booking Service =================
class BookingService {
    private RoomInventory inventory;
    private int roomIdCounter = 1;

    public BookingService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    public void processReservation(Reservation r) {
        if (inventory.allocateRoom(r.getRoomType())) {
            String roomId = r.getRoomType().substring(0, 2).toUpperCase() + roomIdCounter++;
            System.out.println(r.getGuestName() + " confirmed | Room ID: " + roomId +
                    " | Reservation ID: " + r.getReservationId());
        } else {
            System.out.println(r.getGuestName() + " booking failed");
        }
    }

    public void processQueue(BookingRequestQueue queue) {
        while (queue.hasNext()) {
            processReservation(queue.getNextRequest());
        }
    }
}

// ================= UC7 → Add-On Service =================
class AddOnService {
    private String name;
    private double cost;

    public AddOnService(String name, double cost) {
        this.name = name;
        this.cost = cost;
    }

    public double getCost() { return cost; }

    public String toString() {
        return name + " ($" + cost + ")";
    }
}

// ================= UC7 → Service Manager =================
class AddOnServiceManager {
    private Map<String, List<AddOnService>> serviceMap = new HashMap<>();

    public void addService(String reservationId, AddOnService service) {
        serviceMap.putIfAbsent(reservationId, new ArrayList<>());
        serviceMap.get(reservationId).add(service);
    }

    public void displayServices(String reservationId) {
        List<AddOnService> services = serviceMap.get(reservationId);

        if (services == null) {
            System.out.println("No services for " + reservationId);
            return;
        }

        double total = 0;
        System.out.println("Services for " + reservationId + ":");

        for (AddOnService s : services) {
            System.out.println("- " + s);
            total += s.getCost();
        }

        System.out.println("Total Add-On Cost: $" + total);
    }
}