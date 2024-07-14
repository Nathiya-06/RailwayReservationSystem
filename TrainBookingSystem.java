import java.util.*;

class TrainBookingSystem {
    private static final int TOTAL_SEATS = 8;
    private static final int MAX_WAITLIST = 2;
    private static int pnrCounter = 1;

    private static final String[] stations = {"A", "B", "C", "D", "E"};

    private Map<Integer, Booking> bookings = new HashMap<>();
    private List<Booking> waitlist = new ArrayList<>();
    private Seat[] seats = new Seat[TOTAL_SEATS];

    public static void main(String[] args) {
        TrainBookingSystem system = new TrainBookingSystem();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Enter command (book,cancel,chart,exit): ");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Thankyou for using Train Booking System :) ");
                break;
            }
            system.processInput(input, scanner);
        }
        scanner.close();
    }

    private void processInput(String input, Scanner scanner) {
        String[] parts = input.split(",");
        try {
            switch (parts[0].toLowerCase()) {
                case "book":
                    if (parts.length != 4) {
                        System.out.println("Enter booking details in the format: book,fromStation,toStation,passengerCount");
                        input = scanner.nextLine();
                        parts = input.split(",");
                    }
                    bookTicket(parts[1], parts[2], Integer.parseInt(parts[3]));
                    break;
                case "cancel":
                    if (parts.length != 3) {
                        System.out.println("Enter cancel details in the format: cancel,pnr,seatsToCancel");
                        input = scanner.nextLine();
                        parts = input.split(",");
                    }
                    cancelTicket(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                    break;
                case "chart":
                    printChart();
                    break;
                default:
                    System.out.println("Invalid command.");
                    break;
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.out.println("Invalid input format. Please try again.");
        }
    }

    private void bookTicket(String from, String to, int passengerCount) {
        int fromIndex = getIndex(from);
        int toIndex = getIndex(to);
        List<Integer> availableSeats = getAvailableSeats(fromIndex, toIndex);

        if (availableSeats.size() >= passengerCount) {
            List<Integer> bookedSeats = new ArrayList<>();
            for (int i = 0; i < passengerCount; i++) {
                int seatIndex = availableSeats.get(i);
                seats[seatIndex] = new Seat(fromIndex, toIndex);
                bookedSeats.add(seatIndex + 1);
            }
            bookings.put(pnrCounter, new Booking(pnrCounter, from, to, bookedSeats));
            System.out.println("PNR " + pnrCounter + " booked: " + bookedSeats);
            pnrCounter++;
        } else {
            if (waitlist.size() < MAX_WAITLIST) {
                waitlist.add(new Booking(pnrCounter, from, to, Collections.emptyList()));
                System.out.println("PNR " + pnrCounter + " booked with " + (passengerCount - availableSeats.size()) + " WL tickets");
                pnrCounter++;
            } else {
                System.out.println("Booking failed. Not enough seats and waitlist is full.");
            }
        }
    }

    private void cancelTicket(int pnr, int seatsToCancel) {
        Booking booking = bookings.get(pnr);
        if (booking != null) {
            for (int i = 0; i < seatsToCancel && !booking.seatNumbers.isEmpty(); i++) {
                int seatNumber = booking.seatNumbers.remove(0);
                seats[seatNumber - 1] = null;
            }
            if (booking.seatNumbers.isEmpty()) {
                bookings.remove(pnr);
            }
            System.out.println("PNR " + pnr + " seats cancelled: " + seatsToCancel);
            handleWaitlist();
        } else {
            System.out.println("No booking found with PNR: " + pnr);
        }
    }

    private void handleWaitlist() {
        Iterator<Booking> iterator = waitlist.iterator();
        while (iterator.hasNext()) {
            Booking booking = iterator.next();
            List<Integer> availableSeats = getAvailableSeats(getIndex(booking.fromStation), getIndex(booking.toStation));
            int neededSeats = TOTAL_SEATS - booking.seatNumbers.size();

            if (availableSeats.size() >= neededSeats) {
                for (int i = 0; i < neededSeats; i++) {
                    int seatIndex = availableSeats.get(i);
                    seats[seatIndex] = new Seat(getIndex(booking.fromStation), getIndex(booking.toStation));
                    booking.seatNumbers.add(seatIndex + 1);
                }
                bookings.put(booking.pnr, booking);
                iterator.remove();
                System.out.println("Waitlisted PNR " + booking.pnr + " confirmed with seats: " + booking.seatNumbers);
            } else {
                break;
            }
        }
    }

    private void printChart() {
        System.out.println("Booking Chart:");
        System.out.println("Seats status from A to E:");
        System.out.print("Seat No: ");
        for (int i = 1; i <= TOTAL_SEATS; i++) {
            System.out.print(i + " ");
        }
        System.out.println();

        for (int i = 0; i < stations.length - 1; i++) {
            for (int j = i + 1; j < stations.length; j++) {
                System.out.print(stations[i] + " to " + stations[j] + ": ");
                for (int k = 0; k < TOTAL_SEATS; k++) {
                    Seat seat = seats[k];
                    if (seat != null && seat.from <= i && seat.to >= j) {
                        System.out.print("* ");
                    } else {
                        System.out.print("_ ");
                    }
                }
                System.out.println();
            }
        }

        for (Booking booking : waitlist) {
            System.out.println("Waitlisted PNR " + booking.pnr + " from " + booking.fromStation + " to " + booking.toStation);
        }
    }

    private List<Integer> getAvailableSeats(int from, int to) {
        List<Integer> availableSeats = new ArrayList<>();
        for (int i = 0; i < seats.length; i++) {
            Seat seat = seats[i];
            if (seat == null || (seat.to <= from || seat.from >= to)) {
                availableSeats.add(i);
            }
        }
        return availableSeats;
    }

    private int getIndex(String station) {
        for (int i = 0; i < stations.length; i++) {
            if (stations[i].equals(station)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Invalid station: " + station);
    }

    private class Seat {
        int from, to;

        Seat(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }

    private class Booking {
        int pnr;
        String fromStation, toStation;
        List<Integer> seatNumbers;

        Booking(int pnr, String fromStation, String toStation, List<Integer> seatNumbers) {
            this.pnr = pnr;
            this.fromStation = fromStation;
            this.toStation = toStation;
            this.seatNumbers = new ArrayList<>(seatNumbers);
        }
    }
}
