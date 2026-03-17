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

        bookingQueue.addRequest(new Reservation("Alice", "Single Room"));
        bookingQueue.addRequest(new Reservation("Bob", "Double Room"));
        bookingQueue.addRequest(new Reservation("Charlie", "Single Room"));

        // ================= UC8 =================
        BookingHistory history = new BookingHistory();

        // ================= UC6 =================
        BookingService bookingService = new BookingService(inventory, history);

        // ================= UC11 (THREADS) =================
        Thread t1 = new Thread(new ConcurrentBookingProcessor(bookingQueue, bookingService));
        Thread t2 = new Thread(new ConcurrentBookingProcessor(bookingQueue, bookingService));

        System.out.println("\n--- Concurrent Booking Processing ---");
        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (Exception e) {}

        // ================= UC10 =================
        CancellationService cancelService = new CancellationService(inventory, history);

        System.out.println("\n--- Cancellation ---");
        if (!history.getAllReservations().isEmpty()) {
            cancelService.cancelReservation(history.getAllReservations().get(0).getReservationId());
        }

        cancelService.cancelReservation("INVALID123");

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

    public synchronized void registerRoom(String roomName, int count) {
        inventory.put(roomName, count);
    }

    public synchronized boolean allocateRoom(String roomName) {
        int avail = inventory.getOrDefault(roomName, 0);
        if (avail > 0) {
            inventory.put(roomName, avail - 1);
            return true;
        }
        return false;
    }

    public synchronized void releaseRoom(String roomName) {
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

    public synchronized void addRequest(Reservation r) { queue.offer(r); }

    public synchronized boolean hasNext() { return !queue.isEmpty(); }

    public synchronized Reservation getNextRequest() { return queue.poll(); }
}

// ================= UC8 → History =================
class BookingHistory {
    private List<Reservation> history = new ArrayList<>();

    public synchronized void addReservation(Reservation r) {
        history.add(r);
    }

    public List<Reservation> getAllReservations() {
        return history;
    }

    public synchronized boolean removeReservation(String id) {
        return history.removeIf(r -> r.getReservationId().equals(id));
    }

    public synchronized Reservation findReservation(String id) {
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

    public synchronized void processReservation(Reservation r) {

        if (inventory.allocateRoom(r.getRoomType())) {

            String roomId = r.getRoomType().substring(0, 2).toUpperCase() + roomIdCounter++;

            System.out.println(Thread.currentThread().getName() +
                    " → " + r.getGuestName() + " confirmed | Room ID: " + roomId);

            history.addReservation(r);

        } else {
            System.out.println(Thread.currentThread().getName() +
                    " → " + r.getGuestName() + " booking failed");
        }
    }
}

// ================= UC11 → Thread Processor =================
class ConcurrentBookingProcessor implements Runnable {

    private BookingRequestQueue queue;
    private BookingService service;

    public ConcurrentBookingProcessor(BookingRequestQueue queue, BookingService service) {
        this.queue = queue;
        this.service = service;
    }

    public void run() {
        while (true) {

            Reservation r;

            synchronized (queue) {
                if (!queue.hasNext()) break;
                r = queue.getNextRequest();
            }

            service.processReservation(r);

            try {
                Thread.sleep(100);
            } catch (Exception e) {}
        }
    }
}

// ================= UC10 → Cancellation =================
class CancellationService {

    private RoomInventory inventory;
    private BookingHistory history;

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

        rollbackStack.push(reservationId);

        inventory.releaseRoom(r.getRoomType());

        history.removeReservation(reservationId);

        System.out.println("Cancelled successfully: " + reservationId);
    }
}