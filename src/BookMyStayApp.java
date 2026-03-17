import java.util.*;

// ================= UC1 → Application Entry =================
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println("Welcome to Book My Stay");
        System.out.println("=================================\n");

        // ================= UC2 =================
        Room single = new SingleRoom(50.0);
        Room doubleR = new DoubleRoom(80.0);
        Room suite = new SuiteRoom(150.0);

        // ================= UC3 =================
        RoomInventory inventory = new RoomInventory();
        inventory.registerRoom(single.getName(), 2);
        inventory.registerRoom(doubleR.getName(), 1);
        inventory.registerRoom(suite.getName(), 1);

        // ================= UC5 =================
        BookingRequestQueue bookingQueue = new BookingRequestQueue();

        Reservation r1 = new Reservation("Alice", "Single Room");
        Reservation r2 = new Reservation("Bob", "Double Room");
        Reservation r3 = new Reservation("Charlie", "Suite Room");

        // ❌ Invalid cases (UC9)
        Reservation r4 = new Reservation("", "Single Room");
        Reservation r5 = new Reservation("David", "Luxury Room");

        bookingQueue.addRequest(r1);
        bookingQueue.addRequest(r2);
        bookingQueue.addRequest(r3);
        bookingQueue.addRequest(r4);
        bookingQueue.addRequest(r5);

        // ================= UC8 =================
        BookingHistory history = new BookingHistory();

        // ================= UC6 =================
        BookingService bookingService = new BookingService(inventory, history);

        System.out.println("\n--- Reservation Processing ---");
        bookingService.processQueue(bookingQueue);

        // ================= UC7 =================
        AddOnServiceManager serviceManager = new AddOnServiceManager();

        AddOnService wifi = new AddOnService("WiFi", 10);
        AddOnService breakfast = new AddOnService("Breakfast", 20);

        System.out.println("\n--- Add-On Services ---");

        serviceManager.addService(r1.getReservationId(), wifi);
        serviceManager.addService(r1.getReservationId(), breakfast);

        serviceManager.displayServices(r1.getReservationId());

        // ================= UC8 =================
        history.displayHistory();

        BookingReportService reportService = new BookingReportService();
        reportService.generateReport(history.getAllReservations());

        System.out.println("\nApplication execution completed.");
    }
}

// ================= UC2 → Room Classes =================
abstract class Room {
    protected double price;
    public Room(double price) { this.price = price; }
    public abstract String getName();
}

class SingleRoom extends Room {
    public SingleRoom(double price) { super(price); }
    public String getName() { return "Single Room"; }
}

class DoubleRoom extends Room {
    public DoubleRoom(double price) { super(price); }
    public String getName() { return "Double Room"; }
}

class SuiteRoom extends Room {
    public SuiteRoom(double price) { super(price); }
    public String getName() { return "Suite Room"; }
}

// ================= UC3 → Inventory =================
class RoomInventory {
    private Map<String, Integer> inventory = new HashMap<>();

    public void registerRoom(String roomName, int count) {
        inventory.put(roomName, count);
    }

    public boolean allocateRoom(String roomName) {
        int avail = inventory.getOrDefault(roomName, 0);
        if (avail > 0) {
            inventory.put(roomName, avail - 1);
            return true;
        }
        return false;
    }

    // UC9 additions
    public boolean hasRoomType(String roomName) {
        return inventory.containsKey(roomName);
    }

    public boolean isAvailable(String roomName) {
        return inventory.getOrDefault(roomName, 0) > 0;
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
        this.reservationId = (guestName.length() >= 2 ? guestName.substring(0,2) : "XX").toUpperCase()
                + (int)(Math.random()*1000);
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
    public String getReservationId() { return reservationId; }
}

// ================= UC5 → Queue =================
class BookingRequestQueue {
    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation r) { queue.offer(r); }

    public boolean hasNext() { return !queue.isEmpty(); }

    public Reservation getNextRequest() { return queue.poll(); }
}

// ================= UC8 → History =================
class BookingHistory {
    private List<Reservation> history = new ArrayList<>();

    public void addReservation(Reservation r) {
        history.add(r);
    }

    public List<Reservation> getAllReservations() {
        return history;
    }

    public void displayHistory() {
        System.out.println("\n--- Booking History ---");
        for (Reservation r : history) {
            System.out.println(r.getGuestName() + " | " +
                    r.getRoomType() + " | ID: " + r.getReservationId());
        }
    }
}

// ================= UC9 → Exception =================
class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) {
        super(message);
    }
}

// ================= UC9 → Validator =================
class BookingValidator {

    private RoomInventory inventory;

    public BookingValidator(RoomInventory inventory) {
        this.inventory = inventory;
    }

    public void validate(Reservation r) throws InvalidBookingException {

        if (r.getGuestName() == null || r.getGuestName().trim().isEmpty()) {
            throw new InvalidBookingException("Guest name cannot be empty");
        }

        if (!inventory.hasRoomType(r.getRoomType())) {
            throw new InvalidBookingException("Invalid room type: " + r.getRoomType());
        }

        if (!inventory.isAvailable(r.getRoomType())) {
            throw new InvalidBookingException("No rooms available for: " + r.getRoomType());
        }
    }
}

// ================= UC6 → Booking Service =================
class BookingService {
    private RoomInventory inventory;
    private BookingHistory history;
    private BookingValidator validator;
    private int roomIdCounter = 1;

    public BookingService(RoomInventory inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
        this.validator = new BookingValidator(inventory);
    }

    public void processReservation(Reservation r) {
        try {

            validator.validate(r);

            if (inventory.allocateRoom(r.getRoomType())) {

                String roomId = r.getRoomType().substring(0, 2).toUpperCase() + roomIdCounter++;

                System.out.println(r.getGuestName() + " confirmed | Room ID: " + roomId +
                        " | Reservation ID: " + r.getReservationId());

                history.addReservation(r);
            }

        } catch (InvalidBookingException e) {
            System.out.println("Booking failed for " + r.getGuestName() + ": " + e.getMessage());
        }
    }

    public void processQueue(BookingRequestQueue queue) {
        while (queue.hasNext()) {
            processReservation(queue.getNextRequest());
        }
    }
}

// ================= UC7 → Add-On =================
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

// ================= UC8 → Report =================
class BookingReportService {

    public void generateReport(List<Reservation> history) {
        System.out.println("\n--- Booking Report ---");

        Map<String, Integer> countMap = new HashMap<>();

        for (Reservation r : history) {
            countMap.put(r.getRoomType(),
                    countMap.getOrDefault(r.getRoomType(), 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("Total Bookings: " + history.size());
    }
}