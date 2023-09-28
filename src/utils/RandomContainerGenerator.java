package utils;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomContainerGenerator {

    public static int MaxTaken;
    public static List<String> PoolOfDests;

    public RandomContainerGenerator()
    {
        MaxTaken = 0;
        PoolOfDests = new ArrayList<>();

        //PoolOfDests.add("GDANSK");
        PoolOfDests.add("PORTO");
        PoolOfDests.add("MIAMI");
        PoolOfDests.add("JEDDAH");
        PoolOfDests.add("BARCELONA");

    }
    public static LocalDate generateRandomFutureDate() {
        LocalDate currentDate = LocalDate.now();
        long daysToAdd = generateRandomNumber(1, 365); // Generate a random number between 1 and 365

        return currentDate.plusDays(daysToAdd);
    }

    public static int generateRandomNumber(int min, int max) {
        Random random = new Random();
        return min + random.nextInt(max - min + 1);
    }

    public static List<Container> GenerateContainers(int NumberOfContainers)
    {
        List<Container> containers = new ArrayList<>(NumberOfContainers);

        for(int i = 0; i < NumberOfContainers; ++i){
            int int_random = generateRandomNumber(0,3);
            containers.add(new Container(Integer.toString(MaxTaken++),PoolOfDests.get(int_random),generateRandomFutureDate()));
        }
        Container.SortByLatestToEarliest(containers);
        return containers;
    }

    public static List<Container> GenerateContainersByDest(int NumberOfContainers, String dest)
    {
        List<Container> containers = new ArrayList<>(NumberOfContainers);

        for(int i = 0; i < NumberOfContainers; ++i){
            int int_random = generateRandomNumber(0,4);
            containers.add(new Container(Integer.toString(MaxTaken++),dest,generateRandomFutureDate()));
        }
        Container.SortByLatestToEarliest(containers);
        return containers;
    }
}
