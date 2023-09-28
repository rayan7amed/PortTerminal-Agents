package utils;

import jade.core.AID;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Appointment {
    public AID TransporterAID;
    public LocalDateTime Arrival;
    public LocalDateTime Departure;
    public String ExpectedContainers;
    public String TransporterType;

    public Appointment(LocalDateTime d1, int numberOfMinutes, String str, AID aid, String containers){
        Arrival = d1;
        Departure = d1.plusMinutes(numberOfMinutes);
        TransporterAID = aid;
        TransporterType = str;
        ExpectedContainers = containers;
    }

    public static Appointment getAppointmentByAID(List<Appointment> objectList, AID aid) {
        for (Appointment apt : objectList) {
            if (apt.TransporterAID == aid) {
                return apt;
            }
        }
        return null;
    }

}
