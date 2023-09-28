package utils;

import java.time.LocalDate;
import java.util.*;

public class Container {

    private final String code;
    private final String destination;
    private final LocalDate DepartureDate;

    public Container(String code, String destination, LocalDate departureDate) {
        this.code = code;
        this.destination = destination;
        this.DepartureDate = departureDate;
    }

    public String getCode() {
        return code;
    }

    public String getDestination() {
        return destination;
    }

    public Container(String str){
        String[] splits = str.split("@@");
        code = splits[0];
        destination = splits[1];
        DepartureDate = LocalDate.parse(splits[2]);
    }

    public Container(Container container){
        code = container.getCode();
        destination = container.getDestination();
        DepartureDate = container.getDepartureDate();
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
            return true;

        if (!(obj instanceof Container))
            return false;

        Container container = (Container) obj;

        if (!Objects.equals(container.getCode(), this.getCode()))
            return false;
        if (!Objects.equals(container.getDestination(), this.getDestination()))
            return false;
        return Objects.equals(container.getDepartureDate(), this.getDepartureDate());
    }

    @Override
    public String toString(){
        return getCode()+"@@"+getDestination()+"@@"+getDepartureDate().toString();
    }

    public LocalDate getDepartureDate() {
        return DepartureDate;
    }

    public static List<Container> StringToContainers(String str){
        List<Container> containers = new ArrayList<>();
        if(str.length() < 4)
            return containers;
        str = str.substring(1, str.length() - 1);
        String[] cons = str.split(",");
        for (String s : cons) {
            s = s.trim();
            containers.add(new Container(s));
        }
        return containers;
    }



    public static void SortByLatestToEarliest(List<Container> containers){
       containers.sort(Comparator.comparing(Container::getDepartureDate));
       Collections.reverse(containers);
    }
}
