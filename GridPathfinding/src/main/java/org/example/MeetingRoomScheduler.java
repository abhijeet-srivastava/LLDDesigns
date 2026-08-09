package org.example;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MeetingRoomScheduler {
    //EmpId - booking-start-end
    //roomId - booking-start-end
    //roomId - start -(bookingId, end, roomId)
    //bookingId - bookings

    private final static int MAX_RECURRENCE = 20;

    Map<Integer, List<RoomBooking>>  employeeBookings;
    Map<Integer, TreeMap<Integer, RoomBooking>> roomBookings;
    Map<String, BookingDetails> bookings;
    private final int empCount;
    private final int roomCount;

    public MeetingRoomScheduler(int empCount, int roomCount) {
        this.empCount = empCount;
        this.roomCount = roomCount;
        this.employeeBookings = new HashMap<>();
        this.roomBookings = new HashMap<>();
        this.bookings = new HashMap<>();
        for(int i = 0; i < empCount; i++) {
            employeeBookings.put(i, new ArrayList<>());
        }
        for(int i = 0; i < roomCount; i++) {
            roomBookings.put(i, new TreeMap<>());
        }
    }

    boolean bookRoom(String bookingId, int employeeId, int roomId, int startTime, int duration, int repeatDuration) {
        if(duration <= 0 || startTime < 0 || duration >= repeatDuration) {
            return false;
        }
        List<RoomBooking> bookingsToCreate = new ArrayList<>();
        TreeMap<Integer, RoomBooking> existingRoomBookings = roomBookings.get(roomId);
        for(int i = 0; i < MAX_RECURRENCE; i++) {
            int meetStartTime = startTime + i*repeatDuration;
            int meetEndTime = meetStartTime + duration-1;
            if(existingRoomBookings.isEmpty()) {
                bookingsToCreate.add(new RoomBooking(bookingId, roomId, employeeId, meetStartTime, meetEndTime));
                continue;
            }
            var lastMeetBeforeCurrent = existingRoomBookings.floorEntry(meetStartTime);
            if(lastMeetBeforeCurrent != null && lastMeetBeforeCurrent.getValue().end >= meetStartTime) {
                return false;
            }
            var firstMeetAfterCurrent = existingRoomBookings.ceilingEntry(startTime);
            if(firstMeetAfterCurrent != null && firstMeetAfterCurrent.getKey() <= meetEndTime) {
                return false;
            }
            bookingsToCreate.add(new RoomBooking(bookingId, roomId, employeeId, meetStartTime, meetEndTime));
        }
        for(RoomBooking rb: bookingsToCreate) {
            employeeBookings.get(employeeId).add(rb);
            roomBookings.get(roomId).put(rb.start, rb);
        }
        bookings.put(bookingId, new BookingDetails(bookingId, roomId, employeeId));
        return true;
    }

    List<Integer> getAvailableRooms(int startTime, int endTime) {
        if(startTime > endTime) {
            return List.of();
        }
        List<Integer> res = new ArrayList<>();
        for(int ri = 0; ri < roomCount; ri++) {
            if(roomBookings.get(ri).isEmpty()) {
                res.add(ri);
            } else {
                var lastMeetBeforeStart = roomBookings.get(ri).floorEntry(startTime);
                if(lastMeetBeforeStart != null && lastMeetBeforeStart.getValue().end >= startTime) {
                    continue;
                }
                var firstMeetAfterStart = roomBookings.get(ri).ceilingEntry(startTime);
                if(firstMeetAfterStart != null && firstMeetAfterStart.getKey() <= endTime) {
                    continue;
                }
                res.add(ri);
            }
        }
        return res;
    }

    boolean cancelBooking(String bookingId) {
        if(!bookings.containsKey(bookingId)) {
            return false;
        }
        BookingDetails bd = bookings.remove(bookingId);
        List<RoomBooking> empBookings =  employeeBookings.get(bd.empId);
        for(Iterator<RoomBooking> itr = empBookings.iterator(); itr.hasNext(); ) {
            RoomBooking nxt = itr.next();
            if(nxt.bookingId.equals(bookingId)) {
                itr.remove();
            }
        }
        for(Iterator<Map.Entry<Integer, RoomBooking>> itr = roomBookings.get(bd.roomId).entrySet().iterator(); itr.hasNext(); ) {
            RoomBooking rb = itr.next().getValue();
            if(rb.bookingId.equals(bookingId)) {
                itr.remove();
            }
        }
        return true;
    }

    List<String> listBookingsForRoom(int roomId, int n) {
        return roomBookings.get(roomId).entrySet().stream()
                .limit(n).map(Map.Entry::getValue)
                .map(rb -> String.format("%s-%d-%d", rb.bookingId, rb.start, rb.end))
                .toList();
    }

    List<String> listBookingsForEmployee(int employeeId, int n) {
        return employeeBookings.get(employeeId).stream()
                .sorted(Comparator.comparingInt(rb -> rb.start))
                .limit(n)
                .map(rb -> String.format("%s-%d-%d", rb.bookingId, rb.start, rb.end))
                .toList();
    }

    private class BookingDetails {
        String bookingId;
        int roomId;
        int empId;

        public BookingDetails(String bookingId, int roomId, int empId) {
            this.bookingId = bookingId;
            this.roomId = roomId;
            this.empId = empId;
        }
    }

    private class RoomBooking implements Comparable<RoomBooking> {
        String bookingId;
        int roomId;
        int empId;
        int start;
        int end;

        public RoomBooking(String bookingId, int roomId, int empId, int start, int end) {
            this.bookingId = bookingId;
            this.roomId = roomId;
            this.empId = empId;
            this.start = start;
            this.end = end;
        }

        @Override
        public int compareTo(RoomBooking o) {
            return Integer.compare(this.start, o.start);
        }
    }

}
