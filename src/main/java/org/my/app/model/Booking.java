package org.my.app.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record Booking(String customerName, int tableSize, LocalDate bookingDate, LocalTime bookingTime) {

}
