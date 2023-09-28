package utils;

import java.time.LocalDate;
import java.util.*;


public class StorageBay {
    public static int N = 10;
    public static Stack<Container>[][] terminalLayout = new Stack[N][N];
    public static final int Max = 5;

    public StorageBay() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                terminalLayout[i][j] = new Stack<>();
            }
        }
    }

    public static int addContainer(Container container){

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if(terminalLayout[i][j].size() == 0) {
                    terminalLayout[i][j].push(container);
                    return 1;
                }
            }
        }
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if(terminalLayout[i][j].size() > 0 && container.getDepartureDate().isBefore(terminalLayout[i][j].peek().getDepartureDate())
                        && terminalLayout[i][j].size() < Max){
                    terminalLayout[i][j].push(container);
                    return 1;
                }
            }
        }
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                for(int k = terminalLayout[i][j].size() - 1; k >= 0; --k) {
                    if(terminalLayout[i][j].size() > 0 && terminalLayout[i][j].size() < Max)
                    {
                        Container c = terminalLayout[i][j].get(k);
                        if(container.getDepartureDate().isBefore(c.getDepartureDate())){
                            terminalLayout[i][j].add(k + 1, container);
                            if(k == 3)
                                return 1;
                            if(k == 2)
                                return 3;
                            if(k == 1)
                                return 5;
                        }
                    }

                }
            }
        }
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if(terminalLayout[i][j].size() < Max) {
                    terminalLayout[i][j].add(container);
                    return 1;
                }

            }
        }
        return 0;
    }

    public static int removeContainer(Container container){

        int movesCounter = 0;
        Stack<Container> containersRemoved = new Stack<>();
        int[] position = GetContainersPosition(container);
        if(position != null)
        {
            terminalLayout[position[0]][position[1]].remove(container);
            return 2 * position[2] + 1;
        }
        return 0;

    }

    public static int[] GetContainersPosition(Container container){
        int[] position = new int[3];
        for(int i = 0; i < N; ++i)
        {
            for(int j = 0; j < N; ++j)
            {
                for(int k = 0; k < terminalLayout[i][j].size(); ++k)
                {
                    if(container.equals(terminalLayout[i][j].get(k)))
                    {
                        position[0] = i;
                        position[1] = j;
                        position[2] = k;
                        return position;
                    }
                }
            }
        }
        System.out.println(container +" not found");
        return null;
    }
    public static List<Container> GetContainersByDest(String dest){
        List<Container> containers = new ArrayList<>();
        //List<Container> tmp = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                for (Container c:terminalLayout[i][j]) {
                    if(Objects.equals(c.getDestination(), dest)){
                        containers.add(c);
                    }
                }

            }
        }
        Container.SortByLatestToEarliest(containers);
        return containers;
    }

    public static int GetNumberOfContainers(){
        int result = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                result += terminalLayout[i][j].size();
            }
        }
        return result;
    }
    public static <T> Stack<T>[][] convertArrayOfListsToStacks(List<T>[][] lists) {
        @SuppressWarnings("unchecked")
        Stack<T>[][] stacks = new Stack[lists.length][lists.length];
        for (int i = 0; i < lists.length; i++) {
            for(int j = 0; j < lists.length; j++) {
                stacks[i][j] = new Stack<>();
            }
        }
        for (int i = 0; i < lists.length; i++) {
            for(int j = 0; j < lists.length; j++) {
                for (T element : lists[i][j]) {
                    stacks[i][j].push(element);
                }
            }
        }
        return stacks;
    }

    public static <T> List<T>[][] convertArrayOfStacksToLists(Stack<T>[][] stacks) {
        @SuppressWarnings("unchecked")
        List<T>[][] lists = new List[stacks.length][stacks.length];

        // Convert each stack to a list and store it in the array
        for (int i = 0; i < stacks.length; i++) {
            for(int j = 0; j < stacks.length; j++)
                lists[i][j] = new ArrayList<>(stacks[i][j]);
        }

        return lists;
    }

    public static String TerminalLayoutToString(){
        Stack<Container>[][] layout = terminalLayout;
        StringBuilder result = new StringBuilder();
        result.append("[");
        for (int i = 0; i < layout.length; i++) {
            for (int j = 0; j < layout.length; j++)
            {
                String str = layout[i][j].toString();
                str = str.substring(1, str.length() - 1);
                result.append(layout[i][j].toString());
                result.append(",");
            }
        }
        result.append("]");
        return result.toString();
    }

}

