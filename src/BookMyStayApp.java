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

        bookingQueue.addRequest(r1);
        bookingQueue.addRequest(r2);

        // ================= UC8 =================
        BookingHistory history = new BookingHistory();

        // ================= UC6 =================
        BookingService bookingService = new BookingService(inventory, history);

        System.out.println("\n--- Reservation Processing ---");
        bookingService.processQueue(bookingQueue);

        // ================= UC10 =================
        CancellationService cancelService = new CancellationService(inventory, history);

        System.out.println("\n--- Cancellation ---");
        cancelService.cancelReservation(r1.getReservationId()); // valid
        cancelService.cancelReservation("INVALID123"); // invalid case

        // ================= UC8 =================
        history.displayHistory();

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

    // UC10 rollback
    public void releaseRoom(String roomName) {
        inventory.put(roomName, inventory.getOrDefault(roomName, 0) + 1);
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
        this.reservationId = guestName.substring(0,2).toUpperCase()
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

    // UC10 remove
    public boolean removeReservation(String id) {
        return history.removeIf(r -> r.getReservationId().equals(id));
    }

    public Reservation findReservation(String id) {
        for (Reservation r : history) {
            if (r.getReservationId().equals(id)) return r;
        }
        return null;
    }

    public void displayHistory() {
        System.out.println("\n--- Booking History ---");
        for (Reservation r : history) {
            System.out.println(r.getGuestName() + " | " +
                    r.getRoomType() + " | ID: " + r.getReservationId());
        }
    }
}

// ================= UC6 → Booking Service =================
class BookingService {
    private RoomInventory inventory;
    private BookingHistory history;
    private int roomIdCounter = 1;

    public BookingService(RoomInventory inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
    }

    public void processReservation(Reservation r) {

        if (inventory.allocateRoom(r.getRoomType())) {

            String roomId = r.getRoomType().substring(0, 2).toUpperCase() + roomIdCounter++;

            System.out.println(r.getGuestName() + " confirmed | Room ID: " + roomId +
                    " | Reservation ID: " + r.getReservationId());

            history.addReservation(r);

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

// ================= UC10 → Cancellation =================
class CancellationService {

    private RoomInventory inventory;
    private BookingHistory history;

    // Stack for rollback
    private Stack<String> rollbackStack = new Stack<>();

    public CancellationService(RoomInventory inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
    }

    public void cancelReservation(String reservationId) {

        Reservation r = history.findReservation(reservationId);

        if (r == null) {
            System.out.println("Cancellation failed: Reservation not found -> " + reservationId);
            return;
        }

        // rollback tracking
        rollbackStack.push(reservationId);

        // restore inventory
        inventory.releaseRoom(r.getRoomType());

        // remove from history
        history.removeReservation(reservationId);

        System.out.println("Cancelled successfully: " + reservationId);
    }
}