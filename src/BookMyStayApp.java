import java.util.*;
import java.io.*;

// ================= UC1 → Application Entry =================
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println("Welcome to Book My Stay");
        System.out.println("=================================\n");

        // ================= UC12 LOAD =================
        PersistenceService ps = new PersistenceService();
        Object[] data = ps.loadState();

        RoomInventory inventory;
        BookingHistory history;

        if (data != null) {
            inventory = (RoomInventory) data[0];
            history = (BookingHistory) data[1];
        } else {
            inventory = new RoomInventory();
            history = new BookingHistory();
        }

        // ================= UC2 =================
        Room single = new SingleRoom(50.0);
        Room doubleR = new DoubleRoom(80.0);
        Room suite = new SuiteRoom(150.0);

        // ================= UC3 =================
        inventory.registerRoom(single.getName(), 2);
        inventory.registerRoom(doubleR.getName(), 1);
        inventory.registerRoom(suite.getName(), 1);

        // ================= UC5 =================
        BookingRequestQueue bookingQueue = new BookingRequestQueue();

        bookingQueue.addRequest(new Reservation("Alice", "Single Room"));
        bookingQueue.addRequest(new Reservation("Bob", "Double Room"));
        bookingQueue.addRequest(new Reservation("Charlie", "Single Room"));

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

        // ================= UC12 SAVE =================
        ps.saveState(inventory, history);

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
class RoomInventory implements Serializable {
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
class Reservation implements Serializable {
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
class BookingHistory implements Serializable {
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

// ================= UC12 → Persistence =================
class PersistenceService {

    private static final String FILE_NAME = "system_state.ser";

    public void saveState(RoomInventory inventory, BookingHistory history) {

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {

            out.writeObject(inventory);
            out.writeObject(history);

            System.out.println("\nSystem state saved successfully.");

        } catch (Exception e) {
            System.out.println("\nError saving data.");
        }
    }

    public Object[] loadState() {

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE_NAME))) {

            RoomInventory inventory = (RoomInventory) in.readObject();
            BookingHistory history = (BookingHistory) in.readObject();

            System.out.println("System state loaded successfully.\n");

            return new Object[]{inventory, history};

        } catch (Exception e) {

            System.out.println("No previous data found. Starting fresh.\n");
            return null;
        }
    }
}